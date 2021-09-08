package com.naixue.nxdp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(
        value = {"classpath:/profiles/${spring.profiles.active}/application-jdbc.properties"})
public class JDBC {

    public static String HADOOP_METADATA_DB_NAME;

    public static String HIVE_JDBC_URL;

    @Value("${spring.datasource.hive-jdbc.url}")
    public void setHIVE_JDBC_URL(String hIVE_JDBC_URL) {
        HIVE_JDBC_URL = hIVE_JDBC_URL;
    }

    @Value("${spring.datasource.hadoop-metadata.db-name}")
    public void setHADOOP_METADATA_DB_NAME(String hADOOP_METADATA_DB_NAME) {
        HADOOP_METADATA_DB_NAME = hADOOP_METADATA_DB_NAME;
    }
}
