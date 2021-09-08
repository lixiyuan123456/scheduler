package com.naixue.spear.util;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import com.google.common.base.Strings;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcTemplateUtils {

    public static final String MYSQL_DIALECT = "mysql";

    public static final String MYSQL_DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";

    public static final String HIVE_DIALECT = "hive2";

    public static final String HIVE_DRIVER_CLASS_NAME = "org.apache.hive.jdbc.HiveDriver";

  /*public static DataSource createTomcatDataSource(
      String driverClassName, String url, String username, String password) {
    return createTomcatDataSource(
        driverClassName,
        url,
        username,
        password,
        10,
        10,
        100,
        100,
        30000,
        60000,
        "select 1",
        30000,
        true,
        5000,
        true,
        false,
        true,
        120);
  }*/

  /*public static DataSource createTomcatDataSource(
      String driverClassName,
      String url,
      String username,
      String password,
      int initialSize,
      int minIdle,
      int maxIdle,
      int maxActive,
      int maxWait,
      int minEvictableIdleTimeMillis,
      String validationQuery,
      long validationInterval,
      boolean testWhileIdle,
      int timeBetweenEvictionRunsMillis,
      boolean testOnBorrow,
      boolean testOnReturn,
      boolean removeAbandoned,
      int removeAbandonedTimeout) {
    org.apache.tomcat.jdbc.pool.DataSource dataSource =
        new org.apache.tomcat.jdbc.pool.DataSource();
    dataSource.setDriverClassName(driverClassName);
    dataSource.setUrl(url);
    dataSource.setUsername(username);
    dataSource.setPassword(password);
    dataSource.setInitialSize(initialSize);
    dataSource.setMinIdle(minIdle);
    dataSource.setMaxIdle(maxIdle);
    dataSource.setMaxActive(maxActive);
    dataSource.setMaxWait(maxWait);
    dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
    dataSource.setValidationQuery(validationQuery);
    dataSource.setValidationInterval(validationInterval);
    dataSource.setTestWhileIdle(testWhileIdle);
    dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
    dataSource.setTestOnBorrow(testOnBorrow);
    dataSource.setTestOnReturn(testOnReturn);
    dataSource.setRemoveAbandoned(removeAbandoned);
    dataSource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
    return dataSource;
  }*/

    public static JdbcTemplate createJdbcTemplate(String url, String username, String password) {
        DriverManagerDataSource ds = new DriverManagerDataSource(url, username, password);
        return new JdbcTemplate(ds);
        /*new JdbcTemplate(DataSourceBuilder.create().url(url).driverClassName(driverClassName).username(username).password(password).build(),true);*/
    /*DataSource dataSource = createTomcatDataSource(driverClassName, url, username, password);
    return new JdbcTemplate(dataSource);*/
    }

    @Deprecated
    public static JdbcTemplate createJdbcTemplate(
            final String host,
            final String port,
            final String dbName,
            final String dbType,
            final String driverClassName,
            final String username,
            final String password) {
        DbType type = null;
        if (!Strings.isNullOrEmpty(dbType)) {
            type = DbType.parseEnumByDialect(dbType);
        }
        if (!Strings.isNullOrEmpty(driverClassName)) {
            type = DbType.parseEnumByDriverClassName(driverClassName);
        }
        return createJdbcTemplate(type, host, port, dbName, username, password);
    }

    public static JdbcTemplate createJdbcTemplate(
            final DbType dbType,
            final String host,
            final String port,
            final String dbName,
            final String username,
            final String password) {
        String url =
                JdbcUrlBuilder.builder()
                        .dialect(dbType.getDialect())
                        .host(host)
                        .port(port)
                        .dbName(dbName)
                        .build()
                        .toString();
        return createJdbcTemplate(dbType.getDriverClassName(), url, username, password);
    }

    public static JdbcTemplate createJdbcTemplate(
            final String driverClassName,
            final String url,
            final String username,
            final String password) {
        try {
            DataSource dataSource =
                    new SimpleDriverDataSource(
                            (Driver) Class.forName(driverClassName).newInstance(), url, username, password);
            return new JdbcTemplate(dataSource);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @RequiredArgsConstructor
    public static enum DbType {
        MYSQL(MYSQL_DIALECT, MYSQL_DRIVER_CLASS_NAME),
        HIVE(HIVE_DIALECT, HIVE_DRIVER_CLASS_NAME);
        @Getter
        private final String dialect;
        @Getter
        private final String driverClassName;

        public static DbType parseEnumByDialect(final String dialect) {
            for (DbType type : DbType.values()) {
                if (type.dialect.equals(dialect)) {
                    return type;
                }
            }
            return null;
        }

        public static DbType parseEnumByDriverClassName(final String driverClassName) {
            for (DbType type : DbType.values()) {
                if (type.driverClassName.equals(driverClassName)) {
                    return type;
                }
            }
            return null;
        }
    }

    @Builder
    @Getter
    public static class JdbcUrlBuilder {
        private final String dialect;
        private final String host;
        private final String port;
        private final String dbName;

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
                    .append(Strings.isNullOrEmpty(getDbName()) ? "" : "/" + getDbName())
                    .append(
                            getDialect().toLowerCase().equals(MYSQL_DIALECT)
                                    ? "?useSSL=false&characterEncoding=utf8&tinyInt1isBit=false&zeroDateTimeBehavior=convertToNull"
                                    : "")
                    .toString();
        }
    }

  /*public static Map<String, Object> queryForMap(DbConnection dbConnection, String sql) {
    List<Map<String, Object>> list = queryForList(dbConnection, sql);
    return CollectionUtils.isEmpty(list) ? null : list.get(0);
  }*/

  /*public static void validationQuery(DbConnection dbConnection) {
    queryForList(dbConnection, "select 1");
  }*/

  /*public static List<Map<String, Object>> queryForList(DbConnection dbConnection, String sql) {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      conn = dbConnection.getConnection();
      ps = conn.prepareStatement(sql);
      rs = ps.executeQuery();
      ResultSetMetaData metaData = rs.getMetaData();
      int columnCount = metaData.getColumnCount();
      List<Map<String, Object>> result = new ArrayList<>();
      while (rs.next()) {
        Map<String, Object> data = new HashMap<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
          data.put(metaData.getColumnName(i), rs.getObject(i));
        }
        result.add(data);
      }
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (ps != null) {
          ps.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }*/

  /*public static int executeUpdate(DbConnection dbConnection, String sql) {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn = dbConnection.getConnection();
      ps = conn.prepareStatement(sql);
      return ps.executeUpdate();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      try {
        if (ps != null) {
          ps.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }*/

  /*public static class DbConnection {

    @Getter private String driverClassName;

    @Getter private String url;

    @Getter private String username;

    @Getter private String password;

    @Getter private Connection connection;

    public DbConnection(String driverClassName, String url, String username, String password) {
      driverClassName =
          StringUtils.isEmpty(driverClassName) ? "com.mysql.jdbc.Driver" : driverClassName;
      this.driverClassName = driverClassName;
      this.url = url;
      this.username = username;
      this.password = password;
      try {
        Class.forName(this.driverClassName);
        this.connection = DriverManager.getConnection(this.url, this.username, this.password);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }*/

    @Getter
    @RequiredArgsConstructor
    public abstract static class SimpleJdbcTemplate {

        private final String driverClassName;

        private final String url;

        private final String username;

        private final String password;

        public abstract void processResultSet(ResultSet resultSet) throws Exception;

        public final void query(final String sql) {
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                connection = createConnection();
                statement = connection.prepareStatement(sql);
                resultSet = statement.executeQuery();
                processResultSet(resultSet);
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
    }
}
