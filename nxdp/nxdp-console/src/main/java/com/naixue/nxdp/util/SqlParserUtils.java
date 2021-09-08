package com.naixue.nxdp.util;

import java.util.LinkedList;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.dialect.hive.stmt.HiveCreateTableStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.util.JdbcConstants;

import lombok.AllArgsConstructor;
import lombok.Data;

public class SqlParserUtils {

    public static List<Column> parseColumnsFromCreateTableSQL(String sql, String dialect) {
        Assert.hasText(sql, "建表SQL不允许为空");
        Assert.hasText(dialect, "数据库方言类型不允许为空");
        // 这个API只支持MYSQL不支持HIVE
        // SQLCreateTableStatement createTableStatement = SQLParserUtils.createSQLStatementParser(sql,
        // dialect).parseCreateTable();
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(sql, dialect);
        List<Column> columns = new LinkedList<>();
        if (CollectionUtils.isEmpty(sqlStatements)) {
            return columns;
        }
        List<SQLObject> elements = sqlStatements.get(0).getChildren();
        for (SQLObject element : elements) {
            if (element instanceof SQLColumnDefinition) {
                SQLColumnDefinition columnDefinition = (SQLColumnDefinition) element;
                String name = columnDefinition.computeAlias();
                String alias =
                        columnDefinition.getComment() == null
                                ? ""
                                : columnDefinition.getComment().toString().replaceAll("'", "");
                String type = columnDefinition.getDataType().getName();
                int length =
                        CollectionUtils.isEmpty(columnDefinition.getDataType().getArguments())
                                ? 0
                                : Integer.parseInt(columnDefinition.getDataType().getArguments().get(0).toString());
                boolean nullable = !columnDefinition.containsNotNullConstaint();
                columns.add(new Column(name, alias, type, length, nullable));
            }
        }
        return columns;
    }

    public static void main(String[] args) {
        String dbType = JdbcConstants.MYSQL;
        HiveCreateTableStatement hiveCreateTableStatement =
                (HiveCreateTableStatement)
                        SQLUtils.parseStatements(
                                "CREATE TABLE hdp_zhuanzhuan_tmp_algo.zhai_chuan_cheng_inc_1d(\r\n"
                                        + "`id` string COMMENT 'ID'\r\n"
                                        + ")COMMENT '翟传成测试'\r\n"
                                        + "PARTITIONED BY (`dt` string COMMENT '日期'\r\n"
                                        + ")\r\n",
                                JdbcConstants.HIVE)
                                .get(0);
        for (SQLObject child : hiveCreateTableStatement.getChildren()) {
            if (child instanceof SQLColumnDefinition) {
                SQLColumnDefinition column = (SQLColumnDefinition) child;
                System.out.println(
                        column.computeAlias()
                                + "   "
                                + column.getComment()
                                + "   "
                                + column.getDataType().getName()
                                + "   "
                                + column.containsNotNullConstaint());
            }
        }
        SQLCreateTableStatement statement =
                SQLParserUtils.createSQLStatementParser(
                        "CREATE TABLE `t_quota` (\r\n"
                                + "  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '指标ID',\r\n"
                                + "  `name` varchar(200) NOT NULL DEFAULT '' COMMENT '指标名称',\r\n"
                                + "  `description` varchar(500) NOT NULL DEFAULT '' COMMENT '指标描述',\r\n"
                                + "  `rule` text NOT NULL COMMENT '指标统计规则',\r\n"
                                + "  `developer_id` varchar(100) NOT NULL DEFAULT '' COMMENT '开发人员ID',\r\n"
                                + "  `developer_name` varchar(100) NOT NULL DEFAULT '' COMMENT '开发人员',\r\n"
                                + "  `type` tinyint(3) NOT NULL DEFAULT '0' COMMENT '指标类型',\r\n"
                                + "  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\r\n"
                                + "  `modify_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\r\n"
                                + "  `server_id` tinyint(3) NOT NULL DEFAULT '0' COMMENT '服务器',\r\n"
                                + "  `db_id` int(11) NOT NULL DEFAULT '0' COMMENT '数据库ID',\r\n"
                                + "  `table_id` int(11) NOT NULL DEFAULT '0' COMMENT '表ID',\r\n"
                                + "  `table_name` varchar(200) NOT NULL DEFAULT '' COMMENT '表名',\r\n"
                                + "  `column_id` int(11) NOT NULL DEFAULT '0' COMMENT 't_common_column_info主键ID',\r\n"
                                + "  `operator_id` varchar(100) NOT NULL DEFAULT '' COMMENT '操作人微信ID',\r\n"
                                + "  PRIMARY KEY (`id`)\r\n"
                                + ") ENGINE=InnoDB AUTO_INCREMENT=3401 DEFAULT CHARSET=utf8mb4;\r\n"
                                + "",
                        dbType)
                        .parseCreateTable();
        System.out.println(statement.getSchema());
        System.out.println(statement.getAttributes());
        System.out.println(statement.getBeforeCommentsDirect());
        System.out.println(statement.getChildren());
        List<SQLObject> children = statement.getChildren();
        for (SQLObject child : children) {
            if (child instanceof SQLColumnDefinition) {
                SQLColumnDefinition column = (SQLColumnDefinition) child;
                System.out.println(
                        column.computeAlias()
                                + "   "
                                + column.getComment()
                                + "   "
                                + column.getDataType().getName()
                                + "   "
                                + (column.getDataType().getArguments() == null
                                ? 0
                                : column.getDataType().getArguments().get(0))
                                + "   "
                                + column.containsNotNullConstaint());
            }
        }
    /*SchemaStatVisitor schemaStatVisitor = SQLUtils.createSchemaStatVisitor(dbType);
    statement.accept(schemaStatVisitor);*/
        // System.out.println(sqlStatement.getTableOptions());
        // System.out.println(sqlStatement.getHints());
        // System.out.println(sqlStatement.getOptionHints());
        // System.out.println(schemaStatVisitor.getTables());
        // System.out.println(schemaStatVisitor.getColumns());
        /*Collection<Column> columns = schemaStatVisitor.getColumns();*/
    /*for (Column column : columns) {
      System.out.println(
          column.getName()
              + "   "
              + column.getDataType()
              + "   "
              + column.isPrimaryKey()
              + "   "
              + column.getAttributes());
    }*/
    }

    @Data
    @AllArgsConstructor
    public static class Column {
        private String name;

        private String alias;

        private String type;

        private int length;

        private boolean nullable;

        // private boolean partitionable;
    }
}
