package com.naixue.scheduler.jdbc.support;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import com.naixue.scheduler.Consts;
import com.naixue.scheduler.mapper.JobScheduleMapper;
import com.naixue.scheduler.model.JobSchedule;

public class MybatisManager {

  private SqlSessionFactory sqlSessionFactory;

  private MybatisManager() {
    ResourceBundle applicationBundle =
        ResourceBundle.getBundle(Consts.APPLICATION_PROPERTIES_FILE_NAME);
    // ResourceBundle jdbcBundle = ResourceBundle.getBundle(Consts.JDBC_PROPERTIES_FILE_NAME);
    Properties properties = new Properties();
    try {
      properties.load(
          Thread.currentThread()
              .getContextClassLoader()
              .getResourceAsStream(Consts.JDBC_PROPERTIES_FILE_NAME + ".properties"));
    } catch (IOException e) {
      throw new RuntimeException(e.toString(), e);
    }
    PooledDataSourceFactory dataSourceFactory = new PooledDataSourceFactory();
    dataSourceFactory.setProperties(properties);
    DataSource dataSource = dataSourceFactory.getDataSource();
    /*DataSource dataSource =
    new PooledDataSource(
        jdbcBundle.getString(Consts.JDBC_DATASOURCE_DRIVER_CLASS_NAME),
        jdbcBundle.getString(Consts.JDBC_DATASOURCE_URL),
        jdbcBundle.getString(Consts.JDBC_DATASOURCE_USERNAME),
        jdbcBundle.getString(Consts.JDBC_DATASOURCE_PASSWORD));*/
    Environment environment =
        new Environment(
            applicationBundle.getString(Consts.APPLICATION_MYBATIS_ENVIRONMENT_ID),
            new JdbcTransactionFactory(),
            dataSource);
    Configuration configuration = new Configuration(environment);
    configuration.addMappers(
        applicationBundle.getString(Consts.APPLICATION_MYBATIS_MAPPERS_PACKAGE));
    configuration.setMapUnderscoreToCamelCase(true);
    configuration.setLogImpl(StdOutImpl.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
  }

  private static class SingletonHandler {
    private static MybatisManager singleton = new MybatisManager();
  }

  public static SqlSessionFactory getSqlSessionFactory() {
    return SingletonHandler.singleton.sqlSessionFactory;
  }

  public static void main(String[] args) {
    Properties properties = new Properties();
    try {
      properties.load(
          Thread.currentThread()
              .getContextClassLoader()
              .getResourceAsStream(Consts.JDBC_PROPERTIES_FILE_NAME + ".properties"));
    } catch (IOException e) {
      throw new RuntimeException(e.toString(), e);
    }
    for (Object key : properties.keySet()) {
      System.out.println(properties.get(key));
    }

    SqlSessionFactory sqlSessionFactory = MybatisManager.getSqlSessionFactory();
    SqlSession session = sqlSessionFactory.openSession();
    try {
      JobScheduleMapper mapper = session.getMapper(JobScheduleMapper.class);
      List<JobSchedule> list = mapper.findOnlineJobs();
      System.out.println(list.size());
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
    }
  }
}
