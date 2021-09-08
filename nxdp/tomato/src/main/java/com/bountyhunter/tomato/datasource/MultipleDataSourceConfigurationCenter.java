package com.bountyhunter.tomato.datasource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@ConfigurationProperties("application")
public class MultipleDataSourceConfigurationCenter {

    @Primary
    @Bean("primaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource createPrimaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean("viceDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.vice")
    public DataSource createViceDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean("primaryJdbcTemplate")
    public JdbcTemplate createPrimaryJdbcTemplate(
            @Qualifier("primaryDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean("viceJdbcTemplate")
    public JdbcTemplate createViceJdbcTemplate(@Qualifier("viceDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
