package com.naixue.dp.common.util;

import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.ql.QueryState;
import org.apache.hadoop.hive.ql.parse.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HiveSQLParser {

    public static final String TABLE_NAME = "name";

    public static final String TABLE_COMMENT = "comment";

    public static final String TABLE_FIELDS = "fields";

    public static final String TABLE_PARTITIONS = "partitions";

    public static final String TABLE_LOCATION = "location";

    public static final String TABLE_FILE_FORMAT = "fileFormat";

    public static Map<String, Object> parseSQL(final String sql) throws Exception {
        Map<String, Object> table = new TreeMap<>();
        ASTNode tree = (ASTNode) ParseUtils.parse(sql);
        for (int i = 0; i < tree.getChildCount(); ++i) {
            ASTNode node = (ASTNode) tree.getChild(i);
            QueryState queryState = new QueryState.Builder().build();
            switch (node.getType()) {
                case HiveParser.TOK_TABNAME:
                    String tableName = getTableName(node);
                    table.put(TABLE_NAME, tableName);
                    break;
                case HiveParser.TOK_TABLECOMMENT:
                    String comment = BaseSemanticAnalyzer.stripQuotes(node.getChild(0).getText());
                    table.put(TABLE_COMMENT, comment);
                    break;
                case HiveParser.TOK_TABCOLLIST:
                    List<FieldSchema> fields =
                            DDLSemanticAnalyzer.getColumns(node, true, queryState.getConf());
                    table.put(TABLE_FIELDS, fields);
                    break;
                case HiveParser.TOK_TABLEPARTCOLS:
                    // HashMap<String, String> partSpec = DDLSemanticAnalyzer.getPartSpec(node);
                    List<FieldSchema> partitions = new LinkedList<>();
                    for (int j = 0; j < node.getChildCount(); j++) {
                        FieldSchema partition = new FieldSchema();
                        ASTNode partNode = (ASTNode) node.getChild(j);
                        partition.setName(
                                BaseSemanticAnalyzer.unescapeIdentifier(partNode.getChild(0).getText()));
                        partition.setType(DDLSemanticAnalyzer.getTypeName((ASTNode) partNode.getChild(1)));
                        partition.setComment(BaseSemanticAnalyzer.stripQuotes(partNode.getChild(2).getText()));
                        partitions.add(partition);
                    }
                    table.put(TABLE_PARTITIONS, partitions);
                    break;
                case HiveParser.TOK_TABLEROWFORMAT:
                    break;
                case HiveParser.TOK_TABLELOCATION:
                    table.put(TABLE_LOCATION, BaseSemanticAnalyzer.stripQuotes(node.getChild(0).getText()));
                    break;
                case HiveParser.TOK_FILEFORMAT_GENERIC:
                    table.put(TABLE_FILE_FORMAT, node.getChild(0).getText());
                    break;
                default:
                    break;
            }
        }
        return table;
    }

    private static String getTableName(final ASTNode TOK_TABNAME_Node) {
        return DDLSemanticAnalyzer.getUnescapedUnqualifiedTableName(TOK_TABNAME_Node);
    }

    public static void main(String[] args) throws Exception {
        String sql =
                "CREATE TABLE hdp_zhuanzhuan_tmp_global.tmp_test_full_1d(\n"
                        + "`token` string COMMENT '1111',\n"
                        + "`uid` bigint COMMENT '用户id',\n"
                        + "`timestamp` bigint COMMENT '时间戳(秒)',\n"
                        + "`action` string COMMENT '行为类型',\n"
                        + "`cmd` string COMMENT '调用接口服务名称',\n"
                        + "`terminal` string COMMENT '终端类型',\n"
                        + "`version` string COMMENT 'app版本号',\n"
                        + "`info_id` bigint COMMENT '商品id',\n"
                        + "`cate_first_id` string COMMENT '一级分类id',\n"
                        + "`cate_second_id` string COMMENT '二级分类id',\n"
                        + "`cate_third_id` string COMMENT '三级分类id',\n"
                        + "`channel_id` string COMMENT '渠道id',\n"
                        + "`referer_from` string COMMENT 'referer来源',\n"
                        + "`lon` string COMMENT '经度',\n"
                        + "`lat` string COMMENT '纬度',\n"
                        + "`ip` string COMMENT '用户ip',\n"
                        + "`datapool` map<string,string> COMMENT '扩展内容'\n"
                        + ")COMMENT '后端日志离线用户行为序列表'\n"
                        + "PARTITIONED BY (`dt` string COMMENT '分区，yyyy-MM-dd',\n"
                        + "`region` string COMMENT '分区，action首字母小写，首字符为非字母则统一放在other分区中'\n"
                        + ")\n"
                        + "stored as parquet\n"
                        + "location 'hdfs://zzhadoop/home/zdp/warehouse/hdp_zhuanzhuan_tmp_global/tmp_test_full_1d'";
        HiveSQLParser.parseSQL(sql);
    }
}
