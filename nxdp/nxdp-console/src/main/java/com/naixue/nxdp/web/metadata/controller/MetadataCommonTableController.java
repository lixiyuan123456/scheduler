package com.naixue.nxdp.web.metadata.controller;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.naixue.nxdp.config.CFG;
import org.assertj.core.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.naixue.zzdp.common.util.SecurityUtils;
import com.naixue.zzdp.common.util.ShellUtils;
import com.naixue.zzdp.data.model.metadata.MetadataCommonDatabase;
import com.naixue.zzdp.data.model.metadata.MetadataCommonServer;
import com.naixue.zzdp.data.model.metadata.MetadataCommonTable;
import com.naixue.zzdp.data.util.JdbcTemplateUtils;
import com.naixue.zzdp.platform.service.metadata.MetadataCommonTableService;
import com.naixue.nxdp.web.BaseController;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/metadata/common/table")
public class MetadataCommonTableController extends BaseController {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final int KEY_UNIT = 16;
    @Autowired
    private MetadataCommonTableService metadataCommonTableService;
    @Autowired
    private Executor taskExecutor;

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

    @RequestMapping("/list.do")
    public Object listMetadataCommonServers(DataTableRequest<MetadataCommonTable> dataTable) {
        if (dataTable.getStart() == 0 && dataTable.getLength() == 0) {
            List<MetadataCommonTable> list = metadataCommonTableService.listMetadataCommonTables();
            return success(list);
        }
        MetadataCommonTable condition = new MetadataCommonTable();
        if (!Strings.isNullOrEmpty(dataTable.getCondition())) {
            condition = JSON.parseObject(dataTable.getCondition(), MetadataCommonTable.class);
        }
        condition.setSearch(
                dataTable.getSearch() != null
                        ? dataTable.getSearch().get(DataTableRequest.Search.value.toString())
                        : null);
        Page<MetadataCommonTable> page =
                metadataCommonTableService.listMetadataCommonTables(
                        condition,
                        dataTable.getStart() / dataTable.getLength(),
                        dataTable.getLength(),
                        new Sort.Order(Sort.Direction.DESC, "id"));
        return new DataTableResponse<MetadataCommonTable>(
                dataTable.getStart(),
                dataTable.getLength(),
                dataTable.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    @RequestMapping("/matchByTableName.do")
    public Object matchTablesByTableName(String tableName) {
        List<MetadataCommonTable> list =
                metadataCommonTableService.listMetadataCommonTablesByTableNameWithFullName(tableName);
        return success(list);
    }

    @RequestMapping("/matchByTableIds.do")
    public Object matchTablesByTableIds(@RequestParam("tableIds[]") List<Integer> tableIds) {
        List<MetadataCommonTable> list =
                metadataCommonTableService.listMetadataCommonTablesByTableIdsWithFullName(tableIds);
        return success(list);
    }

    @RequestMapping("/sync.do")
    public Object syncUpdateTablesByDatabaseId(Integer databaseId) {
        metadataCommonTableService.syncUpdateTablesByDatabaseId(databaseId);
        return success();
    }

    @RequestMapping("/mirror.do")
    public Object mirrorMysqlTablesInHive(
            @RequestParam(value = "tableIds[]") List<Integer> tableIds) {
        List<MetadataCommonTable> tables =
                metadataCommonTableService.fillMetadataCommonTables(tableIds);
        for (MetadataCommonTable table : tables) {
            taskExecutor.execute(new HiveTableCreater(metadataCommonTableService, table));
        }
        return success();
    }

    private static class HiveTableCreater implements Runnable {

        private static final String SECRET_KEY = "N9430hEfgvFlU3xJtH3CcA==";

        // private MetadataCommonServer server;

        // private MetadataCommonDatabase database;

        private MetadataCommonTableService metadataCommonTableService;

        private MetadataCommonTable table;

        private String salt;

        public HiveTableCreater(
                final MetadataCommonTableService metadataCommonTableService,
                final MetadataCommonTable table) {
            this.metadataCommonTableService = metadataCommonTableService;
            this.table = table;
            this.salt = table.getName() + SECRET_KEY;
        }

        @Override
        public void run() {
            MetadataCommonServer server = table.getServer();
            MetadataCommonDatabase database = table.getDatabase();
            JdbcTemplate jdbcTemplate =
                    JdbcTemplateUtils.createJdbcTemplate(
                            JdbcTemplateUtils.DbType.MYSQL,
                            server.getHost(),
                            server.getPort(),
                            database.getName(),
                            server.getUsername(),
                            SecurityUtils.simpleDecrypt(server.getPassword()));
            List<Map<String, Object>> fieldMaps =
                    jdbcTemplate.queryForList(
                            "SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT, COLUMN_KEY FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = '"
                                    + database.getName()
                                    + "' AND TABLE_NAME = '"
                                    + table.getName()
                                    + "'");
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
                    JdbcTemplateUtils.JdbcUrlBuilder.builder()
                            .dialect(JdbcTemplateUtils.MYSQL_DIALECT)
                            .host(server.getHost())
                            .port(server.getPort())
                            .dbName(database.getName())
                            .build()
                            .toString()
                            + "\n"
                            + server.getUsername()
                            + "\n"
                            + SecurityUtils.simpleDecrypt(server.getPassword());
            String targetJdbcByAES = encrypt4Hive(jdbc, salt);
            Table tableDto = new Table(database.getName(), table.getName(), fields, targetJdbcByAES);
            String createTableSql = generateCreateHiveTableSql(tableDto);
            try {
                ShellUtils.exec("hive", "-e", createTableSql);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            table.setMirroredStatus(MetadataCommonTable.MirroredStatus.MIRRORED);
            metadataCommonTableService.saveMetadataCommonTable(table);
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
