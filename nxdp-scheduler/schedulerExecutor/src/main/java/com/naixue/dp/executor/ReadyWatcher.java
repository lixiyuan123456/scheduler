package com.naixue.dp.executor;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.EnsurePath;
import org.apache.log4j.Logger;
import org.apache.zookeeper.data.Stat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.naixue.dp.ha.ActiveStandbyElector;
import com.naixue.dp.monitor.WXHelper;
import com.naixue.dp.util.CheckRM;
import com.naixue.dp.util.Configuration;
import com.naixue.dp.util.DbUtil;
import com.naixue.dp.util.ToolUtil;
import com.naixue.dp.util.ZkUtil;

/** Created by sunzhiwei on 2018/1/26. */
public class ReadyWatcher implements Runnable {
  private static Logger logger = Logger.getLogger(ReadyWatcher.class);

  private static final String ZK_HOSTS = Configuration.getConfiguration().get("ZK_HOSTS");
  private static final String ZK_SESSION_TIMEOUT =
      Configuration.getConfiguration().get("ZK_SESSION_TIMEOUT");
  private static final String ZK_CONNECTION_TIMEOUT =
      Configuration.getConfiguration().get("ZK_CONNECTION_TIMEOUT");
  private static final String SCHEDULER_NAMESPACE =
      Configuration.getConfiguration().get("SCHEDULER_NAMESPACE");
  private static final String SCHEDULER_PATH =
      Configuration.getConfiguration().get("SCHEDULER_PATH");
  private static final int SCHEDULER_RUNNING_SIZE =
      Integer.parseInt(Configuration.getConfiguration().get("SCHEDULER_RUNNING_SIZE"));
  private static final String CMD_INTERPRETER =
      Configuration.getConfiguration().get("CMD_INTERPRETER");
  private static final String INTERPRETER_SCRIPT =
      Configuration.getConfiguration().get("INTERPRETER_SCRIPT");

  private static final String EMAIL_LIST = Configuration.getConfiguration().get("EMAIL_LIST");
  private static final String PHONE_LIST = Configuration.getConfiguration().get("PHONE_LIST");

  // 为什么用static
  private DbUtil dbUtil = null;
  private CuratorFramework zkClient = null;
  private ActiveStandbyElector activeStandbyElector = null;

  private void init() {
    logger.debug("ExecutorWatcher init...");
    connectDb();
    // retryPolicy参数是指在连接ZK服务过程中重新连接测策略.在它的实现类ExponentialBackoffRetry(int baseSleepTimeMs, int
    // maxRetries)中,baseSleepTimeMs参数代表两次连接的等待时间,maxRetries参数表示最大的尝试连接次数
    zkClient =
        CuratorFrameworkFactory.builder()
            .connectString(ZK_HOSTS)
            .namespace(SCHEDULER_NAMESPACE)
            .retryPolicy(new ExponentialBackoffRetry(1000, 3))
            .sessionTimeoutMs(Integer.parseInt(ZK_SESSION_TIMEOUT))
            .connectionTimeoutMs(Integer.parseInt(ZK_CONNECTION_TIMEOUT))
            .build();
    logger.debug("ReadyWatcher zk connecting...");
    zkClient.start();
    try {
      EnsurePath ensurePath = zkClient.newNamespaceAwareEnsurePath(SCHEDULER_PATH);
      ensurePath.ensure(zkClient.getZookeeperClient());
    } catch (Exception e) {
      logger.error("init zk path - /schedule error...", e);
    }

    activeStandbyElector = new ActiveStandbyElector();
    logger.debug("activeStandbyElector ready...");
  }

  private void connectDb() {
    if (dbUtil == null) {
      dbUtil = new DbUtil();
    }
  }

  private JobThread getJobThread(String jobZKData) throws Exception {
    logger.info(jobZKData);
    String[] arr = jobZKData.split("#");
    if (arr.length < 10) {
      throw new Exception("parse job info in zk fail - " + jobZKData);
    }
    //        每个字段判null
    int jobId = Integer.parseInt(arr[0]);
    int jobTypeId = Integer.parseInt(arr[2]);
    int jobLevel = Integer.parseInt(arr[3]);
    String jobType = null;
    String querySql = "select type from t_dict_job_type where id = " + jobTypeId;
    Statement statement = dbUtil.createStatement(querySql);
    ResultSet resultSet = dbUtil.executeQuery(statement, querySql);
    while (resultSet.next()) {
      jobType = resultSet.getString("type");
    }
    dbUtil.closeResultSet(resultSet, querySql);
    dbUtil.closeStatement(statement, querySql);
    //        return new JobThread(jobId, jobType, jobLevel, arr[4], arr[5], 4);
    return new JobThread(
        jobId,
        jobType,
        jobLevel,
        arr[4],
        arr[5],
        Integer.parseInt(arr[6]),
        Integer.parseInt(arr[7]),
        arr[8],
        arr[9],
        Integer.parseInt(arr[10]));
  }

  private List<JobThread> getJobFromReadyPath(int size, String namespace) {
    List<JobThread> resultJobs = new ArrayList<JobThread>();
    List<JobThread> readyJobs = new ArrayList<JobThread>();
    String readyPath =
        Configuration.getConfiguration().get("SCHEDULER_READY_PATH") + "_" + namespace;
    String runningPath =
        Configuration.getConfiguration().get("SCHEDULER_RUNNING_PATH") + "_" + namespace;
    String errorPath =
        Configuration.getConfiguration().get("SCHEDULER_ERROR_PATH") + "_" + namespace;
    InterProcessMutex lock = new InterProcessMutex(zkClient, readyPath);
    try {
      if (lock.acquire(20L, TimeUnit.SECONDS)) {
        List<String> children = zkClient.getChildren().forPath(readyPath);
        if ((children.size() == 1) && (children.get(0).contains("lock"))) {
          return resultJobs;
        } else if (children.size() == 0) {
          return resultJobs;
        }
        // 直接children不就ok了？
        String[] readys = children.toArray(new String[children.size()]);
        List<String> runnings = zkClient.getChildren().forPath(runningPath);
        for (String readyJobId : readys) {
          if (runnings.contains(readyJobId)) {
            logger.warn("Job " + readyJobId + " is running, can't run again, please check it");
            // 报警
            //                        EmailUtil.sendMailNormal(EMAIL_LIST, "zzdp",
            //                                "Job " + readyJobId + " is running, can't run again,
            // please check it");
            // 暂时不报警
            //                        WXHelper.asyncSendWXAlarmMsg("[zzdp]Job executeId-" +
            // readyJobId + " is running, " +
            //                                "can't run again, please check it");
          } else if (!readyJobId.contains("lock")) {
            byte[] bytes = null;
            bytes = zkClient.getData().forPath(readyPath + "/" + readyJobId);
            String jobData = new String(bytes);
            readyJobs.add(getJobThread(jobData));
          }
        }
      }
    } catch (Exception e) {
      logger.error("get lock of " + readyPath + " fail - ", e);
    } finally {
      try {
        lock.release();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (readyJobs.size() > 0) {
      logger.info("sort by jobLevel, size : " + readyJobs.size());
      Collections.sort(readyJobs);
      // for test
      //            for (JobThread job : readyJobs){
      //                logger.debug("level " + job.getJobLevel());
      //            }
      for (JobThread jobThread : readyJobs) {
        logger.info(
            "start parse job, jobId : "
                + jobThread.getJobId()
                + ", jobType : "
                + jobThread.getJobType());
        // MR && SPARK
        if (jobThread.getJobType().toUpperCase().contains("MAPREDUCE")
            || jobThread.getJobType().toUpperCase().contains("SPARK")
            || jobThread.getJobType().toUpperCase().contains("SPARK_STREAMING")) {
          if (CheckRM.isReady) {
            logger.info(
                "jobId - " + jobThread.getJobId() + " is a MAPREDUCE or SPARK job, start parse...");
          } else {
            logger.warn(
                "jobId - "
                    + jobThread.getJobId()
                    + " is a MAPREDUCE or SPARK job, and yarn is busy, please wait..");
            continue;
          }
        }
        // {"cmd": "sh /home/work/sunzhiwei/log/tmp_scripts/test/test.sh", "code": 0, "query_name":
        // "test"}
        //                String pythonResultJson = "{\"cmd\": \"sh
        // /home/work/sunzhiwei/log/tmp_scripts/test/test.sh\", \"code\": 0, \"query_name\":
        // \"231\"}";
        String pythonResultJson = parseJobCmd(jobThread, namespace);

        // 任务解析异常
        if (!ToolUtil.isValidString(pythonResultJson)) {
          logger.error(
              "jobId :"
                  + jobThread.getJobId()
                  + " - "
                  + "executorID : "
                  + jobThread.getQueryName()
                  + " parse error !");
          //                    MessageUtil.sendMsg("jobName :" + jobThread.getJobName() + " - " +
          // "executorID : " + jobThread.getQueryName()
          //                            + " 解析异常! 请联系zzdp平台 ", jobThread.getMobiles());
          if (jobThread.getIsMonitor() == 1) {
            WXHelper.asyncSendWXAlarmMsg(
                "jobName :"
                    + jobThread.getJobName()
                    + " - "
                    + "executorID : "
                    + jobThread.getQueryName()
                    + " 解析异常! 请联系zzdp平台 ",
                jobThread.getUserNames().split(","));
          }
          this.dealParseError(
              readyPath, errorPath, namespace, jobThread.getJobId(), jobThread.getQueryName());
          continue;
        }
        // TODO
        //              抛异常，进行处理，返回值不是json，待处理。。。。
        JSONObject resultJson = JSON.parseObject(pythonResultJson);
        if (resultJson.getInteger("code") == 0) {
          jobThread.setCommand(resultJson.getString("cmd"));
          jobThread.setNamespace(namespace);
          // jobThread 本身在已有queryName了
          //                    jobThread.setQueryName(resultJson.getInteger("query_name")); //
          // 随后应该变为Executor id
          resultJobs.add(jobThread);
        } else {
          String failDesc = resultJson.getString("desc");
          //                    String executorId = resultJson.getString("query_name"); //
          // 随后应该变为Executor id
          String scriptLogPath =
              Configuration.getConfiguration().get("SCRIPT_LOG_PATH")
                  + "_"
                  + jobThread.getQueryName()
                  + "_process_info";
          ToolUtil.writeFile(scriptLogPath, failDesc);
          // 用户报警
          //                    MessageUtil.sendMsg(failDesc, jobThread.getMobiles());

          if (jobThread.getIsMonitor() == 1) {
            WXHelper.asyncSendWXAlarmMsg(
                "jobName :"
                    + jobThread.getJobName()
                    + " - executorID : "
                    + jobThread.getQueryName()
                    + failDesc,
                jobThread.getUserNames().split(","));
          }
          // update db and zk with error
          this.dealParseError(
              readyPath, errorPath, namespace, jobThread.getJobId(), jobThread.getQueryName());

          //                    try {
          //                        // 将解析错误的jobId从readyPath中删除，然后写入errorPath
          //                        Stat stat = zkClinet.checkExists().forPath(readyPath + "/" +
          // jobThread.getQueryName());
          //                        if (stat != null) {
          //                            ZkUtil.delZKNodeWithLock(zkClinet, readyPath,
          // jobThread.getQueryName());
          //                        }
          //                        // 先判断errorPath是否存在，存在才能创建子节点
          //                        stat = zkClinet.checkExists().forPath(errorPath);
          //                        if (stat == null){
          //                            zkClinet.create().forPath(errorPath, namespace.getBytes());
          //                        }
          //                        zkClinet.create().forPath(errorPath + "/" +
          // jobThread.getQueryName(), Integer.toString(jobId).getBytes());
          //                    } catch (Exception e) {
          //                        logger.error(e.getMessage());
          //                        try {
          //                            Stat stat = zkClinet.checkExists().forPath(readyPath + "/" +
          // jobThread.getQueryName());
          //                            if (stat != null) {
          //                                ZkUtil.delZKNodeWithLock(zkClinet, readyPath,
          // jobThread.getQueryName());
          //                            }
          //                        } catch (Exception e1) {
          //                            e1.printStackTrace();
          //                        }
          //                    }
          //
          //                    String updateSql = "update t_job_execute_log set
          // job_status=2,schedule_status=4," +
          //
          // "excute_time=now(),excute_end_time=now(),excute_kill_time=null,update_time=now(),job_state=4 where id = " +
          //                            jobThread.getQueryName() + ""; // and schedule_status=2
          //                    Statement statement = dbUtil.createStatement(updateSql);
          //                    dbUtil.executeUpdate(statement, updateSql);
          //                    String scheduleSql = "update t_job_schedule set job_state = 4 where
          // job_id = " + jobId;
          //                    dbUtil.executeUpdate(statement, scheduleSql);
          //                    dbUtil.closeStatement(statement, updateSql);
          //                    logger.error("parseJobCmd fail in getJobFromReadyPath - " + jobId);
        }
        if (resultJobs.size() >= size) {
          break;
        }
      }
    }
    return resultJobs;
  }

  private String parseJobCmd(JobThread jobThread, String namespace) {
    logger.debug("start parse command with jobId - " + jobThread.getJobId() + " on " + namespace);
    HashMap<String, String> map = new HashMap<String, String>();
    map.put("jobId", Integer.toString(jobThread.getJobId()));
    map.put("namespace", namespace);
    map.put("queryName", jobThread.getQueryName());
    map.put("chooseTime", jobThread.getChooseTime());
    String params = JSON.toJSONString(map);
    logger.debug("parseJobCmd json is " + params);
    String result = null;
    try {
      result = executorScript(params);
    } catch (Exception e) {
      logger.error("executorScript fail - ", e);
      //            result = "";
    }
    // result 有可能为null
    return result;
  }

  private String executorScript(String params) throws Exception {
    // for test
    //        String file = System.getProperty("user.dir") + "/" + INTERPRETER_SCRIPT;
    //        String[] cmd = {CMD_INTERPRETER, file, params};

    String[] cmd = {CMD_INTERPRETER, INTERPRETER_SCRIPT, params};
    logger.debug("执行命令：" + Arrays.toString(cmd));
    Process process = Runtime.getRuntime().exec(cmd);
    InputStreamReader ir = new InputStreamReader(process.getInputStream());
    LineNumberReader input = new LineNumberReader(ir);
    process.waitFor();
    String result = input.readLine();
    //        result = input.readLine();
    //        while (result != null){
    //            System.out.println(result);
    //            result = input.readLine();
    //        }
    logger.debug("executorScript return : " + result);
    return result;
  }

  // ready 只从zk里取
  private void readyWatcher() {
    List<String> readySpace = new ArrayList<String>();
    List<String> runningSpace = new ArrayList<String>();
    try {
      List<String> schedulePath = zkClient.getChildren().forPath(SCHEDULER_PATH);
      for (String path : schedulePath) {
        if (path.startsWith("ready")) {
          readySpace.add(path);
        } else if (path.startsWith("run")) {
          runningSpace.add(path);
        }
      }
    } catch (Exception e) {
      logger.error("get SCHEDULER_PATH fail - ", e);
    }
    logger.info("The size of readySpace is " + readySpace.size());
    logger.info("The size of runningSpace is " + runningSpace.size());
    for (String path : readySpace) {
      String readyPath = this.SCHEDULER_PATH + "/" + path;
      try {
        int readyPathNode = zkClient.getChildren().forPath(readyPath).size();
        logger.debug("There are " + readyPathNode + " nodes in " + readyPath);
        if (readyPathNode == 0) {
          continue;
        }
      } catch (Exception e) {
        logger.error("get " + readyPath + " fail - ", e);
      }

      String namespace = path.split("_", 2)[1];
      logger.debug("namespce - " + namespace + " and path - " + path);
      String runningPath =
          Configuration.getConfiguration().get("SCHEDULER_RUNNING_PATH") + "_" + namespace;
      try {
        Stat stat = zkClient.checkExists().forPath(runningPath);
        if (stat == null) {
          zkClient.create().forPath(runningPath, namespace.getBytes());
        }
      } catch (Exception e) {
        logger.error("check " + runningPath + " fail - ", e);
      }
      try {
        int runningPathNode = zkClient.getChildren().forPath(runningPath).size();
        if (runningPathNode < SCHEDULER_RUNNING_SIZE) {
          int takeSize = SCHEDULER_RUNNING_SIZE - runningPathNode;
          logger.info("get ready job from " + readyPath);
          List<JobThread> jobs = getJobFromReadyPath(takeSize, namespace);
          for (JobThread jobThread : jobs) {
            updateRunningDb(jobThread, namespace);
            Thread.sleep(5000);
          }
        }
      } catch (Exception e) {
        logger.error("get ready job from " + readyPath + " fail - ", e);
      }
      logger.info("get ready job from " + readyPath + " ends");
    }
  }

  /**
   * 有没有可能直接从0变为3，也就是直接变为running状态？？？？？？？ 如果手动执行只是变为wait状态，那么在wait变为read时的依赖检测需要判断下
   *
   * @param jobThread
   * @param namespace
   */
  private void updateRunningDb(JobThread jobThread, String namespace) {
    logger.debug(
        "updateRunningDb with "
            + jobThread.getJobId()
            + " and queryName : "
            + jobThread.getQueryName());
    // 如果有executorId就不需要schedule_status
    String querySql =
        "select count(0) as count from t_job_execute_log where id = '"
            + jobThread.getQueryName()
            + "'";
    //                + " and dispatch_namespace = '" + namespace + "' and schedule_status = 2;";
    Statement statement = dbUtil.createStatement(querySql);
    ResultSet resultSet = dbUtil.executeQuery(statement, querySql);
    int count = 0;
    try {
      while (resultSet.next()) {
        count = resultSet.getInt("count");
      }
    } catch (SQLException e) {
      logger.error("updateRunningDb ", e);
    } finally {
      dbUtil.closeResultSet(resultSet, querySql);
    }
    String updateSql = null;
    if (count > 0) {
      // job_state=2 正在运行
      updateSql =
          "update t_job_execute_log set job_state=2,"
              + "excute_time=now(),excute_end_time=null,excute_kill_time=null,update_time=now(),"
              + " dispatch_namespace='"
              + namespace
              + "' where id = "
              + jobThread.getQueryName()
              + ";";

      //            updateSql = "update t_job_execute_log set job_status=0,schedule_status=3," +
      //
      // "excute_time=now(),excute_end_time=null,excute_kill_time=null,update_time=now()," +
      //                    "job_state=2, dispatch_namespace='" + namespace + "' where id = " +
      // jobThread.getQueryName() + ";";
      //                    "job_state=2 where id = " + jobThread.getQueryName() + " and
      // dispatch_namespace = '" + namespace + "';";
    } else {
      // 这里应该走不到，因为这样就是说从0直接变为了3，手动执行时是由0变为1，不会变为3
      logger.error(jobThread.getQueryName() + "not exist in t_job_execute_log");
      //            updateSql = "insert into t_job_execute_log (task_id,job_id, job_status,
      // schedule_status, create_time, " +
      //
      // "excute_time,excute_end_time,excute_ready_time,excute_kill_time,update_time,dispatch_namespace, " +
      //                    "query_name,job_state) values (-1," + jobThread.getJobId() +
      // ",0,3,now(),now(),null,now(),null,now(),'" +
      //                    namespace + "','"+ jobThread.getQueryName() + "',2)";
    }
    logger.info(
        "change the status of " + jobThread.getJobId() + " to running with sql - " + updateSql);
    int num = dbUtil.executeUpdate(statement, updateSql);
    String scheduleSql =
        "update t_job_schedule set job_state = 2 where job_id = " + jobThread.getJobId();
    dbUtil.executeUpdate(statement, scheduleSql);
    dbUtil.closeStatement(statement, scheduleSql);
    if (num > 0) {
      logger.debug(jobThread.getJobId() + " update db ends, start update zk...");
      new Thread(jobThread).start();
      logger.debug(jobThread.getJobId() + " JobThread start...");
    }
  }

  public void run() {
    logger.info("executor start...");
    init();
    while (true) {
      logger.debug("judge executor status...");
      if (activeStandbyElector.isActive()) {
        logger.info(activeStandbyElector.getHostname() + " is active...");
        readyWatcher();
      }
      try {
        Thread.sleep(30000);
      } catch (InterruptedException e) {
        logger.error("break from run for sleep : ", e);
        zkClient.close();
        break;
      }
    }
    logger.error("executor shutdown...");
  }

  private void dealParseError(
      String readyPath, String errorPath, String namespace, int jobId, String queryName) {
    // update db and zk with error
    try {
      // 将解析错误的jobId从readyPath中删除，然后写入errorPath
      Stat stat = zkClient.checkExists().forPath(readyPath + "/" + queryName);
      if (stat != null) {
        ZkUtil.delZKNodeWithLock(zkClient, readyPath, queryName);
      }
      // 先判断errorPath是否存在，存在才能创建子节点
      stat = zkClient.checkExists().forPath(errorPath);
      if (stat == null) {
        zkClient.create().forPath(errorPath, namespace.getBytes());
      }
      zkClient
          .create()
          .forPath(errorPath + "/" + queryName, (jobId + " parse cmd error.").getBytes());
    } catch (Exception e) {
      logger.error("dealParseError ", e);
      try {
        Stat stat = zkClient.checkExists().forPath(readyPath + "/" + queryName);
        if (stat != null) {
          ZkUtil.delZKNodeWithLock(zkClient, readyPath, queryName);
        }
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    }
    // job_state=7 解析失败
    String updateSql =
        "update t_job_execute_log set job_state=7,"
            + "excute_time=now(),excute_end_time=now(),excute_kill_time=null,update_time=now() where id = "
            + queryName
            + "";

    //        String updateSql = "update t_job_execute_log set job_status=2,schedule_status=4," +
    //
    // "excute_time=now(),excute_end_time=now(),excute_kill_time=null,update_time=now(),job_state=4
    // where id = " +
    //                queryName + ""; // and schedule_status=2
    Statement statement = dbUtil.createStatement(updateSql);
    dbUtil.executeUpdate(statement, updateSql);
    String scheduleSql = "update t_job_schedule set job_state = 7 where job_id = " + jobId;

    //        String scheduleSql = "update t_job_schedule set job_state = 4 where job_id = " +
    // jobId;
    dbUtil.executeUpdate(statement, scheduleSql);
    dbUtil.closeStatement(statement, updateSql);
    logger.error("parseJobCmd fail in getJobFromReadyPath - " + jobId);
  }

  public static void main(String[] args) {
    ReadyWatcher readyWatcher = new ReadyWatcher();
    //        HashMap<String, String> map = new HashMap<String, String>();
    //        map.put("jobId", Integer.toString(4));
    //        map.put("namespace", "2018-01-29");
    //        String params = JSON.toJSONString(map);
    //        logger.debug("parseJobCmd json is " + params);
    //        String result = null;
    //        try {
    //            result = readyWatcher.executorScript(params);
    //        } catch (Exception e) {
    //            logger.error("");
    //        }

    //        readyWatcher.run();
    //        String data = "2#* * * ?# ";
    //        System.out.println(data.split("#").length);
    try {
      readyWatcher.init();
      //            JobThread jobThread = readyWatcher.getJobThread(data);
      //            System.out.println(jobThread.getJobType());
      while (true) {
        readyWatcher.getJobFromReadyPath(5, "2018-07-23");
        //                readyWatcher.run();
      }
      //            readyWatcher.readyWatcher();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
