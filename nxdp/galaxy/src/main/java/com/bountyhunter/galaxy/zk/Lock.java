package com.bountyhunter.galaxy.zk;

import com.bountyhunter.galaxy.plugin.ZkClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;

public class Lock {

    public static void main(String[] args) {
        CuratorFramework zkClient = ZkClient.newClient("10.126.98.197:2181");
        zkClient.start();
        for (int i = 0; i < 1; i++) {
            new Thread(
                    new Runnable() {

                        @Override
                        public void run() {
                            try {
                                Stat stat = zkClient.checkExists().forPath("/zcchome");
                                System.out.println(stat);
                                if (stat == null) {
                                    zkClient.create().forPath("/zcchome", "123".getBytes());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .start();
        }
    }
}
