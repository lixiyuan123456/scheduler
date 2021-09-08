package com.naixue.chord.jdbc;

import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import com.naixue.chord.mapper.MyMapper;

public class MybatisManager {

  private volatile SqlSessionFactory sqlSessionFactory;

  private MybatisManager() {
    ResourceBundle bundle = ResourceBundle.getBundle("jdbc");
    Iterator<String> iterator = bundle.keySet().iterator();
    while (iterator.hasNext()) {
      System.out.println(bundle.getString(iterator.next()));
    }
    DataSource dataSource =
        new PooledDataSource(
            "com.mysql.jdbc.Driver",
            "jdbc:mysql://192.168.187.245:3306/skynet?useSSL=false&useUnicode=true&characterEncoding=utf-8",
            "root",
            "root");
    Environment environment = new Environment("primary", new JdbcTransactionFactory(), dataSource);
    Configuration configuration = new Configuration(environment);
    configuration.addMappers("com.zhuanzhuan.chord.mapper");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
  }

  public static SqlSessionFactory getSqlSessionFactory() {
    return new MybatisManager().sqlSessionFactory;
  }

  public static void main(String[] args) {
    SqlSessionFactory sqlSessionFactory = MybatisManager.getSqlSessionFactory();
    SqlSession session = sqlSessionFactory.openSession();
    MyMapper mapper = session.getMapper(MyMapper.class);
    List<Object> list = mapper.list();
    System.out.println(list.size());
  }
}
