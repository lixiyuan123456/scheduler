package com.naixue.spear.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import com.google.common.base.Strings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlSessionTemplate implements SqlSession {

    public static final String MAPPERS_PACKAGE_NAME = "com.zhuanzhuan";
    public static final String LOG_IMPL = "";
    private static final String ENVIRONMENT_ID = "environment";
    private final SqlSessionFactory sqlSessionFactory;

    private final SqlSession sqlSessionProxy;

    private SqlSessionTemplate(SqlSessionFactory sessionFactory) {
        this.sqlSessionFactory = sessionFactory;
        this.sqlSessionProxy =
                (SqlSession)
                        Proxy.newProxyInstance(
                                SqlSessionFactory.class.getClassLoader(),
                                new Class[]{SqlSession.class},
                                new SqlSessionInterceptor());
    }

    public static SqlSessionTemplate getInstance() {
        return Singleton.singleton;
    }

    private SqlSessionFactory getSqlSessionFactory() {
        return this.sqlSessionFactory;
    }

    @Override
    public <T> T selectOne(String statement) {
        return this.sqlSessionProxy.selectOne(statement);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        return this.sqlSessionProxy.selectOne(statement, parameter);
    }

    @Override
    public <E> List<E> selectList(String statement) {
        return this.sqlSessionProxy.selectList(statement);
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        return this.sqlSessionProxy.selectList(statement, parameter);
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        return this.sqlSessionProxy.selectList(statement, parameter, rowBounds);
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
        return this.sqlSessionProxy.selectMap(statement, mapKey);
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
        return this.sqlSessionProxy.selectMap(statement, parameter, mapKey);
    }

    @Override
    public <K, V> Map<K, V> selectMap(
            String statement, Object parameter, String mapKey, RowBounds rowBounds) {
        return this.sqlSessionProxy.selectMap(statement, parameter, mapKey, rowBounds);
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement) {
        return this.sqlSessionProxy.selectCursor(statement);
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter) {
        return this.sqlSessionProxy.selectCursor(statement, parameter);
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds) {
        return this.sqlSessionProxy.selectCursor(statement, parameter, rowBounds);
    }

    @Override
    public void select(String statement, Object parameter, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, parameter, handler);
    }

    @Override
    public void select(String statement, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, handler);
    }

    @Override
    public void select(
            String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, parameter, rowBounds, handler);
    }

    @Override
    public int insert(String statement) {
        return this.sqlSessionProxy.insert(statement);
    }

    @Override
    public int insert(String statement, Object parameter) {
        return this.sqlSessionProxy.insert(statement, parameter);
    }

    @Override
    public int update(String statement) {
        return this.sqlSessionProxy.update(statement);
    }

    @Override
    public int update(String statement, Object parameter) {
        return this.sqlSessionProxy.update(statement, parameter);
    }

    @Override
    public int delete(String statement) {
        return this.sqlSessionProxy.delete(statement);
    }

    @Override
    public int delete(String statement, Object parameter) {
        return this.sqlSessionProxy.delete(statement, parameter);
    }

    @Override
    public void commit() {
        this.sqlSessionProxy.commit();
    }

    @Override
    public void commit(boolean force) {
        this.sqlSessionProxy.commit(force);
    }

    @Override
    public void rollback() {
        this.sqlSessionProxy.rollback();
    }

    @Override
    public void rollback(boolean force) {
        this.sqlSessionProxy.rollback(force);
    }

    @Override
    public List<BatchResult> flushStatements() {
        return this.sqlSessionProxy.flushStatements();
    }

    @Override
    public void close() {
        this.sqlSessionProxy.close();
    }

    @Override
    public void clearCache() {
        this.sqlSessionProxy.clearCache();
    }

    @Override
    public Configuration getConfiguration() {
        return this.sqlSessionFactory.getConfiguration();
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return getConfiguration().getMapper(type, this);
    }

    @Override
    public Connection getConnection() {
        return this.sqlSessionProxy.getConnection();
    }

    private static class Singleton {

        private static SqlSessionTemplate singleton = new SqlSessionTemplate(buildSqlSessionFactory());

        private static SqlSessionFactory buildSqlSessionFactory() {
            Environment environment =
                    new Environment.Builder(ENVIRONMENT_ID)
                            .transactionFactory(new JdbcTransactionFactory())
                            .dataSource(
                                    new UnpooledDataSource(
                                            "com.mysql.cj.jdbc.Driver",
                                            "jdbc:mysql://10.148.15.12:3306/zzdp?useSSL=false&useUnicode=true&characterEncoding=utf-8",
                                            "root",
                                            "UDP@mj505"))
                            .build();
            Configuration config = new Configuration(environment);
            if (Strings.isNullOrEmpty(MAPPERS_PACKAGE_NAME)) {
                log.error("mybatis mappers package has not configured yet");
                throw new RuntimeException("mybatis mappers package has not configured yet");
            }
            config.addMappers(MAPPERS_PACKAGE_NAME);
            config.setMapUnderscoreToCamelCase(true);
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(config);
            log.info("sql session factory is created.");
            return factory;
        }
    }

    private static class SqlSessionInterceptor implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            SqlSession sqlSession = null;
            try {
                sqlSession = getInstance().getSqlSessionFactory().openSession();
                return method.invoke(sqlSession, args);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                if (sqlSession != null) {
                    sqlSession.close();
                }
            }
        }
    }
}
