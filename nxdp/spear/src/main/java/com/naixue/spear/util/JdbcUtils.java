package com.naixue.spear.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.google.common.base.Strings;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public class JdbcUtils {

    public static final String MYSQL_DIALECT = "mysql";

    public static final String MYSQL_DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";

    public static final String HIVE_DIALECT = "hive";

    public static final String HIVE2_DIALECT = "hive2";

    public static final String HIVE_DRIVER_CLASS_NAME = "org.apache.hive.jdbc.HiveDriver";

    private String dialect; // 数据库方言
    private String driverClassName; // 数据库驱动
    private String url;
    private String urlSuffix; // 数据库连接后缀
    private String host;
    private String port;
    private String databaseName;
    private String username;
    private String password;

    public static void main(String[] args) {
    }

    public JdbcUtils build() {
        if (Strings.isNullOrEmpty(this.url)) {
            String url =
                    new UrlBuilder.UrlBuilderBuilder()
                            .dialect(this.dialect)
                            .host(this.host)
                            .port(this.port)
                            .databaseName(this.databaseName)
                            .suffix(this.urlSuffix)
                            .build()
                            .toString();
            this.url = url;
        }
        return this;
    }

    public void executeQuery(ResultSetProcessor processor, final String sql) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = createConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            processor.processResultSet(resultSet);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                    log.debug("ResultSet is colsed now.");
                }
                if (statement != null) {
                    statement.close();
                    log.debug("Statement is colsed now.");
                }
                if (connection != null) {
                    connection.close();
                    log.debug("Connection is colsed now.");
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private Connection createConnection() throws Exception {
        Class.forName(this.driverClassName);
        return DriverManager.getConnection(this.url, this.username, this.password);
    }

    public static interface ResultSetProcessor {

        void processResultSet(ResultSet resultSet) throws Exception;
    }

    @Builder
    @Getter
    public static class UrlBuilder {
        private String dialect; // 数据库方言
        private String host;
        private String port;
        private String databaseName;
        private String suffix; // 数据库连接后缀

        @Override
        public String toString() {
            return new StringBuilder("jdbc")
                    .append(":")
                    .append(getDialect())
                    .append(":")
                    .append("//")
                    .append(getHost())
                    .append(":")
                    .append(getPort())
                    .append(Strings.isNullOrEmpty(getDatabaseName()) ? "" : "/" + getDatabaseName())
                    .append(Strings.isNullOrEmpty(getSuffix()) ? "" : getSuffix())
                    .toString();
        }
    }
}
