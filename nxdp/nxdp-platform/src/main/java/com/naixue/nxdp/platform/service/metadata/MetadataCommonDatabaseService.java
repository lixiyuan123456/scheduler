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
import com.naixue.nxdp.data.dao.metadata.MetadataCommonDatabaseDao;
import com.naixue.nxdp.data.model.metadata.MetadataCommonDatabase;
import com.naixue.nxdp.data.model.metadata.MetadataCommonServer;
import com.naixue.nxdp.data.model.metadata.MetadataType;
import com.naixue.nxdp.data.util.JdbcTemplateUtils;

@Service
public class MetadataCommonDatabaseService {

  @Autowired private MetadataCommonDatabaseDao metadataCommonDatabaseDao;

  @Autowired private MetadataCommonServerService metadataCommonServerService;

  public Page<MetadataCommonDatabase> listMetadataCommonDatabases(
      MetadataCommonDatabase condition, Integer pageIndex, Integer pageSize, Sort.Order... orders) {
    Specification<MetadataCommonDatabase> specification =
        new Specification<MetadataCommonDatabase>() {
          @Override
          public Predicate toPredicate(
              Root<MetadataCommonDatabase> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            if (Objects.isNull(condition)) {
              return null;
            }
            List<Predicate> data = new ArrayList<>();
            if (condition.getServerId() != null && condition.getServerId() != 0) {
              data.add(cb.equal(root.get("serverId"), condition.getServerId()));
            }
            if (!Strings.isNullOrEmpty(condition.getSearch())) {
              data.add(cb.like(root.get("name"), "%" + condition.getSearch() + "%"));
            }
            return cb.and(data.toArray(new Predicate[data.size()]));
          }
        };
    Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(orders));
    return metadataCommonDatabaseDao.findAll(specification, pageable);
  }

  public List<MetadataCommonDatabase> listMetadataCommonDatabases() {
    return metadataCommonDatabaseDao.findByStatus(Status.NORMAL.getCode());
  }

  public List<MetadataCommonDatabase> listMetadataCommonDatabases(final Integer serverId) {
    return metadataCommonDatabaseDao.findByServerId(serverId);
  }

  public List<MetadataCommonDatabase> listMetadataCommonDatabases(
      final MetadataType type, final Integer serverId) {
    return metadataCommonDatabaseDao.findByTypeAndServerId(type.getCode(), serverId);
  }

  public MetadataCommonDatabase getMetadataCommonDatabaseByDatabaseId(final Integer databaseId) {
    Assert.notNull(databaseId, "请求参数databaseId不允许为空");
    return metadataCommonDatabaseDao.findOne(databaseId);
  }

  @org.springframework.transaction.annotation.Transactional
  public void syncUpdateDatabasesByServerId(final Integer serverId) {
    Assert.notNull(serverId, "数据库ID不允许为空");
    MetadataCommonServer server =
        metadataCommonServerService.getMetadataCommonServerByServerId(serverId);
    Assert.notNull(server, "不存在ID=" + serverId + "的数据库");
    // DbType type = JdbcTemplateUtils.DbType.valueOf(server.getMetadataType().name());
    /*List<Map<String, Object>> list = new ArrayList<>();
    JdbcUtils jdbcClient =
        JdbcUtils.builder()
            .driverClassName(type.getDriverClassName())
            .url(
                JdbcUtils.UrlBuilder.builder()
                    .host(server.getHost())
                    .port(server.getPort())
                    .build()
                    .toString())
            .username(server.getUsername())
            .password(SecurityUtils.simpleDecrypt(server.getPassword()))
            .build();
    jdbcClient.executeQuery(
        new ResultSetProcessor() {
          @Override
          public void processResultSet(ResultSet resultSet) throws Exception {
            Map<String, Object> row = new HashMap<>();
            while (resultSet.next()) {
              row.put("DATABASE", resultSet.getString(1));
            }
            list.add(row);
          }
        },
        "SHOW DATABASES");*/
    JdbcTemplate jdbcTemplate =
        JdbcTemplateUtils.createJdbcTemplate(
            JdbcTemplateUtils.DbType.valueOf(server.getMetadataType().getName()),
            server.getHost(),
            server.getPort(),
            null,
            server.getUsername(),
            SecurityUtils.simpleDecrypt(server.getPassword()));
    /*List<Map<String, Object>> list = null;
    if (type == JdbcTemplateUtils.DbType.MYSQL) {
      list = jdbcTemplate.queryForList("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA");
    }
    if (type == JdbcTemplateUtils.DbType.HIVE) {
      list =
          jdbcTemplate.queryForList(
              "SELECT DB_ID, `DESC`, DB_LOCATION_URI, `NAME`, OWNER_NAME , OWNER_TYPE FROM DBS");
    }*/
    List<Map<String, Object>> list = jdbcTemplate.queryForList("SHOW DATABASES");
    if (CollectionUtils.isEmpty(list)) {
      return;
    }
    List<MetadataCommonDatabase> databases = new ArrayList<>();
    for (int i = 0; i < list.size(); i++) {
      /*if (type == JdbcTemplateUtils.DbType.MYSQL) {
        database.setName((String) list.get(i).get("SCHEMA_NAME"));
      }
      if (type == JdbcTemplateUtils.DbType.HIVE) {
        database.setName((String) list.get(i).get("NAME"));
      }*/
      Iterator<Entry<String, Object>> iterator = list.get(i).entrySet().iterator();
      if (iterator.hasNext()) {
        Entry<String, Object> entry = iterator.next();
        MetadataCommonDatabase database = new MetadataCommonDatabase();
        database.setName((String) entry.getValue());
        database.setServerId(serverId);
        database.setType(server.getType());
        databases.add(database);
      }
    }
    // 从数据库查出有效的数据与新数据找差集
    List<MetadataCommonDatabase> existDatabases =
        metadataCommonDatabaseDao.findByStatusAndServerId(Status.NORMAL.getCode(), serverId);
    // toSaveDatabases = databases - existDatabases
    List<MetadataCommonDatabase> toSaveDatabases = new ArrayList<>();
    toSaveDatabases.addAll(databases);
    toSaveDatabases.removeAll(existDatabases);
    // toDeleteDatabases = existDatabases - databases
    existDatabases.removeAll(databases);
    if (!CollectionUtils.isEmpty(existDatabases)) {
      for (MetadataCommonDatabase toDeleteDatabase : existDatabases) {
        toDeleteDatabase.setStatus(Status.DELETED.getCode());
      }
    }
    toSaveDatabases.addAll(existDatabases);
    metadataCommonDatabaseDao.save(toSaveDatabases);
  }
}
