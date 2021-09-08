package com.naixue.nxdp.platform.service.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Strings;
import com.naixue.zzdp.common.util.SecurityUtils;
import com.naixue.nxdp.data.Status;
import com.naixue.nxdp.data.dao.metadata.MetadataCommonTableDao;
import com.naixue.nxdp.data.model.metadata.MetadataCommonDatabase;
import com.naixue.nxdp.data.model.metadata.MetadataCommonServer;
import com.naixue.nxdp.data.model.metadata.MetadataCommonTable;
import com.naixue.nxdp.data.model.metadata.MetadataType;
import com.naixue.nxdp.data.util.JdbcTemplateUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MetadataCommonTableService {

  @Autowired private MetadataCommonTableDao metadataCommonTableDao;
  @Autowired private MetadataCommonDatabaseService metadataCommonDatabaseService;
  @Autowired private MetadataCommonServerService metadataCommonServerService;

  public Page<MetadataCommonTable> listMetadataCommonTables(
      MetadataCommonTable condition, Integer pageIndex, Integer pageSize, Sort.Order... orders) {
    Specification<MetadataCommonTable> specification =
        new Specification<MetadataCommonTable>() {
          @Override
          public Predicate toPredicate(
              Root<MetadataCommonTable> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            if (Objects.isNull(condition)) {
              return null;
            }
            List<Predicate> data = new ArrayList<>();
            if (condition.getServerId() != null && condition.getServerId() != 0) {
              data.add(cb.equal(root.get("serverId"), condition.getServerId()));
            }
            if (condition.getDatabaseId() != null && condition.getDatabaseId() != 0) {
              data.add(cb.equal(root.get("databaseId"), condition.getDatabaseId()));
            }
            if (!Strings.isNullOrEmpty(condition.getSearch())) {
              data.add(cb.like(root.get("name"), "%" + condition.getSearch() + "%"));
            }
            return cb.and(data.toArray(new Predicate[data.size()]));
          }
        };
    Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(orders));
    return metadataCommonTableDao.findAll(specification, pageable);
  }

  public List<MetadataCommonTable> listMetadataCommonTables() {
    return metadataCommonTableDao.findByStatus(Status.NORMAL.getCode());
  }

  public List<MetadataCommonTable> listMetadataCommonTablesWithFullName() {
    List<MetadataCommonTable> list = listMetadataCommonTables();
    if (!CollectionUtils.isEmpty(list)) {
      return list;
    }
    for (MetadataCommonTable table : list) {
      MetadataCommonDatabase database =
          metadataCommonDatabaseService.getMetadataCommonDatabaseByDatabaseId(
              table.getDatabaseId());
      if (database == null) {
        throw new RuntimeException("database[id=" + table.getDatabaseId() + "] is not exist.");
      }
      table.setDatabaseName(database.getName());
    }
    return list;
  }

  public List<MetadataCommonTable> listMetadataCommonTables(
      final Integer serverId, final Integer databaseId) {
    return metadataCommonTableDao.findByServerIdAndDatabaseId(serverId, databaseId);
  }

  public List<MetadataCommonTable> listMetadataCommonTables(
      final MetadataType type, final Integer databaseId) {
    return metadataCommonTableDao.findByTypeAndDatabaseId(type.getCode(), databaseId);
  }

  public List<MetadataCommonTable> listMetadataCommonTablesByTableNameWithFullName(
      final String tableName) {
    if (Strings.isNullOrEmpty(tableName)) {
      return new ArrayList<>();
    }
    List<MetadataCommonTable> tables =
        metadataCommonTableDao.findTop10ByStatusAndNameLike(
            Status.NORMAL.getCode(), "%" + tableName + "%");
    return extendTablesWithFullName(tables);
  }

  public List<MetadataCommonTable> listMetadataCommonTablesByTableIdsWithFullName(
      final List<Integer> tableIds) {
    if (CollectionUtils.isEmpty(tableIds)) {
      return new ArrayList<>();
    }
    List<MetadataCommonTable> tables = metadataCommonTableDao.findByIdIn(tableIds);
    return extendTablesWithFullName(tables);
  }

  private List<MetadataCommonTable> extendTablesWithFullName(List<MetadataCommonTable> tables) {
    if (CollectionUtils.isEmpty(tables)) {
      return new ArrayList<>();
    }
    for (MetadataCommonTable table : tables) {
      MetadataCommonDatabase database =
          metadataCommonDatabaseService.getMetadataCommonDatabaseByDatabaseId(
              table.getDatabaseId());
      if (database == null) {
        throw new RuntimeException("database[id=" + table.getDatabaseId() + "] is not exist.");
      }
      table.setDatabaseName(database.getName());
      table.setFullName(table.getDatabaseName() + "." + table.getName());
    }
    return tables;
  }

  @org.springframework.transaction.annotation.Transactional
  public void syncUpdateTablesByDatabaseId(final Integer databaseId) {
    Assert.notNull(databaseId, "databaseId is not allowed to be null");
    MetadataCommonDatabase database =
        metadataCommonDatabaseService.getMetadataCommonDatabaseByDatabaseId(databaseId);
    Assert.notNull(database, "database[id=" + databaseId + "] is not exist");
    MetadataCommonServer server =
        metadataCommonServerService.getMetadataCommonServerByServerId(database.getServerId());
    // DbType type = JdbcTemplateUtils.DbType.valueOf(server.getMetadataType().name());
    JdbcTemplate jdbcTemplate =
        JdbcTemplateUtils.createJdbcTemplate(
            JdbcTemplateUtils.DbType.valueOf(server.getMetadataType().getName()),
            server.getHost(),
            server.getPort(),
            null,
            server.getUsername(),
            SecurityUtils.simpleDecrypt(server.getPassword()));
    List<Map<String, Object>> list =
        jdbcTemplate.queryForList("SHOW TABLES FROM " + database.getName());
    if (CollectionUtils.isEmpty(list)) {
      return;
    }
    List<MetadataCommonTable> tables = new ArrayList<>();
    for (int i = 0; i < list.size(); i++) {
      Iterator<Entry<String, Object>> iterator = list.get(i).entrySet().iterator();
      if (iterator.hasNext()) {
        Entry<String, Object> entry = iterator.next();
        MetadataCommonTable table = new MetadataCommonTable();
        table.setName((String) entry.getValue());
        table.setType(database.getType());
        table.setServerId(server.getId());
        table.setDatabaseId(database.getId());
        /*List<Map<String, Object>> columnsList =
            jdbcTemplate.queryForList(
                "SHOW COLUMNS IN " + database.getName() + "." + entry.getValue());
        if (!CollectionUtils.isEmpty(columnsList)) {
          table.setColumns(JSON.toJSONString(columnsList));
        }*/
        tables.add(table);
      }
    }
    List<MetadataCommonTable> existTables =
        metadataCommonTableDao.findByStatusAndDatabaseId(Status.NORMAL.getCode(), databaseId);
    // toSaveTables = tables - existTables
    List<MetadataCommonTable> toSaveTables = new ArrayList<>();
    toSaveTables.addAll(tables);
    toSaveTables.removeAll(existTables);
    // toDeleteTables = existTables - tables
    existTables.removeAll(tables);
    if (!CollectionUtils.isEmpty(existTables)) {
      for (MetadataCommonTable toDeleteTable : existTables) {
        toDeleteTable.setStatus(Status.DELETED.getCode());
      }
    }
    toSaveTables.addAll(existTables);
    metadataCommonTableDao.save(toSaveTables);
  }

  @org.springframework.transaction.annotation.Transactional
  public void saveMetadataCommonTable(MetadataCommonTable table) {
    metadataCommonTableDao.save(table);
  }

  public List<MetadataCommonTable> fillMetadataCommonTables(final List<Integer> tableIds) {
    Assert.notEmpty(tableIds, "request parameter tableIds is not allowed to be null");
    List<MetadataCommonTable> list = new ArrayList<>();
    for (Integer tableId : tableIds) {
      MetadataCommonTable table = metadataCommonTableDao.findOne(tableId);
      MetadataCommonServer server =
          metadataCommonServerService.getMetadataCommonServerByServerId(table.getServerId());
      table.setServer(server);
      MetadataCommonDatabase database =
          metadataCommonDatabaseService.getMetadataCommonDatabaseByDatabaseId(
              table.getDatabaseId());
      table.setDatabase(database);
      list.add(table);
    }
    return list;
  }
}
