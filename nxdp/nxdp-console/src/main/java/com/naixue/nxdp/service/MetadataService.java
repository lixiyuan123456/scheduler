package com.naixue.nxdp.service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.Column;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.naixue.zzdp.common.util.SecurityUtils;
import com.naixue.zzdp.common.util.ShellUtils;
import com.naixue.nxdp.config.CFG;
import com.naixue.nxdp.dao.MetadataDbTableRepository;
import com.naixue.nxdp.dao.ServerConfigRepository;
import com.naixue.zzdp.data.util.JdbcTemplateUtils;
import com.naixue.nxdp.model.ServerConfig;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.model.metadata.MetadataDbTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MetadataService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final int KEY_UNIT = 16;
    @Autowired
    private ServerConfigRepository serverConfigRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private MetadataDbTableRepository metadataDbTableRepository;

    private static JdbcTemplate createJdbcTemplate(ServerConfig server, String dbName) {
        String url = generateJdbc(server, dbName);
        return JdbcTemplateUtils.createJdbcTemplate(
                url, server.getUsername(), SecurityUtils.simpleDecrypt(server.getPassword()));
    }

    private static String generateJdbc(ServerConfig server, String dbName) {
        return JdbcTemplateUtils.JdbcUrlBuilder.builder()
                .dialect(JdbcTemplateUtils.MYSQL_DIALECT)
                .host(server.getHost())
                .port(server.getPort())
                .dbName(dbName)
                .build()
                .toString();
    }

  /*private static JdbcUtils.DbConnection newMysqlDbConnection(ServerConfig server, String dbName) {
    return new JdbcUtils.DbConnection(
        "com.mysql.jdbc.Driver",
        generateJdbc(server, dbName),
        server.getUsername(),
        SecurityUtils.SimpleDecrypt(server.getPassword()));
  }*/

    /**
     * 加密
     *
     * @param src
     * @param salt
     * @return
     */
    private static String encrypt4Hive(String src, String salt) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(
                    Cipher.ENCRYPT_MODE, new SecretKeySpec(reduceKey(salt.getBytes(), KEY_UNIT), ALGORITHM));
            byte[] bytes = cipher.doFinal(src.getBytes(Charset.defaultCharset()));
            return Base64Utils.encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解密
     *
     * @param src
     * @param salt
     * @return
     */
    private static String decrypt4Hive(String src, String salt) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(
                    Cipher.DECRYPT_MODE, new SecretKeySpec(reduceKey(salt.getBytes(), KEY_UNIT), ALGORITHM));
            byte[] encryptedBytes = Base64Utils.decodeFromString(src);
            byte[] bytes = cipher.doFinal(encryptedBytes);
            return new String(bytes, Charset.defaultCharset());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] reduceKey(byte[] src, int unit) {
        int length = Math.min(src.length, unit);
        byte[] output = new byte[length];
        for (int i = 0; i < length; i++) {
            output[i] = src[i];
        }
        return output;
    }

    private static String generateCreateHiveTableSql(Table table) {
        StringBuilder sb = new StringBuilder();
        sb.append(" set role admin; ");
        sb.append(" add jar ")
                .append(CFG.HDFS_CLUSTER_URL_PREFIX + "/skynet")
                .append("/udf/jdbc2hive.jar; ");
        sb.append(" DROP TABLE IF EXISTS ").append(table.getTargetFullTableName()).append(";");
        sb.append(" CREATE EXTERNAL TABLE IF NOT EXISTS ").append(table.toString());
        sb.append(" ROW FORMAT SERDE ");
        sb.append(" 'com.anjuke.hive.storage.jdbc.JdbcSerDe' ");
        sb.append(" STORED BY ");
        sb.append(" 'com.anjuke.hive.storage.jdbc.JdbcStorageHandler' ");
        sb.append(" WITH SERDEPROPERTIES ('serialization.format'='1') ");
        sb.append(" TBLPROPERTIES ");
        sb.append(" ( ");
        sb.append(" 'COLUMN_STATS_ACCURATE'='false', ");
        sb.append(" 'jdbc2hive.jdbc.class'='com.mysql.jdbc.Driver', ");
        Assert.hasText(table.getTargetJdbcByAES(), "创建hive表所需的加密jdbc串不允许为空");
        sb.append(" 'jdbc2hive.jdbc.config'='").append(table.getTargetJdbcByAES()).append("', ");
        if (!StringUtils.isEmpty(table.getPrimaryKey())) {
            sb.append(" 'jdbc2hive.splited.by'='").append(table.getPrimaryKey()).append("', ");
        }
        Assert.hasText(table.getSourceTableName(), "创建hive表所需的mysql表名不允许为空");
        sb.append(" 'jdbc2hive.table.name'='").append(table.getSourceTableName()).append("', ");
        sb.append(" 'numFiles'='0', ");
        sb.append(" 'numRows'='-1', ");
        sb.append(" 'rawDataSize'='-1', ");
        sb.append(" 'totalSize'='0' ");
        sb.append(" ) ");
        String hiveSql = sb.toString();
        log.info("创建hive表语句=" + hiveSql);
        return hiveSql;
    }

    public static void main(String[] args) {
        String SECRET_KEY = "N9430hEfgvFlU3xJtH3CcA==";
        String s =
                "62RRUR+eGA0d1qvIt7zl/iZrqYMXIcxrDsm/zszZIH6Oo3jBnrjInL2Ik+j5RTBj0FsMhFZ0IHPZi9vW/AXCdy1Hob2mj8PM2/tv0Qwt3syWiFfjDN2GM1cX/Amb1mZTD+x70Zg4D7NcxlxL9CbH7Yx/ldLdvYJsaaYxIJfOcDragsMq/xEuQGhAWcZamPwuoc8T4rItt7PbIc3HKsPpq3Gg+kBCAXunkZIdlljWK9MJ9a9/yQmRJKdW7fNjK060";
        String string = decrypt4Hive(s, "t_info_stock_1" + SECRET_KEY);
        System.out.println(string);
        String t = encrypt4Hive(string, "t_info_stock_1" + SECRET_KEY);
        System.out.println(t.equals(s));
    }

    public DbMappingTreeDTO mapping() {
        DbMappingTreeDTO vo = new DbMappingTreeDTO();
        // 初始化父节点
        vo.setId("-1");
        vo.setText("服务器列表");
        vo.setState(new State(true, false));
        List<DbMappingTreeDTO> children = new ArrayList<>();
        vo.setChildren(children);
        List<ServerConfig> servers = serverConfigRepository.findAll();
        if (CollectionUtils.isEmpty(servers)) {
            return vo;
        }
        for (ServerConfig server : servers) {
            DbMappingTreeDTO serverMapping = new DbMappingTreeDTO();
            serverMapping.setId(server.getId() + "");
            serverMapping.setText(server.getHost());
            serverMapping.setState(new State());
            children.add(serverMapping);
        }
        return vo;
    }

    public List<ServerConfigDTO> listServers() {
        List<ServerConfig> servers = serverConfigRepository.findAll();
        if (CollectionUtils.isEmpty(servers)) {
            return new ArrayList<>();
        }
        Map<String, User> userMap = userService.findAllInMap();
        List<ServerConfigDTO> list = new ArrayList<>();
        for (ServerConfig s : servers) {
            ServerConfigDTO v = new ServerConfigDTO();
            BeanUtils.copyProperties(s, v);
            v.setCreatorName(((User) userMap.get(s.getCreatorId())).getName());
            v.setLastModifierName(((User) userMap.get(s.getLastModifierId())).getName());
            list.add(v);
        }
        return list;
    }

    public List<MetadataDbTable> listMetadataDbTables(
            Integer serverId, String dbName, String tableKw) {
        List<MetadataDbTable> result = new LinkedList<>();
        // Assert.notNull(serverId, "请求参数serverId不允许为空");
        // Assert.hasText(dbName, "请求参数dbName不允许为空");
        if (serverId == null || StringUtils.isEmpty(dbName)) {
            return result;
        }
        ServerConfig server = serverConfigRepository.findOne(serverId);
        Assert.notNull(server, "服务器数据不存在serverId=" + serverId);
        if (ServerConfig.ServerType.MYSQL.getId() != server.getServerType()) {
            throw new RuntimeException("暂时只支持mysql数据库");
        }
        String sql =
                " SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = '" + dbName + "' ";
        sql += StringUtils.isEmpty(tableKw) ? "" : " AND TABLE_NAME LIKE '%" + tableKw + "%' ";
        JdbcTemplate jdbcTemplate = createJdbcTemplate(server, dbName);
        List<Map<String, Object>> tableNameList = jdbcTemplate.queryForList(sql);
        // JdbcUtils.queryForList(newMysqlDbConnection(server, dbName), sql);
        if (!CollectionUtils.isEmpty(tableNameList)) {
            List<MetadataDbTable> loadedTables =
                    metadataDbTableRepository.findByServerIdAndDbName(serverId, dbName);
            Map<String, MetadataDbTable> mapping = new HashMap<>(loadedTables.size());
            if (!CollectionUtils.isEmpty(loadedTables)) {
                for (MetadataDbTable loadedTable : loadedTables) {
                    mapping.put(loadedTable.getTableName(), loadedTable);
                }
            }
            for (Map<String, Object> tableNameMap : tableNameList) {
                MetadataDbTable table = new MetadataDbTable();
                table.setServerId(serverId);
                table.setDbName(dbName);
                table.setTableName((String) tableNameMap.get("TABLE_NAME"));
                if (mapping.containsKey((String) tableNameMap.get("TABLE_NAME"))) {
                    table.setStatus(MetadataDbTable.Status.LOADED);
                }
                result.add(table);
            }
        }
        return result;
    }

    public List<Map<String, Object>> listMetadataDbs(Integer serverId) {
        Assert.notNull(serverId, "请求参数serverId不允许为空");
        ServerConfig server = serverConfigRepository.findOne(serverId);
        Assert.notNull(server, "服务器数据不存在serverId=" + serverId);
        JdbcTemplate jdbcTemplate = createJdbcTemplate(server, null);
        return jdbcTemplate.queryForList(
                "SELECT SCHEMA_NAME,SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA ORDER BY SCHEMA_NAME");
    /*return JdbcUtils.queryForList(
    newMysqlDbConnection(server, null),
    "SELECT SCHEMA_NAME,SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA ORDER BY SCHEMA_NAME");*/
    }

    /**
     * 导入元数据
     *
     * @param serverId
     * @param dbName
     * @param tableNames
     */
    @Transactional
    public void importMetadata(Integer serverId, String dbName, String[] tableNames) {
        Assert.notNull(serverId, "请求参数serverId不允许为空");
        Assert.hasText(dbName, "请求参数dbName不允许为空");
        Assert.notEmpty(tableNames, "请求参数tableNames不允许为空");
        ServerConfig server = serverConfigRepository.findOne(serverId);
        Assert.notNull(server, "服务器数据不存在serverId=" + serverId);
        List<MetadataDbTable> data = new ArrayList<>();
        for (String tableName : tableNames) {
            MetadataDbTable metadataDbTable = new MetadataDbTable();
            metadataDbTable.setServerId(serverId);
            metadataDbTable.setDbName(dbName);
            metadataDbTable.setTableName(tableName);
            metadataDbTable.setModifyTime(new Date());
            data.add(metadataDbTable);
            new Thread(new HiveTableCreater(server, dbName, tableName)).start();
        }
        metadataDbTableRepository.save(data);
    }

    @Data
    private static class DbMappingTreeDTO {

        private String id;

        private String text;

        private State state;

        private List<DbMappingTreeDTO> children;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class State {

        private boolean opened = false;

        private boolean selected = false;
    }

    @Data
    public static class ServerConfigDTO {

        private ServerConfig server;

        private Integer id;

        private String name;

        private String host;

        private String port;

        @JsonProperty("user_name")
        private String username;

        @JsonProperty("pass_word")
        private String password;

        @JsonProperty("creator_id")
        private String creatorId;

        @JsonProperty("created_by")
        private String creatorName;

        @JsonProperty("server_type")
        private Integer serverType;

        @JsonProperty("logic_type")
        private Integer logicType;

        private Integer isBinlogSupported;

        @Column(name = "create_time")
        private Date createTime;

        @JsonProperty("update_time")
        private Date modifyTime;

        @JsonProperty("last_modifier_id")
        private String lastModifierId;

        @JsonProperty("updated_by")
        private String lastModifierName;
    }

    private static class HiveTableCreater implements Runnable {

        private static final String SECRET_KEY = "N9430hEfgvFlU3xJtH3CcA==";

        private ServerConfig server;

        private String dbName;

        private String tableName;

        private String salt;

        public HiveTableCreater(ServerConfig server, String dbName, String tableName) {
            this.server = server;
            this.dbName = dbName;
            this.tableName = tableName;
            this.salt = this.tableName + SECRET_KEY;
        }

        @Override
        public void run() {
            JdbcTemplate jdbcTemplate = createJdbcTemplate(server, dbName);
            List<Map<String, Object>> fieldMaps =
                    jdbcTemplate.queryForList(
                            "SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT, COLUMN_KEY FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = '"
                                    + dbName
                                    + "' AND TABLE_NAME = '"
                                    + tableName
                                    + "'");
      /*JdbcUtils.queryForList(
      newMysqlDbConnection(server, dbName),
      "SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT, COLUMN_KEY FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = '"
          + dbName
          + "' AND TABLE_NAME = '"
          + tableName
          + "'");*/
            if (CollectionUtils.isEmpty(fieldMaps)) {
                return;
            }
            List<Field> fields = new ArrayList<>();
            for (Map<String, Object> fieldMap : fieldMaps) {
                Field field =
                        new Field(
                                (String) fieldMap.get("COLUMN_NAME"),
                                (String) fieldMap.get("COLUMN_TYPE"),
                                (String) fieldMap.get("COLUMN_COMMENT"),
                                (String) fieldMap.get("COLUMN_KEY"));
                fields.add(field);
            }
            String jdbc =
                    generateJdbc(server, dbName)
                            + "\n"
                            + server.getUsername()
                            + "\n"
                            + SecurityUtils.simpleDecrypt(server.getPassword());
            String targetJdbcByAES = encrypt4Hive(jdbc, salt);
            Table table = new MetadataService.Table(dbName, tableName, fields, targetJdbcByAES);
            String createTableSql = generateCreateHiveTableSql(table);
      /*File tempFile;
      try {
        tempFile = File.createTempFile("temp-", ".sql");
        log.info("临时文件" + tempFile.getAbsolutePath());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      try (FileWriter fileWriter = new FileWriter(tempFile); ) {
        fileWriter.write(createTableSql);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }*/
            try {
                // ShellUtils.exec("hive", "-f", tempFile.getAbsolutePath());
                ShellUtils.exec("hive", "-e", createTableSql);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // 如果不加这句shell命令执行到中途就会直接退出
            try {
                Thread.sleep(60000L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Data
    public static class Table {

        private String sourceDbName;

        private String sourceTableName;

        private String targetDbName;

        private String targetTableName;

        private List<Field> fields;

        private String targetJdbcByAES;

        private String targetFullTableName;

        public Table(
                String sourceDbName, String sourceTableName, List<Field> fields, String targetJdbcByAES) {
            this.sourceDbName = sourceDbName;
            this.sourceTableName = sourceTableName;
            this.fields = fields;
            this.targetJdbcByAES = targetJdbcByAES;
        }

        public String getTargetDbName() {
            targetDbName = getSourceDbName();
            return targetDbName;
        }

        public String getTargetTableName() {
            targetTableName = getSourceTableName();
            return targetTableName;
        }

        public String getTargetFullTableName() {
            targetFullTableName =
                    "`" + "mysql_db." + getTargetDbName() + "_" + getTargetTableName() + "`";
            return targetFullTableName;
        }

        public String getPrimaryKey() {
            List<Field> fields = getFields();
            if (CollectionUtils.isEmpty(fields)) {
                return null;
            }
            for (Field field : fields) {
                if ("PRI".equals(field.getSourceKey())) {
                    return field.getSourceName();
                }
            }
            return null;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
      /*sb.append("`")
      .append("mysql_db.")
      .append(getTargetDbName())
      .append("_")
      .append(getTargetTableName())
      .append("`");*/
            sb.append(getTargetFullTableName());
            sb.append("(");
            if (!CollectionUtils.isEmpty(fields)) {
                for (int i = 0; i < fields.size(); i++) {
                    Field field = fields.get(i);
                    sb.append("`")
                            .append(field.getTargetName())
                            .append("`")
                            .append(" ")
                            .append(field.getTargetType())
                            .append(" ")
                            .append("COMMENT")
                            .append(" ")
                            .append("'")
                            .append(field.getTargetComment())
                            .append("'");
                    if (i < fields.size() - 1) {
                        sb.append(",");
                    }
                }
            }
            sb.append(")");
            log.info(sb.toString());
            return sb.toString();
        }
    }

    @Data
    public static class Field {

        private String sourceName;

        private String sourceType;

        private String sourceComment;

        private String sourceKey;

        private String targetName;

        private String targetType;

        private String targetComment;

        public Field(String sourceName, String sourceType, String sourceComment, String sourceKey) {
            this.sourceName = sourceName;
            this.sourceType = sourceType;
            this.sourceComment = sourceComment;
            this.sourceKey = sourceKey;
        }

        public String getTargetName() {
            targetName = getSourceName();
            return targetName;
        }

        public String getTargetType() {
            String type = getSourceType();
            if (StringUtils.isEmpty(type)) {
                targetType = type;
                return targetType;
            }
            if (type.contains("(")) {
                type = type.substring(0, type.indexOf("("));
            }
            switch (type.toLowerCase()) {
                case "integer":
                    targetType = "int";
                    break;
                case "text":
                    targetType = "string";
                    break;
                case "tinytext":
                    targetType = "string";
                    break;
                case "longtext":
                    targetType = "string";
                    break;
                case "blob":
                    targetType = "binary";
                    break;
                case "tinyblob":
                    targetType = "binary";
                    break;
                case "datetime":
                    targetType = "string";
                    break;
                case "timestamp":
                    targetType = "string";
                    break;
                case "time":
                    targetType = "string";
                    break;
                case "year":
                    targetType = "string";
                    break;
                case "varchar":
                    targetType = "string";
                    break;
                default:
                    targetType = type;
                    break;
            }
            return targetType;
        }

        public String getTargetComment() {
            targetComment = getSourceComment();
            return targetComment;
        }
    }
}
