package com.naixue.nxdp.datasource;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@PropertySource(
        value = {"classpath:/profiles/${spring.profiles.active}/application-jdbc.properties"})
public class DataSourceConfig {

    @Bean("primaryTomcatPoolConfiguration")
    @ConfigurationProperties("spring.datasource.primary.tomcat")
    public org.apache.tomcat.jdbc.pool.PoolConfiguration poolConfiguration() {
        return new org.apache.tomcat.jdbc.pool.PoolProperties();
    }

    @Primary
    @Bean("primaryDataSource")
    @ConfigurationProperties("spring.datasource.primary")
    public DataSource createPrimaryDataSource(
            @Qualifier("primaryTomcatPoolConfiguration")
                    org.apache.tomcat.jdbc.pool.PoolConfiguration poolConfiguration) {
        return new org.apache.tomcat.jdbc.pool.DataSource(poolConfiguration);
    }

    @Bean("primaryJdbcTemplate")
    public JdbcTemplate createPrimaryJdbcTemplate(
            @Qualifier("primaryDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean("hadoopMetadataDataSource")
    @ConfigurationProperties("spring.datasource.hadoop-metadata")
    public DataSource createHadoopMetadataDataSource() {
        return (DriverManagerDataSource)
                DataSourceBuilder.create().type(DriverManagerDataSource.class).build();
    }

    @Bean("hadoopMetadataJdbcTemplate")
    public JdbcTemplate createHadoopMetadataJdbcTemplate(
            @Qualifier("hadoopMetadataDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean("kafkaMonitorDataSource")
    @ConfigurationProperties("spring.datasource.kafka-monitor")
    public DataSource createKafkaMonitorDataSource() {
        return DataSourceBuilder.create().type(DriverManagerDataSource.class).build();
    }

    @Bean("kafkaMonitorJdbcTemplate")
    public JdbcTemplate createKafkaMonitorJdbcTemplate(
            @Qualifier("kafkaMonitorDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean("hueDataSource")
    @ConfigurationProperties("spring.datasource.hue")
    public DataSource createHueDataSource() {
        return DataSourceBuilder.create().type(DriverManagerDataSource.class).build();
    }

    @Bean("hueJdbcTemplate")
    public JdbcTemplate createHueJdbcTemplate(@Qualifier("hueDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
