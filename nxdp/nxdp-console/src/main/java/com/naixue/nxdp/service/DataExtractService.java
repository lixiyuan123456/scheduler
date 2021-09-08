package com.naixue.nxdp.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.naixue.zzdp.common.util.SecurityUtils;
import com.naixue.nxdp.dao.JobUpdateRecordRepository;
import com.naixue.nxdp.dao.ServerConfigRepository;
import com.naixue.zzdp.data.model.metadata.MetadataCommonDatabase;
import com.naixue.zzdp.data.model.metadata.MetadataCommonServer;
import com.naixue.zzdp.data.model.metadata.MetadataCommonTable;
import com.naixue.zzdp.data.model.metadata.MetadataType;
import com.naixue.zzdp.data.util.JdbcTemplateUtils;
import com.naixue.nxdp.model.JobConfig;
import com.naixue.nxdp.model.JobUpdateRecord;
import com.naixue.nxdp.model.ServerConfig;
import com.naixue.nxdp.model.User;
import com.naixue.zzdp.platform.service.metadata.MetadataCommonDatabaseService;
import com.naixue.zzdp.platform.service.metadata.MetadataCommonServerService;
import com.naixue.zzdp.platform.service.metadata.MetadataCommonTableService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DataExtractService {

    private static final Integer HADOOP_METADATA_DB_ID = 0;

    @Autowired
    private ServerConfigRepository serverConfigRepository;

    @Autowired
    private DevTaskService devTaskService;

    @Autowired
    private JobUpdateRecordRepository jobUpdateRecordRepository;

    @Autowired
    private JdbcTemplate hadoopMetadataJdbcTemplate;

    @Autowired
    private MetadataCommonServerService metadataCommonServerService;

    @Autowired
    private MetadataCommonDatabaseService metadataCommonDatabaseService;

    @Autowired
    private MetadataCommonTableService metadataCommonTableService;

    @Transactional
    public Integer save(User currentUser, JobConfig jobConfig) {
        // 校验名称
        devTaskService.checkJobName(jobConfig.getJobName(), jobConfig.getId());
        if (StringUtils.isEmpty(jobConfig.getDetails())) {
            throw new RuntimeException("任务JSON数据不存在");
        }
        // 解析
        JSONObject json = JSON.parseObject(jobConfig.getDetails());
        String filePath =
                StringUtils.isEmpty(json.getString("filePath")) ? "" : json.getString("filePath");
        String fileName =
                StringUtils.isEmpty(json.getString("fileName")) ? "" : json.getString("fileName");
        // Integer uploadFlag = json.getInteger("uploadFlag");
        Integer[] dependences = devTaskService.parseDependence(json.getString("dependencies"));
        // 保存
        if (jobConfig.getId() == null) {
            devTaskService.addJob(jobConfig, currentUser, dependences);
        } else {
            devTaskService.updateJob(jobConfig, currentUser, dependences);
        }
        // 更新记录
        String operatorId = currentUser.getId();
        String operatorName = currentUser.getName();
        JobUpdateRecord jobUpdateRecord =
                new JobUpdateRecord(
                        jobConfig.getId(),
                        operatorId,
                        operatorName,
                        fileName,
                        filePath,
                        "",
                        Timestamp.valueOf(LocalDateTime.now()));
        jobUpdateRecordRepository.save(jobUpdateRecord);
        return jobConfig.getId();
    }

    // @Cacheable(value = "data-extract-cache", key = "#root.method.name + #p0 + #p1")
    public List<Map<String, Object>> fetchServers(
            ServerConfig.ServerType serverType, ServerConfig.LogicType logicType) throws Exception {
        List<Map<String, Object>> data = new ArrayList<>();
    /*if (ServerConfig.ServerType.MYSQL == serverType) {
      List<ServerConfig> list = null;
      if (ServerConfig.LogicType.DATA_SOURCE == logicType) {
        list =
            serverConfigRepository.findByServerTypeAndLogicType(
                ServerConfig.ServerType.MYSQL.getId(), ServerConfig.LogicType.DATA_SOURCE.getId());
      }
      if (ServerConfig.LogicType.DATA_WAREHOUSE == logicType) {
        list =
            serverConfigRepository.findByServerTypeAndLogicType(
                ServerConfig.ServerType.MYSQL.getId(),
                ServerConfig.LogicType.DATA_WAREHOUSE.getId());
      }
      if (!CollectionUtils.isEmpty(list)) {
        for (ServerConfig serverConfig : list) {
          Map<String, Object> map = new HashMap<>();
          map.put("id", serverConfig.getId());
          map.put("filename", serverConfig.getName());
          map.put("serverType", serverConfig.getServerType());
          data.add(map);
        }
      }
    }
    if (ServerConfig.ServerType.HIVE == serverType) {
      Map<String, Object> map = new HashMap<>();
      map.put("id", HADOOP_METADATA_DB_ID);
      map.put("filename", JDBC.HADOOP_METADATA_DB_NAME);
      map.put("serverType", serverType);
      data.add(map);
    }*/
        List<MetadataCommonServer> servers =
                metadataCommonServerService.listMetadataCommonServers(
                        MetadataType.valueOf(serverType.name()));
        if (!CollectionUtils.isEmpty(servers)) {
            for (MetadataCommonServer server : servers) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", server.getId());
                map.put("filename", server.getName());
                map.put("serverType", server.getType());
                data.add(map);
            }
        }
        return data;
    }

    // @Cacheable(value = "data-extract-cache", key = "#root.method.name + #p0 + #p1")
    public List<Map<String, Object>> fetchDbs(ServerConfig.ServerType serverType, Integer serverId) {
        List<Map<String, Object>> data = new ArrayList<>();
    /*if (ServerConfig.ServerType.MYSQL == serverType) {
      JdbcTemplate jdbcTemplate = createJdbcTemplate(serverType, serverId);
      List<Map<String, Object>> list =
          jdbcTemplate.queryForList("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA");
      List<String> list =newJdbcTemplate(serverType, serverId).queryForList("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA", String.class);
      if (!CollectionUtils.isEmpty(list)) {
        for (int i = 0; i < list.size(); i++) {
          Map<String, Object> map = new HashMap<>();
          map.put("id", String.valueOf(i + 1));
          map.put("db_name", list.get(i).get("SCHEMA_NAME"));
          data.add(map);
        }
      }
    }
    if (ServerConfig.ServerType.HIVE == serverType) {
      List<Map<String, Object>> list =
          hadoopMetadataJdbcTemplate.queryForList(
              "SELECT DB_ID, `DESC`, DB_LOCATION_URI, `NAME`, OWNER_NAME , OWNER_TYPE FROM DBS");
      if (!CollectionUtils.isEmpty(list)) {
        for (Map<String, Object> eMap : list) {
          Map<String, Object> map = new HashMap<>();
          map.put("id", eMap.get("DB_ID"));
          map.put("db_name", eMap.get("NAME"));
          data.add(map);
        }
      }
    }*/
        List<MetadataCommonDatabase> databases =
                metadataCommonDatabaseService.listMetadataCommonDatabases(
                        MetadataType.valueOf(serverType.name()), serverId);
        if (!CollectionUtils.isEmpty(databases)) {
            for (int i = 0; i < databases.size(); i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", databases.get(i).getId());
                map.put("db_name", databases.get(i).getName());
                data.add(map);
            }
        }
        return data;
    }

    // @Cacheable(value = "data-extract-cache", key = "#root.method.name + #p0 + #p1 + #p2 + #p3")
    public List<Map<String, Object>> fetchDbTables(
            ServerConfig.ServerType serverType, Integer serverId, Integer dbId, String dbName) {
        List<Map<String, Object>> tables = new ArrayList<>();
    /*if (ServerConfig.ServerType.MYSQL == serverType) {
      JdbcTemplate jdbcTemplate = createJdbcTemplate(serverType, serverId);
      List<Map<String, Object>> list =
          jdbcTemplate.queryForList(
              "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = '"
                  + dbName
                  + "'");
      List<String> tableNames =newJdbcTemplate(serverType, serverId).queryForList("SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = '"+ dbName+ "'",String.class);
      if (CollectionUtils.isEmpty(list)) {
        return tables;
      }
      for (int i = 0; i < list.size(); i++) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", String.valueOf(i + 1));
        map.put("name", list.get(i).get("TABLE_NAME"));
        tables.add(map);
      }
    }
    if (ServerConfig.ServerType.HIVE == serverType) {
      List<Map<String, Object>> tablesList =
          hadoopMetadataJdbcTemplate.queryForList(
              "SELECT DB_ID,TBL_ID,TBL_NAME FROM "
                  + JDBC.HADOOP_METADATA_DB_NAME
                  + ".TBLS WHERE DB_ID = "
                  + dbId);
      if (CollectionUtils.isEmpty(tablesList)) {
        return tables;
      }
      for (Map<String, Object> tableMap : tablesList) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", tableMap.get("TBL_ID"));
        map.put("name", tableMap.get("TBL_NAME"));
        tables.add(map);
      }
    }*/
        List<MetadataCommonTable> list =
                metadataCommonTableService.listMetadataCommonTables(
                        MetadataType.valueOf(serverType.name()), dbId);
        if (!CollectionUtils.isEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", list.get(i).getId());
                map.put("name", list.get(i).getName());
                tables.add(map);
            }
        }
        return tables;
    }

    // @Cacheable(value = "data-extract-cache", key = "#root.method.name + #p0 + #p1 + #p2 + #p3 +
    // #p4")
    public List<Map<String, Object>> fetchDbTableFields(
            ServerConfig.ServerType serverType, Integer serverId, String dbName, String tableName) {
        List<Map<String, Object>> data = new ArrayList<>();
        if (ServerConfig.ServerType.MYSQL == serverType) {
      /*List<Map<String, Object>> list =
      newJdbcTemplate(serverType, serverId)
          .queryForList(
              "SELECT COLUMN_NAME, COLUMN_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = '"
                  + dbName
                  + "' AND TABLE_NAME = '"
                  + tableName
                  + "'");*/
            JdbcTemplate jdbcTemplate = createJdbcTemplate(serverType, serverId);
            List<Map<String, Object>> list =
                    jdbcTemplate.queryForList(
                            "SELECT COLUMN_NAME, COLUMN_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = '"
                                    + dbName
                                    + "' AND TABLE_NAME = '"
                                    + tableName
                                    + "'");
            if (CollectionUtils.isEmpty(list)) {
                return data;
            }
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", list.get(i).get("COLUMN_NAME"));
                map.put("type", list.get(i).get("COLUMN_TYPE"));
                map.put("load", true);
                map.put("extract", true);
                map.put("cleanse", false);
                data.add(map);
            }
        }
        if (ServerConfig.ServerType.HIVE == serverType) {
            /** 注释掉该段代码的原因是该SQL无法保证字段顺序* */
      /*List<Map<String, Object>> list =
      hadoopDataJdbcTemplate.queryForList(
          " SELECT * FROM( "
              + " (SELECT COLUMN_NAME,TYPE_NAME FROM COLUMNS_V2,SDS,TBLS "
              + " WHERE COLUMNS_V2.CD_ID = SDS.CD_ID "
              + " AND SDS.SD_ID = TBLS.SD_ID "
              + " AND TBLS.TBL_ID = "
              + tableId
              + " ORDER BY INTEGER_IDX) "
              + " UNION ALL "
              + " (SELECT PKEY_NAME,PKEY_TYPE FROM PARTITION_KEYS WHERE TBL_ID = "
              + tableId
              + " ) "
              + " ) t ");*/
            List<Map<String, Object>> fields =
                    hadoopMetadataJdbcTemplate.queryForList(
                            " SELECT COLUMN_NAME,TYPE_NAME FROM COLUMNS_V2,SDS,TBLS "
                                    + " WHERE COLUMNS_V2.CD_ID = SDS.CD_ID "
                                    + " AND SDS.SD_ID = TBLS.SD_ID "
                                    // + " AND TBLS.TBL_ID = " + tableId
                                    + " AND TBLS.TBL_NAME = '"
                                    + tableName
                                    + "' ORDER BY INTEGER_IDX ");
            List<Map<String, Object>> partitions =
                    // hadoopMetadataJdbcTemplate.queryForList(" SELECT PKEY_NAME AS COLUMN_NAME,PKEY_TYPE AS
                    // TYPE_NAME FROM PARTITION_KEYS WHERE TBL_ID = "+ tableId);
                    hadoopMetadataJdbcTemplate.queryForList(
                            " SELECT t1.PKEY_NAME AS COLUMN_NAME,t1.PKEY_TYPE AS TYPE_NAME FROM PARTITION_KEYS t1,TBLS t2 WHERE t1.TBL_ID = t2.TBL_ID AND t2.TBL_NAME = '"
                                    + tableName
                                    + "'");
            List<Map<String, Object>> list = new LinkedList<>();
            list.addAll(fields);
            list.addAll(partitions);
            if (CollectionUtils.isEmpty(list)) {
                return data;
            }
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", list.get(i).get("COLUMN_NAME"));
                map.put("type", list.get(i).get("TYPE_NAME"));
                map.put("load", true);
                map.put("extract", true);
                map.put("cleanse", false);
                data.add(map);
            }
        }
        return data;
    }

    // @CacheEvict(value = "data-extract-cache", allEntries = true)
    public void clearMemoryCache() {
    }

    // @Cacheable(value = "data-extract-cache", key = "#root.method.name + #p0")
    private ServerConfig getServerConfigById(Integer serverId) {
        log.debug("根据serverConfigId=" + serverId + "查询服务器配置信息.");
        return serverConfigRepository.findOne(serverId);
    }

    // @Cacheable(value = "data-extract-cache", key = "#root.method.name + #p0 + #p1")
  /*private JdbcTemplate newJdbcTemplate(ServerConfig.ServerType serverType, Integer serverId) {
    log.debug("创建JDBC连接:" + " serverType= " + serverType + " serverId=" + serverId);
    ServerConfig server = getServerConfigById(serverId);
    Assert.notNull(server, "服务器信息读取失败,serverId=" + serverId);
    return JdbcUtils.createJdbcTemplateByTomcatDatasource(
        server.getHost(),
        server.getPort(),
        null,
        ServerConfig.ServerType.MYSQL.getName(),
        "com.mysql.jdbc.Driver",
        server.getUsername(),
        SecurityUtils.SimpleDecrypt(server.getPassword()));
  }*/

    private JdbcTemplate createJdbcTemplate(ServerConfig.ServerType serverType, Integer serverId) {
        log.debug("创建JDBC连接:" + " serverType= " + serverType + " serverId=" + serverId);
        // ServerConfig server = getServerConfigById(serverId);
        MetadataCommonServer server =
                metadataCommonServerService.getMetadataCommonServerByServerId(serverId);
        Assert.notNull(server, "服务器信息读取失败,serverId=" + serverId);
        String url =
                JdbcTemplateUtils.JdbcUrlBuilder.builder()
                        .dialect(JdbcTemplateUtils.MYSQL_DIALECT)
                        .host(server.getHost())
                        .port(server.getPort())
                        .dbName(null)
                        .build()
                        .toString();
        return JdbcTemplateUtils.createJdbcTemplate(
                url, server.getUsername(), SecurityUtils.simpleDecrypt(server.getPassword()));
    /*return new JdbcUtils.DbConnection(
    "com.mysql.jdbc.Driver",
    url,
    server.getUsername(),
    SecurityUtils.SimpleDecrypt(server.getPassword()));*/
    }
}
