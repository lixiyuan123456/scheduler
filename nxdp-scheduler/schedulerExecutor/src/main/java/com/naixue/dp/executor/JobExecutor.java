package com.naixue.dp.executor;

import com.naixue.dp.util.Configuration;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.EnsurePath;
import org.apache.log4j.Logger;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by sunzhiwei on 2018/1/31.
 */
public class JobExecutor {
    private static Logger logger = Logger.getLogger(JobExecutor.class);

    private static final String ZK_HOSTS = Configuration.getConfiguration().get("ZK_HOSTS");
    private static final String ZK_SESSION_TIMEOUT = Configuration.getConfiguration().get("ZK_SESSION_TIMEOUT");
    private static final String ZK_CONNECTION_TIMEOUT = Configuration.getConfiguration().get("ZK_CONNECTION_TIMEOUT");
    private static final String SCHEDULER_NAMESPACE = Configuration.getConfiguration().get("SCHEDULER_NAMESPACE");
    private static final String SCHEDULER_PATH = Configuration.getConfiguration().get("SCHEDULER_PATH");

    private int jobId;
    private String command;
    private String namespace;
    private String queryName;
    private CuratorFramework zkClinet = null;

    public JobExecutor(){};
    public JobExecutor(int jobId, String command, String namespace, String executorId) {
        this.jobId = jobId;
        this.command = command;
        this.namespace = namespace;
        this.queryName = executorId;
        init();
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public Process executor() {
        logger.info(jobId + " start execute...");
        Process process = null;
        Runtime runtime = Runtime.getRuntime();
        try {
            process = runtime.exec(this.command);
        } catch (IOException e) {
            logger.error(jobId + " start execute...", e);
        }
        return process;
    }

    public int processResult(Process process, String jobType){
        int result = -1;
        LogCollection logProcessInfo = new LogCollection(process.getErrorStream(), "_process_info", this.queryName);
        LogCollection logResultInfo = new LogCollection(process.getInputStream(), "_result_info", this.queryName);
        logProcessInfo.start();
        logResultInfo.start();
        try {
            if (jobType.toUpperCase().contains("SPARK_STREAMING")){
                logger.info("jobId: " + this.jobId + " is " + jobType + ", will kill terminal command after 1min...");
                Thread.sleep(60000);
                process.destroy(); // 并没有把spark streaming
                logger.debug("jobId: " + this.jobId + " is " + jobType + ", kill success");
            }
            logProcessInfo.join();
            logResultInfo.join();
            logger.debug("jobId: " + this.jobId + " is " + jobType + ", join end");
            result = process.waitFor();
            logger.debug("jobId: " + this.jobId + " is " + jobType + ", waitFor is " + result);
//            check下result的返回值？
//            是否需要为spark_streaming增加额外的返回状态
        } catch (InterruptedException e) {
            logger.error("processResult queryName - " + this.queryName + " fail ", e);
        }
        return result;
    }

    private void initZK() throws Exception {
        zkClinet = CuratorFrameworkFactory.builder().connectString(ZK_HOSTS).namespace(SCHEDULER_NAMESPACE)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)).sessionTimeoutMs(Integer.parseInt(ZK_SESSION_TIMEOUT))
                .connectionTimeoutMs(Integer.parseInt(ZK_CONNECTION_TIMEOUT)).build();
        logger.debug("JobExecutor zk connecting...");
        zkClinet.start();
        // 连接数过多，造成 KeeperErrorCode = ConnectionLoss  得不到新的连接
        try {
            EnsurePath ensurePath = zkClinet.newNamespaceAwareEnsurePath(SCHEDULER_PATH);
            ensurePath.ensure(zkClinet.getZookeeperClient());
        } catch (Exception e) {
            logger.error("init zk path error...", e);
            throw new Exception(e.getMessage());
        }
    }

    private void closeZk(){
        logger.debug("JobExecutor zk connection close...");
        zkClinet.close();
    }

    private boolean delZKNodeWithLock(CuratorFramework zkClinet, String path, String node){
        InterProcessMutex lock = new InterProcessMutex(zkClinet, path);
        boolean result = false;
        for (int i=0; i<5; i++) {
            if (result == true){
                break;
            }
            try {
                if (lock.acquire(15L, TimeUnit.SECONDS)) {
                    zkClinet.delete().forPath(path + "/" + node);
                    result = true;
                }
            } catch (Exception e) {
                result = false;
                logger.error("get lock or delete fail - " , e);
            } finally {
                try {
                    lock.release();
                } catch (Exception e) {
                    logger.error("release lock fail - " , e);
                }
            }
        }
        return result;
    }

    // 容易引起readyPath的item删不掉，然后循环创建线程去执行该executeId
    private void init() {
        logger.info("start run job - " + this.jobId);
        String readyPath = Configuration.getConfiguration().get("SCHEDULER_READY_PATH") + "_" + this.namespace;
        String runningPath = Configuration.getConfiguration().get("SCHEDULER_RUNNING_PATH") + "_" + this.namespace;
        try {
            initZK();
            for (int i = 0; i < 5; i++) {
                Stat stat = null;
                try {
                    stat = zkClinet.checkExists().forPath(runningPath);
                    if (stat == null) {
                        logger.info(runningPath + " not exist, create it ..");
                        zkClinet.create().forPath(runningPath);
                    }
                } catch (Exception e) {
                    logger.error("check " + runningPath + " fail - ", e);
                    continue;
                }
                // 删除readyPath之后create runningPath出错了怎么办？
                // 先create runningpath 然后再删除readypath
                try {
                    stat = zkClinet.checkExists().forPath(runningPath + "/" + this.queryName);
                    if (stat != null) {
                        delZKNodeWithLock(zkClinet, runningPath, this.queryName);
                    }
                    // 将jobId写入node中？
                    zkClinet.create().forPath(runningPath + "/" + this.getQueryName(), namespace.getBytes());
                } catch (Exception e) {
                    logger.error("create runningpath ", e);
                }
                try {
                    stat = zkClinet.checkExists().forPath(readyPath + "/" + this.queryName);
                    if (stat == null) {
                        logger.error("jobId - " + this.jobId + ", executorId - " + this.queryName + " not exist in " + readyPath + ", exit out.");
                        break;
                    }
                } catch (Exception e) {
                    logger.error("check " + readyPath + "/" + this.queryName + " fail - " , e);
                    continue;
                }
                if (stat != null) {
                    delZKNodeWithLock(zkClinet, readyPath, this.queryName);
                    logger.info("delete " + readyPath + "/" + this.queryName + ", start put it in runningPath");
                }
                // 正常break
                break;
            }
        } catch (Exception e) {
            logger.error("jobExecutor init zk error ", e);
        } finally {
            closeZk();
        }
    }

    public static void main(String[] args){
        JobExecutor jobExecutor = new JobExecutor();
        for (int i = 0; i < 160; i ++) {
            try {
                jobExecutor.initZK();
            } catch (Exception e) {
                System.out.println("fsfs" + e.getMessage());
            }
        }
        for (int i = 0; i < 10; i++){
            try {
                jobExecutor.initZK();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
