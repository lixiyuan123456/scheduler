package com.naixue.dp.executor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.naixue.dp.monitor.WXHelper;
import com.naixue.dp.util.DbUtil;

/** Created by sunzhiwei on 2018/1/26. */
public class JobThread implements Runnable, Comparable<JobThread> {
  private static Logger logger = Logger.getLogger(JobThread.class);

  private int jobId;
  // 提供一个默认值？
  private String jobType;
  private int jobLevel;
  private String command;
  private String queryName;
  private String namespace;
  private String mobiles;
  private int retry;
  private int retryTimeSpan;
  private String chooseTime;
  private String userNames;
  private String jobName;
  private int isMonitor;

  public JobThread(
      int jobId,
      String jobType,
      int jobLevel,
      String queryName,
      String userNames,
      int retry,
      int retryTimeSpan,
      String chooseTime,
      String jobName,
      int isMonitor) {
    this.jobId = jobId;
    this.jobType = jobType;
    this.jobLevel = jobLevel;
    this.queryName = queryName;
    this.userNames = userNames;
    this.retry = retry;
    this.retryTimeSpan = retryTimeSpan;
    this.chooseTime = chooseTime;
    this.jobName = jobName;
    this.isMonitor = isMonitor;
  }

  public String getUserNames() {
    return userNames;
  }

  public void setUserNames(String userNames) {
    this.userNames = userNames;
  }

  public String getJobName() {
    return jobName;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  public int getIsMonitor() {
    return isMonitor;
  }

  public void setIsMonitor(int isMonitor) {
    this.isMonitor = isMonitor;
  }

  public String getChooseTime() {
    return chooseTime;
  }

  public void setChooseTime(String chooseTime) {
    this.chooseTime = chooseTime;
  }

  public int getRetry() {
    return retry;
  }

  public void setRetry(int retry) {
    this.retry = retry;
  }

  public int getRetryTimeSpan() {
    return retryTimeSpan;
  }

  public void setRetryTimeSpan(int retryTimeSpan) {
    this.retryTimeSpan = retryTimeSpan;
  }

  public String getMobiles() {
    return mobiles;
  }

  public void setMobiles(String mobiles) {
    this.mobiles = mobiles;
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

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public int getJobLevel() {
    return jobLevel;
  }

  public void setJobLevel(int jobLevel) {
    this.jobLevel = jobLevel;
  }

  public int getJobId() {
    return jobId;
  }

  public void setJobId(int jobId) {
    this.jobId = jobId;
  }

  public String getJobType() {
    return jobType;
  }

  public void setJobType(String jobType) {
    this.jobType = jobType;
  }

  public void run() {
    JobExecutor jobExecutor =
        new JobExecutor(this.jobId, this.command, this.namespace, this.queryName);
    int result = -1;
    // 重跑次数，从db中拿
    int retry = 0;
    // 不需要判断  && (result != 0)，因为等于0就break了
    while (this.retry >= retry) {
      Process process = jobExecutor.executor();
      result = jobExecutor.processResult(process, this.getJobType());
      logger.info(
          "jobId - "
              + this.jobId
              + ", queryName - "
              + this.queryName
              + " complete... retry "
              + retry);
      if (result == 0) {
        logger.info("job - " + jobId + " executor successful ");
        break;
      } else {
        if (this.retryTimeSpan > 0) {
          try {
            Thread.sleep(this.retryTimeSpan * 1000);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }

        retry++;
      }
    }
    updateDb(result);
  }

  private void updateDb(int result) {
    String ip = "";
    try {
      ip = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      logger.error(e.toString(), e);
    }
    String updateSql = "";
    String scheduleSql = "";
    for (int i = 0; i < 3; i++) {
      if (result == 0) {
        // 是否需要executorId，需要，同一个namespace中同一jobId可以运行很多次，
        // executeId 变为 queryName
        // jobId和namespace并不能唯一确认一条信息
        //                updateSql = "update t_job_execute_log set job_status=1,schedule_status=4,"
        // +
        //
        // "excute_end_time=now(),excute_kill_time=null,update_time=now(),job_state=3 where id = " +
        //                        this.getQueryName() + ";";

        updateSql =
            "update t_job_execute_log set job_state=3,"
                + "excute_end_time=now(),excute_kill_time=null,update_time=now(),target_server='"
                + ip
                + "' where id = "
                + this.getQueryName()
                + ";";

        scheduleSql = "update t_job_schedule set job_state = 3 where job_id = " + jobId;
      } else {
        //                updateSql = "update t_job_execute_log set job_status=2,schedule_status=4,"
        // +
        //
        // "excute_end_time=now(),excute_kill_time=null,update_time=now(),job_state=4 where id = '"
        // +
        //                        this.getQueryName() + "';";

        updateSql =
            "update t_job_execute_log set job_state=4,"
                + "excute_end_time=now(),excute_kill_time=null,update_time=now(),target_server='"
                + ip
                + "' where id = '"
                + this.getQueryName()
                + "';";

        scheduleSql = "update t_job_schedule set job_state = 4 where job_id = " + jobId;
      }
      //            spark_streaming 时返回固定值，时job状态一直时running

      logger.debug(
          "queryName - "
              + this.queryName
              + " executor complete, and update db with sql - "
              + updateSql);
      DbUtil dbUtil = new DbUtil();
      Statement statement = dbUtil.createStatement(updateSql);
      int num = dbUtil.executeUpdate(statement, updateSql);
      dbUtil.executeUpdate(statement, scheduleSql);
      if (num == 0) {
        logger.debug(
            "queryName - "
                + this.queryName
                + "update db with sql - "
                + updateSql
                + "and return num is "
                + num
                + " to continue.");
        continue;
      } else {
        dbUtil.closeConnection(statement);
        break;
      }
    }
    // 用户报警
    if (result != 0) {
      //            logger.error(this.jobId + "-" + this.queryName + " fail, please check out. user
      // monitor");
      //            MessageUtil.sendMsg("jobId: " + this.jobId + ", executeId: " + this.queryName +
      // " fail, please check out.", this.mobiles);
      WXHelper.asyncSendWXAlarmMsg(
          "jobName "
              + this.getJobName()
              + " - executorID : "
              + this.getQueryName()
              + " fail, please check out.",
          this.getUserNames().split(","));
    }
  }

  public int compareTo(JobThread jobThread) {
    if (jobLevel > jobThread.getJobLevel()) {
      return -1;
    } else if (jobLevel < jobThread.getJobLevel()) {
      return 1;
    } else {
      return 0;
    }
  }

  public static void main(String[] args) throws SQLException {
    DbUtil dbUtil = new DbUtil();
    String jobZKData = "34604#6 6 * * * ? *#5#2#4118#sunzhiwei#0#2018-07-15 04:06:06#shell_test#0";
    String[] arr = jobZKData.split("#");
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
    JobThread jobThread =
        new JobThread(
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

    jobThread.run();
  }
}
