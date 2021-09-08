package com.naixue.dp.ha;

import com.naixue.dp.util.Configuration;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.EnsurePath;
import org.apache.log4j.Logger;
import org.apache.zookeeper.data.Stat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by sunzhiwei on 2018/1/26.
 */
public class ActiveStandbyElector {
    private static Logger logger = Logger.getLogger(ActiveStandbyElector.class);

    private static final String ZK_HOSTS = Configuration.getConfiguration().get("ZK_HOSTS");
    private static final String ZK_SESSION_TIMEOUT = Configuration.getConfiguration().get("ZK_SESSION_TIMEOUT");
    private static final String ZK_CONNECTION_TIMEOUT = Configuration.getConfiguration().get("ZK_CONNECTION_TIMEOUT");
    private static final String SCHEDULER_NAMESPACE = Configuration.getConfiguration().get("SCHEDULER_NAMESPACE");
    private static final String SCHEDULER_HA_PATH = Configuration.getConfiguration().get("SCHEDULER_HA_PATH");
    private static final String EXECUTOR_HA_PATH = "/executor";

    private static final String ACTIVE = "active";
    private static final String STANDBY = "standby";

    private String hostname = null;
    private CuratorFramework zkClinet = null;
    private Map<String, Map<String, String>> hostStatus = new HashMap<String, Map<String, String>>();

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public ActiveStandbyElector(){
        zkClinet = CuratorFrameworkFactory.builder().connectString(ZK_HOSTS).namespace(SCHEDULER_NAMESPACE)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)).sessionTimeoutMs(Integer.parseInt(ZK_SESSION_TIMEOUT))
                .connectionTimeoutMs(Integer.parseInt(ZK_CONNECTION_TIMEOUT)).build();
        zkClinet.start();
        // create hostname path
        try {
//            hostname = "testHost";
            hostname = InetAddress.getLocalHost().getHostName();
            EnsurePath ensurePath = zkClinet.newNamespaceAwareEnsurePath(SCHEDULER_HA_PATH + EXECUTOR_HA_PATH
                    + "/" + hostname);
            ensurePath.ensure(zkClinet.getZookeeperClient());
//            String path = new String((byte[])zkClinet.getData().forPath(SCHEDULER_HA_PATH + hostname));
//            System.out.println("dfs "+ path);
        } catch (UnknownHostException e) {
            logger.error("get hostname error...", e);
        } catch (Exception e) {
            logger.error("create zk path error...", e);
        }


//            zkClinet.create().forPath("/szw/s", new byte[0]);
    }

    private boolean getHaHostData(){
        boolean result = true;
        this.hostStatus.clear();
        try {
            List<String> hostList = zkClinet.getChildren().forPath(SCHEDULER_HA_PATH + EXECUTOR_HA_PATH);
            for (String host : hostList){
                if (!host.contains("lock")){
                    String data = new String(zkClinet.getData().forPath(SCHEDULER_HA_PATH + EXECUTOR_HA_PATH + "/" + host));
                    logger.info("The data of " + host + " is " + data);
                    if (data != "" || data.length() > 0){
                        String[] array = data.split("#");
                        if (array.length != 2){
                            logger.info(host + " status is invalid, the data is " + data);
                            this.deleteHostData(host);
                        }else {
                            Map<String, String> status = new HashMap<String, String>();
                            status.put("STATUS", array[0]);
                            status.put("TIMESTAMP", array[1]);
                            hostStatus.put(host, status);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("get host data error : ", e);
            result = false;
        }
        return result;
    }

    private void deleteHostData(String hostname){
        Stat stat = null;
        try {
            stat = zkClinet.checkExists().forPath(this.SCHEDULER_HA_PATH + EXECUTOR_HA_PATH + "/" + hostname);
            if (stat != null){
                zkClinet.delete().forPath(this.SCHEDULER_HA_PATH + EXECUTOR_HA_PATH + "/" + hostname);
            }
        } catch (Exception e) {
            logger.error("delete host error : ", e);
        }
    }

    public void updateHostData(String host, boolean isActive){
        String data = "";
        Date now = new Date();
        if (isActive){
            data = this.ACTIVE + "#" + now.getTime();
        }else {
            data = this.STANDBY + "#" + now.getTime();
        }
        Stat stat = null;
        try {
            stat = zkClinet.checkExists().forPath(SCHEDULER_HA_PATH + EXECUTOR_HA_PATH + "/" + host);
            if (stat == null){
                logger.info("create a new host : " + host);
                zkClinet.create().forPath(SCHEDULER_HA_PATH + EXECUTOR_HA_PATH + "/" + host, data.getBytes());
            } else {
                logger.info("update data of host : " + host);
                zkClinet.setData().forPath(SCHEDULER_HA_PATH + EXECUTOR_HA_PATH + "/" + host, data.getBytes());
            }
        } catch (Exception e) {
            logger.error("update host data error...", e);
        }

    }

    /**
     * 根据另一个节点的状态更新hostname的状态
     * @param hostname
     * @param notSelf 是否排除当前节点
     * @return hostname的状态的更新值
     */
    public boolean updateLocalStatus(String hostname, boolean notSelf){
        boolean isActive = true;
        for (String hostStr : hostStatus.keySet()){
            // notSelf为true时只判断另一个节点的状态
            if (notSelf && hostStr.equals(hostname)){
                continue;
            }
            Map<String, String> anotherMap = hostStatus.get(hostStr);
            String anotherStatus = anotherMap.get("STATUS");
            String anotherTimestamp = anotherMap.get("TIMESTAMP");
            if (anotherStatus.equals(this.ACTIVE)) {
                if (new Date().getTime() - Long.valueOf(anotherTimestamp) >= 300000) {
                    logger.warn("The localStatus of " + hostStr + " expired, so delete it, and change " + hostname + " to active");
                    this.deleteHostData(hostname);
                }else {
                    logger.info("The localStatus of " + hostStr + " is active, so change " + hostname + " to standby");
                    // 另一节点必须为standby,则直接更新状态
                    isActive = false;
                }
            }else if (anotherStatus.equals(this.STANDBY)){
                // 另一节点必须为active
                logger.info("The localStatus of " + hostStr + " is standby, so change " + hostname + " to active");

            }else {
                logger.warn("The localStatus of " + hostStr + " is error, so delete it, and change " + hostname + " to active");
                deleteHostData(hostStr);
            }
        }
        return isActive;
    }

    public boolean judgeUpdateActive(String hostname){
        boolean isActive = true;
        if (hostStatus.containsKey(hostname)){
            Map<String, String> map = hostStatus.get(hostname);
//            String localStatus = map.get("STATUS");
//            String localStamp = map.get("TIMESTAMP");
            isActive = this.updateLocalStatus(hostname, true);
//          //当前节点为active则其它节点必须为standby
//            if (localStatus.equals(this.ACTIVE)){
//                for (String hostStr : hostStatus.keySet()){
//                    // 这里只判断另一个节点的状态
//                    if (hostStr.equals(hostname)){
//                        continue;
//                    }
//                    Map<String, String> anotherMap = hostStatus.get(hostStr);
//                    String anotherStatus = anotherMap.get("STATUS");
//                    String anotherTimestamp = anotherMap.get("TIMESTAMP");
//                    if (anotherStatus.equals(this.ACTIVE)) {
//                        if (new Date().getTime() - Long.valueOf(anotherTimestamp) >= 300000) {
//                            logger.warn("The localStatus of " + hostStr + " expired, so delete it, and change " + hostname + " to active");
//                            this.deleteHostData(hostname);
//                        }else {
//                            logger.info("The localStatus of " + hostStr + " is active, so change " + hostname + " to standby");
//                            // 另一节点必须为standby,则直接更新状态
//                            isActive = false;
//                        }
//                    }else if (anotherStatus.equals(this.STANDBY)){
//                        // 另一节点必须为active
//                        logger.info("The localStatus of " + hostStr + " is standby, so change " + hostname + " to active");
//
//                    }else {
//                        logger.warn("The localStatus of " + hostStr + " is error, so delete it, and change " + hostname + " to active");
//                        deleteHostData(hostStr);
//                    }
//                }
//            }else if (localStatus.equals("standby")){
//                // 另一节点必须为active
//                for (String hostStr : hostStatus.keySet()){
//                    if (hostStr.equals(hostname)){
//                        continue;
//                    }
//                    Map<String, String> anotherMap = hostStatus.get(hostStr);
//                    String anotherStatus = anotherMap.get("STATUS");
//                    String anotherTimestamp = anotherMap.get("TIMESTAMP");
//                    if (anotherStatus.equals(this.ACTIVE)) {
//                        if (new Date().getTime() - Long.valueOf(anotherTimestamp) >= 300000) {
//                            logger.warn("The localStatus of " + hostStr + " expired, so delete it, and change " + hostname + " to active");
//                            this.deleteHostData(hostname);
//                        }else {
//                            logger.info("The localStatus of " + hostStr + " is active, so change " + hostname + " to standby");
//                            // 另一节点必须为standby,则直接更新状态
//                            isActive = false;
//                        }
//                    }else if (anotherStatus.equals(this.STANDBY)){
//                        // 另一节点必须为active
//                        logger.info("The localStatus of " + hostStr + " is standby, so change " + hostname + " to active");
//
//                    }else {
//                        logger.warn("The localStatus of " + hostStr + " is error, so delete it, and change " + hostname + " to active");
//                        deleteHostData(hostStr);
//                    }
//                }
//            }// 如果只判断另一个节点的状态，是不是localStauts的状态就无所谓了？这是重复代码呀
        }else if (hostStatus.size() == 0){
            // 当前节点变为active
            logger.info("There is no host status in zk, so update " + hostname + "to active...");
        }else {
            logger.info("hostname - " + hostname + " not contains in hostStatus...");
            // 判断另一个节点的状态然后更新自己的状态
            isActive = this.updateLocalStatus(hostname, false);
        }
        updateHostData(hostname, isActive);
        return isActive;
    }
    public boolean isActive(){
        boolean isActive = false;
        // 分布式重入锁
        InterProcessMutex nodeLock = new InterProcessMutex(this.zkClinet, SCHEDULER_HA_PATH + EXECUTOR_HA_PATH);
        try {
            if (nodeLock.acquire(20L, TimeUnit.SECONDS)) {
                logger.info("start get host data...");
                if (this.getHaHostData()) {
                    logger.info("get data of host, start to judge active");
                    isActive = judgeUpdateActive(hostname);
                }
            }
        } catch (Exception e) {
            logger.error("get lock error : ", e);
        } finally {
            try {
                nodeLock.release();
            } catch (Exception e) {
                logger.error(" release lock error : ", e);
            }
        }


        return isActive;
    }

    public static void main(String[] args){
        ActiveStandbyElector activeStandbyElector = new ActiveStandbyElector();
        System.out.println(activeStandbyElector.isActive());

    }
}
