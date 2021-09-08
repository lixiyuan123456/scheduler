package com.naixue.nxdp.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {"classpath:/profiles/${spring.profiles.active}/zk.properties"})
public class ZKConfig {

    public static String ZK_HOSTS;

    public static String ZK_NAME_SPACE;

    public static int ZK_CONNECTION_TIMEOUT_MS;

    public static int ZK_SESSION_TIMEOUT_MS;

    @Value("${zk.hosts}")
    public void setZK_HOSTS(String zK_HOSTS) {
        ZK_HOSTS = zK_HOSTS;
    }

    @Value("${zk.name.space}")
    public void setZK_NAME_SPACE(String zK_NAME_SPACE) {
        ZK_NAME_SPACE = zK_NAME_SPACE;
    }

    @Value("${zk.connection.timeout.ms}")
    public void setZK_CONNECTION_TIMEOUT_MS(int zK_CONNECTION_TIMEOUT_MS) {
        ZK_CONNECTION_TIMEOUT_MS = zK_CONNECTION_TIMEOUT_MS;
    }

    @Value("${zk.session.timeout.ms}")
    public void setZK_SESSION_TIMEOUT_MS(int zK_SESSION_TIMEOUT_MS) {
        ZK_SESSION_TIMEOUT_MS = zK_SESSION_TIMEOUT_MS;
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework newClient() {
        return CuratorFrameworkFactory.builder()
                .connectString(ZK_HOSTS)
                .namespace(ZK_NAME_SPACE)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .connectionTimeoutMs(ZK_CONNECTION_TIMEOUT_MS)
                .sessionTimeoutMs(ZK_SESSION_TIMEOUT_MS)
                .build();
    }
}
