package com.bountyhunter.tomato.datasource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

// @Configuration
// @MapperScan(basePackages = "com.bountyhunter.tomato.dao.mapper")
public class MybatisPrimaryDatasource {

    @Bean(name = "mybatisPrimaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    @Primary
    public DataSource getDatasource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "mybatisPrimarySqlSessionFactory")
    @Primary
    public SqlSessionFactory getSqlSessionFactory(
            @Qualifier("mybatisPrimaryDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "mybatisPrimaryDataSourceTransactionManager")
    @Primary
    public DataSourceTransactionManager getDataSourceTransactionManager(
            @Qualifier("mybatisPrimaryDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "mybatisPrimarySqlSessionTemplate")
    @Primary
    public SqlSessionTemplate getSqlSessionTemplate(
            @Qualifier("mybatisPrimarySqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
