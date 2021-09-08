package com.naixue.nxdp.attachment.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;

public class KafkaUtils {

    public static void createTopic(
            String hosts, String topicName, int numPartitions, short replicationFactor) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, hosts);
        AdminClient adminClient = KafkaAdminClient.create(props);
        NewTopic topic = new NewTopic(topicName, numPartitions, replicationFactor);
        Map<String, KafkaFuture<Void>> resultMap =
                adminClient.createTopics(Arrays.asList(topic)).values();
        try {
            resultMap.get(topicName).get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        adminClient.close(20, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "10.126.88.226:9092");
        KafkaUtils.createTopic("10.126.88.226:9092", "zhaichuancheng", 5, (short) 3);
    }
}
