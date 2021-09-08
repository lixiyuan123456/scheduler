package com.naixue.nxdp.attachment.goshawk;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {"classpath:/profiles/${spring.profiles.active}/goshawk.properties"})
public class GoshawkCfg {

    public static String YARN_QUEUE_RESOURCE_TOTAL_QUERY_URL;

    public static String KAFKA_CLUSTER_HOSTS;

    public static Short KAFKA_TOPIC_REPLICATION_FACTOR;

    @Value("${yarn_queue_resource_total_query_url}")
    public void setYARN_QUEUE_RESOURCE_TOTAL_QUERY_URL(String yARN_QUEUE_RESOURCE_TOTAL_QUERY_URL) {
        YARN_QUEUE_RESOURCE_TOTAL_QUERY_URL = yARN_QUEUE_RESOURCE_TOTAL_QUERY_URL;
    }

    @Value("${kafka_cluster_hosts}")
    public void setKAFKA_CLUSTER_HOSTS(String kAFKA_CLUSTER_HOSTS) {
        KAFKA_CLUSTER_HOSTS = kAFKA_CLUSTER_HOSTS;
    }

    @Value("${kafka_topic_replication_factor}")
    public void setKAFKA_TOPIC_REPLICATION_FACTOR(Short kAFKA_TOPIC_REPLICATION_FACTOR) {
        KAFKA_TOPIC_REPLICATION_FACTOR = kAFKA_TOPIC_REPLICATION_FACTOR;
    }
}
