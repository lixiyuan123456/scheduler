package com.bountyhunter.galaxy.plugin;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetAddress;
import java.util.List;

public class ZkClient {

    private static final String ZK_HOST = "10.126.98.197:2181";

    public static void main(String[] args) throws Exception {
        String host = InetAddress.getLocalHost().getHostAddress();
        System.out.println(host);
        CuratorFramework client = newClient(ZK_HOST);
        client.start();
        List<String> list = client.getChildren().forPath("/");
        for (String s : list) {
            System.out.println(s);
        }
        client.close();
    }

    public static CuratorFramework newClient(final String connectionString) {
        return CuratorFrameworkFactory.newClient(
                connectionString, new ExponentialBackoffRetry(1000, 3));
    }

    public static CuratorFramework newClient(
            final String connectionString,
            final RetryPolicy retryPolicy,
            final int connectionTimeoutMs,
            final int sessionTimeoutMs) {
        return CuratorFrameworkFactory.builder()
                .connectString(connectionString)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .build();
    }

    public static void destroyClient(CuratorFramework client) {
        client.close();
    }

    public static List<String> watchedGetChildren(CuratorFramework client, String path)
            throws Exception {
        return client.getChildren().watched().forPath(path);
    }
}
