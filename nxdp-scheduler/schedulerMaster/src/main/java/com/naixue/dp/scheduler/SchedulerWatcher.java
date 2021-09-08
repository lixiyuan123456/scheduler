package com.naixue.dp.scheduler;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.EnsurePath;
import org.apache.log4j.Logger;
import org.apache.zookeeper.data.Stat;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import com.alibaba.fastjson.JSONObject;
import com.naixue.dp.model.JobModel;
import com.naixue.dp.monitor.WXHelper;
import com.naixue.dp.util.Configuration;
import com.naixue.dp.util.DbUtil;
import com.naixue.dp.util.ToolUtil;

/** Created by sunzhiwei on 2018/1/20. */
public class SchedulerWatcher implements Runnable {
  private static Logger logger = Logger.getLogger(SchedulerWatcher.class);

  private static final String ZK_HOSTS = Configuration.getConfiguration().get("ZK_HOSTS");
  private static final String ZK_SESSION_TIMEOUT =
      Configuration.getConfiguration().get("ZK_SESSION_TIMEOUT");
  private static final String ZK_CONNECTION_TIMEOUT =
      Configuration.getConfiguration().get("ZK_CONNECTION_TIMEOUT");
  private static final String SCHEDULER_NAMESPACE =
      Configuration.getConfiguration().get("SCHEDULER_NAMESPACE");

  // monitor
  //    private static final String EMAIL_LIST = Configuration.getConfiguration().get("EMAIL_LIST");
  //    private static final String PHONE_LIST = Configuration.getConfiguration().get("PHONE_LIST");
  private static final String USER_LIST = Configuration.getConfiguration().get("USER_LIST");

  private Map<String, JobModel> jobMap = new HashMap<String, JobModel>();
  // 为什么用static
  private DbUtil dbUtil = null;
  private Scheduler scheduler = null;
  private CuratorFramework zkClinet = null;
  // private ActiveStandbyElector activeStandbyElector = null;
  // 常驻内存 上线的任务列表
  private Map<String, JobDetail> scheduleMap = new HashMap<String, JobDetail>();

  public SchedulerWatcher(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  private void init() {
    logger.debug("SchedulerWatcher init...");
    connectDb();
    // retryPolicy参数是指在连接ZK服务过程中重新连接测策略.在它的实现类ExponentialBackoffRetry(int baseSleepTimeMs, int
    // maxRetries)中,baseSleepTimeMs参数代表两次连接的等待时间,maxRetries参数表示最大的尝试连接次数
    zkClinet =
        CuratorFrameworkFactory.builder()
            .connectString(ZK_HOSTS)
            .namespace(SCHEDULER_NAMESPACE)
            .retryPolicy(new ExponentialBackoffRetry(1000, 3))
            .sessionTimeoutMs(Integer.parseInt(ZK_SESSION_TIMEOUT))
            .connectionTimeoutMs(Integer.parseInt(ZK_CONNECTION_TIMEOUT))
            .build();
    logger.debug("SchedulerWatcher zk connecting...");
    zkClinet.start();
    // create hostname path
    try {
      EnsurePath ensurePath =
          zkClinet.newNamespaceAwareEnsurePath(
              Configuration.getConfiguration().get("SCHEDULER_PATH"));
      ensurePath.ensure(zkClinet.getZookeeperClient());
    } catch (Exception e) {
      logger.error("init zk path error...", e);
    }
    /**
     * HA改造
     *
     * @date 2019.03.28
     */
    // activeStandbyElector = new ActiveStandbyElector();
    // logger.debug("activeStandbyElector ready...");
    /*try {
      scheduler = new StdSchedulerFactory().getScheduler();
      scheduler.start();
    } catch (SchedulerException e) {
      logger.error("quartz scheduler start error, " + e.getMessage(), e);
    }*/
  }

  private void connectDb() {
    if (dbUtil == null) {
      dbUtil = new DbUtil();
    }
  }

  private Map<String, JobModel> loadJob() {
    Map<String, JobModel> resultMap = new HashMap<String, JobModel>();
    // status = 1, online
    String querySql =
        "select job_id, user_id, job_name, job_type, run_time, job_level from t_job_schedule where status = 1";
    Statement statement = dbUtil.createStatement(querySql);
    ResultSet resultSet = dbUtil.executeQuery(statement, querySql);
    try {
      while (resultSet.next()) {
        int jobId = resultSet.getInt("job_id");
        String userId = resultSet.getString("user_id");
        String jobName = resultSet.getString("job_name");
        String scheduleTime = resultSet.getString("run_time");
        if (!ToolUtil.isValidString(scheduleTime)) {
          logger.error(jobId + "'s run_time is not valid, please check, don't schedule");
          continue;
        }
        int jobType = resultSet.getInt("job_type");
        int jobLevel = resultSet.getInt("job_level");
        JobModel jobModel = new JobModel(jobId, scheduleTime, jobType, jobLevel, userId, jobName);
        resultMap.put(String.valueOf(jobId) + "#" + scheduleTime, jobModel);
      }
    } catch (SQLException e) {
      logger.error("loadJob " + e.getMessage(), e);
    } finally {
      dbUtil.closeResultSet(resultSet, querySql);
      dbUtil.closeStatement(statement, querySql);
    }
    return resultMap;
  }

  private void offlineJob(Set<String> offline) {
    for (String scheduleJobId : offline) {
      try {
        logger.info("offline " + scheduleJobId);
        scheduler.deleteJob(scheduleMap.get(scheduleJobId).getKey());
        scheduleMap.remove(scheduleJobId);
      } catch (SchedulerException e) {
        e.printStackTrace();
      }
    }
  }

  private void loadSchedule() {
    logger.info("get all online job from db...");
    // 关键字段 scheduleTime
    // 以jobId#scheduleTime为key，解决已上线的job更改调度时间时scheduleMap不更新
    jobMap = loadJob();
    String scheduleKey = null;
    JobModel jobModel = null;
    logger.info("start update scheduleMap...");
    Set<String> offline = new HashSet<String>();
    for (Map.Entry<String, JobDetail> map : scheduleMap.entrySet()) {
      scheduleKey = map.getKey();
      if (!jobMap.containsKey(scheduleKey)) {
        offline.add(scheduleKey);
      }
    }
    // 先把要下线的job放入offline中，然后再从scheduleMap中delete掉，
    // 不能直接在上面的for循环中直接从scheduleMap中delete掉
    if (offline.size() != 0) {
      offlineJob(offline);
    }
    logger.info("start add job to quartz...");
    for (Map.Entry<String, JobModel> map : jobMap.entrySet()) {
      scheduleKey = map.getKey();
      jobModel = map.getValue();
      if (!scheduleMap.containsKey(scheduleKey)
          && ToolUtil.isValidString(jobModel.getScheduleTime())) {
        JobDetail jobDetail = newJob(QuartzJob.class).build();
        jobDetail.getJobDataMap().put("jobId", jobModel.getJobId());
        jobDetail.getJobDataMap().put("userId", jobModel.getUserId());
        jobDetail.getJobDataMap().put("jobName", jobModel.getJobName());
        CronTrigger cronTrigger =
            newTrigger().withSchedule(cronSchedule(jobModel.getScheduleTime())).build();
        try {
          logger.debug("add " + scheduleKey + " in online with cron " + jobModel.getScheduleTime());
          scheduler.scheduleJob(jobDetail, cronTrigger);
          scheduleMap.put(scheduleKey, jobDetail);
        } catch (SchedulerException e) {
          logger.error(
              "There is error when scheduling "
                  + jobModel.getJobId()
                  + ", error info "
                  + e.getMessage(),
              e);
          // 系统报警
          //                    EmailUtil.sendMailNormal(EMAIL_LIST, "zzdp",
          //                            "There is error when scheduling " + jobModel.getJobId() + ",
          // error info " + e.getMessage());
          WXHelper.asyncSendWXAlarmMsg(
              "There is error when scheduling "
                  + jobModel.getJobId()
                  + ", error info "
                  + e.getMessage(),
              USER_LIST.split(","));
        }
      }
    }
    logger.info("all job add to quartz ends...");
  }

  /**
   * 拿到所有namespace下的waiting job
   *
   * @return
   */
  private Map<String, Set<JobModel>> getWaitingJobs() {
    Map<String, Set<JobModel>> waitingJobs = new HashMap<String, Set<JobModel>>();
    // 在这拿到executeId
    String querySql =
        "select job_id, choose_run_time, id, type, retry, retry_time_span, job_name from t_job_execute_log where job_state = 1";
    //        String querySql = "select job_id, choose_run_time, id, type, retry from
    // t_job_execute_log where schedule_status = 1";
    Statement statement = dbUtil.createStatement(querySql);
    ResultSet resultSet = dbUtil.executeQuery(statement, querySql);
    int jobId = 0;
    String chooseTime = null;
    String namespace = null;
    String queryName = null;
    int type = 0;
    int retry = 0;
    int retryTimeSpan = 0;
    String jobName = null;
    try {
      while (resultSet.next()) {
        jobId = resultSet.getInt("job_id");
        chooseTime = resultSet.getString("choose_run_time");
        namespace = resultSet.getString("choose_run_time").substring(0, 10);
        queryName = resultSet.getString("id");
        type = resultSet.getInt("type");
        retry = resultSet.getInt("retry");
        retryTimeSpan = resultSet.getInt("retry_time_span");
        jobName = resultSet.getString("job_name");

        // 是否前端处理，是否需要往t_job_execute_log表中加字段以提高速度
        String query =
            "select receiver, is_monitor, parallel_run from t_job_schedule where job_id = " + jobId;
        Statement s = dbUtil.createStatement(query);
        ResultSet result = dbUtil.executeQuery(s, query);
        String receiver = null;
        int parallelRun = 0;
        int isMonitor = 1;
        while (result.next()) {
          receiver = result.getString("receiver");
          parallelRun = result.getInt("parallel_run");
          isMonitor = result.getInt("is_monitor");
        }
        dbUtil.closeResultSet(result, query);
        if (ToolUtil.isValidString(receiver)) {
          StringBuilder stringBuilder = new StringBuilder();
          for (int i = 0; i < 3; i++) {
            for (String id : receiver.split(",")) {
              String q = " select name, email, mobile from t_user where userid = '" + id + "'";
              result = dbUtil.executeQuery(s, q);
              while (result.next()) {
                stringBuilder.append(result.getString("name")).append(",");
              }
              dbUtil.closeResultSet(result, q);
            }
            if (ToolUtil.isValidString(stringBuilder.toString())
                && stringBuilder.toString().length() > 1) {
              receiver =
                  stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
              break;
            }
            stringBuilder.delete(0, stringBuilder.length());
          }
        }
        dbUtil.closeStatement(s, query);
        if (waitingJobs.containsKey(namespace)) {
          Set<JobModel> treeSet = waitingJobs.get(namespace);
          treeSet.add(
              new JobModel(
                  jobId,
                  queryName,
                  namespace,
                  receiver,
                  type,
                  retry,
                  retryTimeSpan,
                  parallelRun,
                  chooseTime,
                  jobName,
                  isMonitor));
          waitingJobs.put(namespace, treeSet);
        } else {
          Set<JobModel> treeSet = new TreeSet<JobModel>();
          treeSet.add(
              new JobModel(
                  jobId,
                  queryName,
                  namespace,
                  receiver,
                  type,
                  retry,
                  retryTimeSpan,
                  parallelRun,
                  chooseTime,
                  jobName,
                  isMonitor));
          waitingJobs.put(namespace, treeSet);
        }
        jobId = 0;
      }
    } catch (SQLException e) {
      logger.error("from t_job_execute_log get waiting job fail : " + e.getMessage(), e);
    } catch (Exception e) {
      logger.error("from t_job_execute_log get waiting job fail : " + e.getMessage(), e);
    } finally {
      dbUtil.closeResultSet(resultSet, querySql);
      dbUtil.closeStatement(statement, querySql);
    }
    logger.info("getWaitingJob ends...");
    return waitingJobs;
  }

  private boolean getCompleteJobs(String jobId, String runningDay) {
    // 两个任务有大量等待信号的问题，b任务必须等待a任务的所有executedId执行完才能继续执行？？？？
    // TODO 是否可以通过调度时间来检查依赖？天级别的任务依赖时间差
    // 不在乎job是否重跑过，只要完成就ok吧？？
    String querySql =
        "select job_id, job_state, choose_run_time from t_job_execute_log where choose_run_time like '%"
            + runningDay
            + "%' and job_id = "
            + Integer.parseInt(jobId)
            // + " order by choose_run_time desc limit 1;";
            + " order by update_time desc limit 1;";
    //        String querySql = "select job_id from t_job_execute_log where choose_run_time like '%"
    // + day + "%' and job_state = 3 ";
    //        String querySql = "select job_id from t_job_execute_log where dispatch_namespace = '"
    // + day + "' and job_status = 1 ";
    //        String querySql = "select job_id from t_job_execute_log where dispatch_namespace = '"
    // + day + "' and job_status = 1 " +
    //                "and is_repeat = 0";
    Statement statement = dbUtil.createStatement(querySql);
    ResultSet resultSet = dbUtil.executeQuery(statement, querySql);
    int jobState = 0;
    int id = 0;
    String chooseTime = null;
    try {
      while (resultSet.next()) {
        id = resultSet.getInt("job_id");
        jobState = resultSet.getInt("job_state");
        chooseTime = resultSet.getString("choose_run_time");
      }
    } catch (SQLException e) {
      logger.error("from t_job_execute_log get complete job fail : " + e.getMessage(), e);
    } finally {
      dbUtil.closeResultSet(resultSet, querySql);
      dbUtil.closeStatement(statement, querySql);
    }
    logger.info(
        "getCompleteJobs details , id :"
            + id
            + " - jobState :"
            + jobState
            + " - chooseTime:"
            + chooseTime);
    //        jobState为3，运行成功    调度失败之后，重跑是否可以正常触发？？？？
    if (jobState != 3) {
      return false;
    }
    return true;
  }

  private boolean checkParentComplete(int jobId, String runningDay) {
    String parentSql = "select details from t_job_config where id = " + jobId;
    Statement statement = dbUtil.createStatement(parentSql);
    ResultSet resultSet = dbUtil.executeQuery(statement, parentSql);
    String parents = null;
    try {
      while (resultSet.next()) {
        String details = resultSet.getString("details");
        JSONObject json = JSONObject.parseObject(details);
        parents = json.getString("dependencies");
        logger.debug(jobId + " parents - " + parents);
      }
      if (!ToolUtil.isValidString(parents)) {
        logger.info(jobId + " has not a parent...");
        return true;
      }
      String[] parentArray = parents.split(",");
      for (String parent : parentArray) {
        if (!getCompleteJobs(parent, runningDay)) {
          logger.info(jobId + " 's parents not complete, please waiting...");
          return false;
        }
      }
    } catch (SQLException e) {
      logger.error("from t_job_config get parent for jobId fail - " + jobId, e);
    } finally {
      dbUtil.closeResultSet(resultSet, parentSql);
      dbUtil.closeStatement(statement, parentSql);
    }
    return true;
  }

  private JobModel getJobModel(int jodId) {
    String querySql =
        "select job_id, run_time, job_type, job_level from t_job_schedule where job_id = " + jodId;
    Statement statement = dbUtil.createStatement(querySql);
    ResultSet resultSet = dbUtil.executeQuery(statement, querySql);
    JobModel jobModel = null;
    try {
      while (resultSet.next()) {
        int jobId = resultSet.getInt("job_id");
        String scheduleTime = resultSet.getString("run_time");
        int jobType = resultSet.getInt("job_type");
        int jobLevel = resultSet.getInt("job_level");
        jobModel = new JobModel(jobId, scheduleTime, jobType, jobLevel);
      }
    } catch (SQLException e) {
      logger.error("from t_job_schedule get jobModel fail : " + e.getMessage(), e);
    } finally {
      dbUtil.closeResultSet(resultSet, querySql);
      dbUtil.closeStatement(statement, querySql);
    }
    return jobModel;
  }

  private void updateReadQueue(
      String queryName, int jobId, String namespace, String userId, String jobName) {
    String querySql =
        "select count(0) as count from t_job_execute_log where id = " + Integer.parseInt(queryName);
    //                + " and dispatch_namespace = '" + namespace + "' and schedule_status = 1";
    Statement statement = dbUtil.createStatement(querySql);
    ResultSet resultSet = dbUtil.executeQuery(statement, querySql);
    int count = 0;
    try {
      while (resultSet.next()) {
        count = resultSet.getInt("count");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      dbUtil.closeResultSet(resultSet, querySql);
    }
    String updateSql = null;
    if (count > 0) {
      // job_state=6  等待资源
      updateSql =
          "update t_job_execute_log set job_state=6,"
              + "excute_time=null,excute_end_time=null,excute_ready_time=now(),excute_kill_time=null,"
              + "update_time=now(), dispatch_namespace="
              + namespace
              + " where id = "
              + queryName
              + ";";

      //            updateSql = "update t_job_execute_log set job_status=0,schedule_status=2," +
      //
      // "excute_time=null,excute_end_time=null,excute_ready_time=now(),excute_kill_time=null," +
      //                    "update_time=now(), dispatch_namespace=" + namespace + " where id = " +
      // queryName + ";";

      //                    "update_time=now() where id = '" + queryName + "' and dispatch_namespace
      // = '" + namespace + "';";
    } else {
      //            String str = jobId + "_" + ToolUtil.getTime();
      // 这里需要update choose_run_time
      // 这个应该不会执行
      //            updateSql = "insert into t_job_execute_log (task_id,job_id, job_status,
      // schedule_status, create_time," +
      //
      // "excute_time,excute_end_time,excute_ready_time,excute_kill_time,update_time,dispatch_namespace" +
      //                    ") values (-1," + jobId + ",0,2,now(),null,null,now(),null,now(),'" +
      // namespace + "')";

      updateSql =
          "insert into t_job_execute_log (task_id,job_id, job_state, user_id, job_name, create_time,"
              + "excute_time,excute_end_time,excute_ready_time,excute_kill_time,update_time,dispatch_namespace"
              + ") values (-1,"
              + jobId
              + ",6,'"
              + userId
              + "', '"
              + jobName
              + "', now(),null,null,now(),null,now(),'"
              + namespace
              + "')";
    }
    dbUtil.executeUpdate(statement, updateSql);
    dbUtil.closeStatement(statement, updateSql);
  }

  private void waitingWatcher() {
    // namespace -> jobIds
    Map<String, Set<JobModel>> waitingJobs = getWaitingJobs();
    String runningDay = null;
    String readyZKPath = null;
    //        Set<String> completeJobs = null;
    for (Map.Entry<String, Set<JobModel>> item : waitingJobs.entrySet()) {
      // 对SCHEDULER_READY_PATH节点加锁后，创建子节点
      String readyPathRoot = Configuration.getConfiguration().get("SCHEDULER_READY_PATH");
      InterProcessMutex createLock = new InterProcessMutex(zkClinet, readyPathRoot);
      try {
        if (createLock.acquire(20, TimeUnit.SECONDS)) {
          runningDay = item.getKey();
          readyZKPath = readyPathRoot + "_" + runningDay;
          Stat stat = zkClinet.checkExists().forPath(readyZKPath);
          if (stat == null) {
            logger.info("create readyZKPath - " + readyZKPath);
            zkClinet.create().forPath(readyZKPath, runningDay.getBytes());
          }
        }
      } catch (Exception e) {
        logger.error("create readyZKPath fail - " + e.toString(), e);
      } finally {
        try {
          createLock.release();
        } catch (Exception e) {
          logger.error(e.toString(), e);
        }
      }
      // logger.info("get all complete job in runningDay : " + runningDay);
      // completeJobs = getCompleteJobs(runningDay);
      Set<JobModel> jobModels = item.getValue();
      for (JobModel job : jobModels) {
        // JobModel主要是为了往zk里写job信息 完善信息
        // 再次查询db生成jobModel防止手动提交产生null的jobModel
        JobModel jobModel = getJobModel(job.getJobId());
        // queryName不为null，为null就不能继续执行了，报警吧
        jobModel.setQueryName(job.getQueryName());
        jobModel.setUserNames(job.getUserNames());
        jobModel.setRetry(job.getRetry());
        jobModel.setRetryTimeSpan(job.getRetryTimeSpan());
        jobModel.setChooseTime(job.getChooseTime());
        jobModel.setType(job.getType());
        jobModel.setJobName(job.getJobName());
        jobModel.setIsMonitor(job.getIsMonitor());
        // 判断是否需要并发执行，为0是不支持并发执行，1为支持并发执行
        boolean canRun = true;
        if (job.getParallelRun() == 0) {
          // 有无历史任务未结束
          if (hasRunning(job.getJobId(), runningDay, job.getQueryName())) {
            canRun = false;
          }
        }
        //                else if (job.getParallelRun() == 1){
        //                    canRun = true;
        //                }
        // 这里需要判断强制重跑还是依赖重跑
        // type = 3 为强制重跑 -- 强制重跑时是否需要判断canRun???????  暂时需要判断
        if ((canRun && jobModel.getType() == 3)
            || (canRun && checkParentComplete(jobModel.getJobId(), runningDay))) {
          logger.info(
              job.getJobId()
                  + "'s parents are ends, and job_type is "
                  + jobModel.getJobType()
                  + ", put it in ready queue...");
          InterProcessMutex lock = new InterProcessMutex(zkClinet, readyZKPath);
          try {
            if (lock.acquire(20L, TimeUnit.SECONDS)) {
              // 由jobId变为queryName
              // TODO 支持并发运行，path是否应该加个时间戳
              String jobPath = readyZKPath + "/" + job.getQueryName();
              //                            String jobPath = readyZKPath + "/" + job.getJobId();
              Stat stat = zkClinet.checkExists().forPath(jobPath);
              // 在此判断是否支持并发重跑？？？？
              if (stat != null) {
                logger.error(job + " has be putted in ready queue already, will skip it...");
                // 报警
              } else {
                zkClinet.create().forPath(jobPath, jobModel.toString().getBytes());
                logger.info("put job - " + jobModel.toString() + " in zk and db...");
                updateReadQueue(
                    jobModel.getQueryName(),
                    jobModel.getJobId(),
                    runningDay,
                    jobModel.getUserId(),
                    jobModel.getJobName());
              }
            }
          } catch (Exception e) {
            logger.error("get lock error : " + e.getMessage(), e);
          } finally {
            try {
              lock.release();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
        //                else {
        //                    if (canRun){
        //
        //                    } else {
        //                        logger.debug("jobId - " + job.getJobId() + " is schedule, can't
        // schedule again");
        //                    }
        //                }
      }
    }
  }

  private boolean hasRunning(int jobId, String namespace, String queryName) {
    boolean result = false;
    String querySql =
        "select count(0) as count from t_job_execute_log where job_id = "
            + jobId
            + " and choose_run_time like '%"
            + namespace
            + "%' and id < "
            + Integer.parseInt(queryName)
            + " and job_state not in (0, 3, 4, 5, 7, 8, 99);";
    //        String querySql = "select count(0) as count from t_job_execute_log where job_id = " +
    // jobId
    //                + " and choose_run_time like '%" + namespace + "%' and id < " +
    // Integer.parseInt(queryName)
    //                + " and job_state = 1 or job_state = 2 or job_state = 6;";

    //        String querySql = "select count(0) as count from t_job_execute_log where job_id = " +
    // jobId
    //                + " and choose_run_time like '%" + namespace + "%' and schedule_status != 4;";
    Statement statement = dbUtil.createStatement(querySql);
    ResultSet resultSet = dbUtil.executeQuery(statement, querySql);
    int count = 0;
    try {
      while (resultSet.next()) {
        count = resultSet.getInt("count");
      }
    } catch (SQLException e) {
      logger.error("hasRunning " + e.getMessage(), e);
    } finally {
      dbUtil.closeResultSet(resultSet, querySql);
    }
    if (count >= 1) {
      logger.debug(
          "jobId - "
              + jobId
              + " has "
              + count
              + " executorId not complete at"
              + namespace
              + ", so waiting");
      result = true;
    }
    return result;
  }

  public void run() {
    init();
    /**
     * HA改造
     *
     * @date 2019.03.28
     */
    /*while (true) {
      logger.debug("SchedulerWatcher starting...");
      if (activeStandbyElector.isActive()) {
        loadSchedule();
        waitingWatcher();
      }
      try {
        Thread.sleep(15000);
      } catch (InterruptedException e) {
        logger.error("break from run for sleep : " + e.getMessage(), e);
        zkClinet.close();
        break;
      }
    }*/
    while (true) {
      try {
        waitingWatcher();
        Thread.sleep(15000);
      } catch (Exception e) {
        zkClinet.close();
        throw new RuntimeException(e.toString(), e);
      }
    }
  }
}
