package com.naixue.nxdp.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.sql.DataSource;

import org.apache.hive.jdbc.HiveDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.naixue.nxdp.config.CFG;
import com.naixue.nxdp.config.JDBC;
import com.naixue.nxdp.dao.MetadataHiveTableEditionRepository;
import com.naixue.nxdp.dao.MetadataHiveTableRepository;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.model.metadata.MetadataHiveDb;
import com.naixue.nxdp.model.metadata.MetadataHiveTable;
import com.naixue.nxdp.model.metadata.MetadataHiveTable.TableEdition;
import com.naixue.nxdp.util.DateUtils;

@Service
public class MetadataHiveTableService {

    @Autowired
    private MetadataHiveTableRepository metadataHiveTableRepository;

    @Autowired
    private MetadataHiveTableEditionRepository metadataHiveTableEditionRepository;

    @Autowired
    private MetadataHiveDbService metadataHiveDbService;

    @Autowired
    private UserService userService;

    @Autowired
    private HadoopBindingService hadoopBindingService;

    public static JdbcTemplate createHiveJdbcJdbcTemplate(
            String url, String username, String password) {
        DataSource dataSource = new SimpleDriverDataSource(new HiveDriver(), url, username, password);
        return new JdbcTemplate(dataSource);
    }

    private String buildMetadataHiveTableFullName(MetadataHiveTable.TableEdition tableEdition) {
        Assert.notNull(tableEdition, "入参不允许为空");
        Assert.notNull(tableEdition.getDbId(), "数据库ID不允许为空");
        Assert.notNull(tableEdition.getName(), "表名不允许为空");
        Assert.notNull(tableEdition.getUpdateType(), "表更新方式不允许为空");
        Assert.notNull(tableEdition.getPartition(), "表分区方式不允许为空");
        MetadataHiveDb db = metadataHiveDbService.getById(tableEdition.getDbId());
        return db.getCode()
                + "_"
                + tableEdition.getName()
                + MetadataHiveTable.TableUpdateType.getEnum(tableEdition.getUpdateType()).getName()
                + MetadataHiveTable.TablePartition.getEnum(tableEdition.getPartition()).getName();
    }

    @Transactional
    public MetadataHiveTable save(MetadataHiveTable table) {
        if (table.getLastEdition().getId() != null) {
            table.getLastEdition().setId(null);
        }
        if (table.getLastEdition().getTableId() != null) {
            table.setId(table.getLastEdition().getTableId());
        }
        String tableFullName = buildMetadataHiveTableFullName(table.getLastEdition());
        table.setName(table.getLastEdition().getName());
        table.setFullName(tableFullName);
        table.setStatus(MetadataHiveTable.TableStatus.DRAFT.getCode());
        MetadataHiveTable newTable = metadataHiveTableRepository.save(table);
        MetadataHiveTable.TableEdition tableEdition = table.getLastEdition();
        tableEdition.setTableId(newTable.getId());
        tableEdition.setFullName(tableFullName);
        tableEdition.setStatus(MetadataHiveTable.TableStatus.DRAFT.getCode());
        tableEdition.setVersion(UUID.randomUUID().toString());
        MetadataHiveTable.TableEdition newTableEdition =
                metadataHiveTableEditionRepository.save(tableEdition);
        newTable.setLastEdition(newTableEdition);
        return newTable;
    }

    public MetadataHiveTable getTableByTableId(Integer tableId) {
        MetadataHiveTable table = metadataHiveTableRepository.findOne(tableId);
        Assert.notNull(table, "表不存在，ID=" + tableId);
        List<MetadataHiveTable.TableEdition> tableEditions =
                metadataHiveTableEditionRepository.findByTableIdAndStatusInOrderByIdDesc(
                        tableId, Arrays.asList(0, 1));
        if (!CollectionUtils.isEmpty(tableEditions)) {
            table.setEditions(tableEditions);
            for (MetadataHiveTable.TableEdition tableEdition : tableEditions) {
                User creator = userService.getUserByUserId(tableEdition.getCreatorId());
                if (creator != null) {
                    tableEdition.setCreator(creator.getName());
                }
                User modifier = userService.getUserByUserId(tableEdition.getModifierId());
                if (modifier != null) {
                    tableEdition.setModifier(modifier.getName());
                }
            }
        }
        User creator = userService.getUserByUserId(table.getCreatorId());
        if (creator != null) {
            table.setCreator(creator.getName());
        }
        User modifier = userService.getUserByUserId(table.getModifierId());
        if (modifier != null) {
            table.setModifier(modifier.getName());
        }
        return table;
    }

    public MetadataHiveTable.TableEdition getTableEditionByEditionId(Integer editionId) {
        MetadataHiveTable.TableEdition table = metadataHiveTableEditionRepository.findOne(editionId);
        User creator = userService.getUserByUserId(table.getCreatorId());
        if (creator != null) {
            table.setCreator(creator.getName());
        }
        return table;
    }

    @Transactional
    public void deleteTableEditionByTableEditionId(final Integer tableEditionId) {
        TableEdition source = metadataHiveTableEditionRepository.findOne(tableEditionId);
        if (source.getStatus() != 0) {
            throw new RuntimeException("当前版本状态不允许删除");
        }
        source.setStatus(-1);
        metadataHiveTableEditionRepository.save(source);
    }

    @Transactional
    public void delete(User currentUser, Integer id) {
    /*MetadataHiveTable table = metadataHiveTableRepository.findOne(id);
    if (table == null) {
      return;
    }*/
        List<MetadataHiveTable.TableEdition> tableEditions =
                metadataHiveTableEditionRepository.findByTableIdAndStatus(
                        id, MetadataHiveTable.TableStatus.FORMAL.getCode());
        if (CollectionUtils.isEmpty(tableEditions)) {
            return;
        }
        if (tableEditions.size() > 1) {
            throw new RuntimeException("设计历史中包含2个及以上的正式版本，删表失败");
        }
        MetadataHiveTable.TableEdition edition = tableEditions.get(0);
        MetadataHiveDb db = metadataHiveDbService.getById(edition.getDbId());
    /*if (MetadataHiveTable.TableStatus.FORMAL.getCode().equals(table.getStatus())) {
      throw new RuntimeException("正式版本不允许删除");
    }*/
        String hadoopUserGroupName =
                hadoopBindingService.getHadoopUserGroupName(currentUser.getDeptId());
        // HiveJdbcUtils.executeUpdate(hadoopUserGroupName, sql);
        JdbcTemplate jdbcTemplate =
                createHiveJdbcJdbcTemplate(JDBC.HIVE_JDBC_URL, hadoopUserGroupName, null);
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + db.getName() + "." + edition.getFullName());
        MetadataHiveTable table = metadataHiveTableRepository.findOne(edition.getTableId());
        table.setStatus(MetadataHiveTable.TableStatus.DELETE.getCode());
        table.setModifierId(currentUser.getId());
        metadataHiveTableRepository.save(table);
    }

    public Page<MetadataHiveTable> list(
            MetadataHiveTable condition, Integer pageIndex, Integer pageSize) {
        Specification<MetadataHiveTable> specification =
                new Specification<MetadataHiveTable>() {
                    @Override
                    public Predicate toPredicate(
                            Root<MetadataHiveTable> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                        if (condition == null) {
                            return null;
                        }
                        List<Predicate> data = new ArrayList<>();
                        if (condition.getQueryStartDate() != null) {
                            Predicate p =
                                    cb.greaterThanOrEqualTo(
                                            root.get("createTime").as(Date.class), condition.getQueryStartDate());
                            data.add(p);
                        }
                        if (condition.getQueryEndDate() != null) {
                            Predicate p =
                                    cb.lessThanOrEqualTo(
                                            root.get("createTime").as(Date.class),
                                            DateUtils.getEndingOfDay(condition.getQueryEndDate()));
                            data.add(p);
                        }
                        if (!StringUtils.isEmpty(condition.getCreatorId())) {
                            if (!condition.getCreatorId().equals("0")) {
                                data.add(cb.equal(root.get("creatorId"), condition.getCreatorId()));
                            }
                        }
                        if (!StringUtils.isEmpty(condition.getName())) {
                            data.add(cb.like(root.get("fullName"), "%" + condition.getName() + "%"));
                        }
                        if (condition.getStatus() != null) {
                            data.add(cb.equal(root.get("status"), condition.getStatus()));
                        }
                        return cb.and(data.toArray(new Predicate[data.size()]));
                    }
                };
        Page<MetadataHiveTable> page =
                metadataHiveTableRepository.findAll(
                        specification,
                        new PageRequest(
                                pageIndex, pageSize, new Sort(new Order(Direction.DESC, "createTime"))));
        if (page != null && !CollectionUtils.isEmpty(page.getContent())) {
            List<MetadataHiveTable> list = page.getContent();
            for (MetadataHiveTable table : list) {
                User user = userService.getUserByUserId(table.getCreatorId());
                if (user != null) {
                    table.setCreator(user.getName());
                }
            }
        }
        return page;
    }

    @Transactional
    public void executeHiveTableSql(
            User currentUser, MetadataHiveTable.TableEdition table, String sql) {
        MetadataHiveTable hiveTable = metadataHiveTableRepository.findOne(table.getTableId());
        if (!hiveTable.getStatus().equals(MetadataHiveTable.TableStatus.DRAFT.getCode())) {
            throw new RuntimeException("表当前状态非草稿，不允许执行建表");
        }
        hiveTable.setModifierId(currentUser.getId());
        hiveTable.setStatus(MetadataHiveTable.TableStatus.FORMAL.getCode());
        metadataHiveTableRepository.save(hiveTable);
        table.setModifierId(currentUser.getId());
        table.setStatus(MetadataHiveTable.TableStatus.FORMAL.getCode());
        metadataHiveTableEditionRepository.save(table);
        String hadoopUserGroupName =
                hadoopBindingService.getHadoopUserGroupName(currentUser.getDeptId());
        // HiveJdbcUtils.executeUpdate(hadoopUserGroupName, sql);
        JdbcTemplate jdbcTemplate =
                createHiveJdbcJdbcTemplate(JDBC.HIVE_JDBC_URL, hadoopUserGroupName, null);
        jdbcTemplate.execute(sql);
    }

    public String parseHiveTableSql(Integer editionId) {
        MetadataHiveTable.TableEdition table = getTableEditionByEditionId(editionId);
        Assert.notNull(table, "请求参数editionId对应的数据不存在");
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE ");
        if (MetadataHiveTable.TableGroup.OUTER.getCode().equals(table.getGroup())) {
            sql.append("EXTERNAL ");
        }
        MetadataHiveDb db = metadataHiveDbService.getById(table.getDbId());
        if (db == null) {
            throw new RuntimeException("元数据HIVE库id=" + table.getDbId() + "不存在");
        }
        sql.append("TABLE ")
                .append(db.getName())
                .append(".")
                .append(
                        MetadataHiveTable.TableLevel.getEnum(table.getLevel()).toString().toLowerCase()
                                + "_"
                                + table.getName())
                .append(MetadataHiveTable.TableUpdateType.getEnum(table.getUpdateType()).getName())
                .append(MetadataHiveTable.TablePartition.getEnum(table.getPartition()).getName())
                .append("(")
                .append(System.lineSeparator());
        if (!StringUtils.isEmpty(table.getJson())) {
            JSONArray data = JSON.parseArray(table.getJson());
            List<Object> columns = new ArrayList<>();
            for (Object column : data) {
                JSONObject obj = (JSONObject) column;
                JSONArray marker = obj.getJSONArray("columnPartitionMarker");
                if (CollectionUtils.isEmpty(marker) || !marker.get(0).equals("true")) {
                    columns.add(column);
                }
            }
            String columnSql = parseHiveTableColumnSql(JSON.toJSONString(columns));
            sql.append(columnSql);
        }
        sql.append(")");
        sql.append("COMMENT ")
                .append("'" + table.getDescription() + "'")
                .append(System.lineSeparator());
        // 分区
        if (!MetadataHiveTable.TablePartition._0P.getCode().equals(table.getPartition())) {
            JSONArray columns = JSON.parseArray(table.getJson());
            if (!CollectionUtils.isEmpty(columns)) {
                List<Object> partitions = new ArrayList<>();
                for (Object column : columns) {
                    JSONObject obj = (JSONObject) column;
                    JSONArray marker = obj.getJSONArray("columnPartitionMarker");
                    if (!CollectionUtils.isEmpty(marker) && marker.get(0).equals("true")) {
                        partitions.add(column);
                    }
                }
                if (!CollectionUtils.isEmpty(partitions)) {
                    String partitionSql = parseHiveTableColumnSql(JSON.toJSONString(partitions));
                    sql.append("PARTITIONED BY ")
                            .append("(")
                            .append(partitionSql)
                            .append(")")
                            .append(System.lineSeparator());
                }
            }
        }
        // 当存储格式为textfile时自定义分隔符
        if (MetadataHiveTable.TableStorageFormat.TEXTFILE.getCode().equals(table.getStorageFormat())) {
            sql.append("ROW FORMAT DELIMITED FIELDS TERMINATED BY '" + table.getCharSeparator() + "'")
                    .append(System.lineSeparator());
        }
        // 存储类型
        if (!db.getName().equals("hdp_zhuanzhuan_rawdb_global")) {
            // sql.append("stored as parquet ").append(System.lineSeparator());
            sql.append(
                    "stored as "
                            + MetadataHiveTable.TableStorageFormat.getEnum(table.getStorageFormat())
                            .getName())
                    .append(System.lineSeparator());
        }
        User creator = userService.getUserByUserId(table.getCreatorId());
        if (creator == null) {
            throw new RuntimeException("用户id=" + table.getCreatorId() + "不存在");
        }
        String hadoopUserGroupName = hadoopBindingService.getHadoopUserGroupName(creator.getDeptId());
        // 判断是否配置了路径若配置则使用配置的路径
        if (table.getModifyLocation() == 0) {
            sql.append(
                    "location '"
                            + CFG.HDFS_CLUSTER_URL_PREFIX
                            + "/"
                            + hadoopUserGroupName
                            + "/warehouse/"
                            + db.getName()
                            + "/"
                            + MetadataHiveTable.TableLevel.getEnum(table.getLevel()).toString().toLowerCase()
                            + "_"
                            + table.getName()
                            + MetadataHiveTable.TableUpdateType.getEnum(table.getUpdateType()).getName()
                            + MetadataHiveTable.TablePartition.getEnum(table.getPartition()).getName())
                    .append("'");
        } else {
            sql.append("location '" + table.getLocation()).append("'");
        }
        return sql.toString();
    }

    private String parseHiveTableColumnSql(String json) {
        Assert.hasText(json, "请求参数json不允许为空");
        JSONArray array = JSON.parseArray(json);
        if (CollectionUtils.isEmpty(array)) {
            throw new RuntimeException("元数据HIVE表的字段数据为空");
        }
        StringBuilder sql = new StringBuilder();
        for (int i = 0; i < array.size(); i++) {
            JSONObject column = (JSONObject) array.get(i);
            sql.append("`").append(column.getString("columnName")).append("`").append(" ");
            sql.append(column.getString("columnType")).append(" ");
            sql.append("COMMENT").append(" ");
            sql.append("'").append(column.getString("columnCname")).append("'");
            if (i != array.size() - 1) {
                sql.append(",");
            }
            sql.append(System.lineSeparator());
        }
        return sql.toString();
    }
}
