package com.naixue.dp.scheduler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.naixue.dp.util.DbUtil;
import com.naixue.dp.util.ToolUtil;

import lombok.extern.slf4j.Slf4j;

/** Created by sunzhiwei on 2018/1/23. */
@Slf4j
public class QuartzJob implements Job {

  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    int jobId = jobExecutionContext.getJobDetail().getJobDataMap().getInt("jobId");
    String userId = jobExecutionContext.getJobDetail().getJobDataMap().getString("userId");
    String jobName = jobExecutionContext.getJobDetail().getJobDataMap().getString("jobName");
    // 在此判断之前调度的jobId是否已经完成，没有完成则报警 重复提交，停止此次调度
    // logger.info(jobId + "'s QuartzJob start...");
    log.debug("任务(jobId={},jobName={})开始执行..................", jobId, jobName);
    DbUtil dbUtil = new DbUtil();
    String nextFireTime = ToolUtil.dateFormat(jobExecutionContext.getNextFireTime());
    String namespace = ToolUtil.getToday();
    String chooseTime = ToolUtil.getDate();
    // jobId 大于0，则是有效的jobId
    if (jobId > 0) {
      // schedule_status为0的状态一般情况下是没有的，也就是说每次调度都会insert一条数据
      // todo 直接取出id，判断id
      // 当天执行n次的job如何选择，比如小时任务，小时任务是否有依赖呀？？？？
      String querySql =
          "select count(0) as count from t_job_execute_log where job_id = "
              + jobId
              + " and choose_run_time like '%"
              + namespace
              + "%' and job_state = 0;";
      //            String querySql = "select count(0) as count from t_job_execute_log where job_id
      // = " + jobId
      //                    + " and choose_run_time like '%" + namespace + "%' and schedule_status =
      // 0;";

      //            String querySql = "select count(0) as count from t_job_execute_log where job_id
      // = " + jobId
      //                    + " and dispatch_namespace = '" + namespace + "' and schedule_status =
      // 0;";
      //            String querySql = "select count(0) as count from t_job_execute_log where job_id
      // = " + jobId
      //                    + " and dispatch_namespace = '" + namespace + "'";
      Statement statement = dbUtil.createStatement(querySql);
      ResultSet resultSet = dbUtil.executeQuery(statement, querySql);
      int count = 0;
      try {
        while (resultSet.next()) {
          count = resultSet.getInt("count");
        }
      } catch (SQLException e) {
        log.error(e.toString(), e);
      } finally {
        dbUtil.closeResultSet(resultSet, querySql);
      }
      String updateSql = null;
      if (count > 0) {
        updateSql =
            "update t_job_execute_log set task_id=-1, user_id='"
                + userId
                + "', job_name='"
                + jobName
                + "',job_state=1, excute_time=null,excute_end_time=null,excute_ready_time=null,"
                + "excute_kill_time=null,update_time=now() where job_id = "
                + jobId
                + " and dispatch_namespace = '"
                + namespace
                + "' and schedule_status = 0;";

        //                updateSql = "update t_job_execute_log set job_status=0,schedule_status=1,"
        // +
        //
        // "excute_time=null,excute_end_time=null,excute_ready_time=null,excute_kill_time=null,update_time=now()" +
        //                        ",job_state=1 where job_id = " + jobId + " and dispatch_namespace
        // = '" + namespace + "' and schedule_status = 0;";
      } else {
        //                String queryName = jobId + "_" + ToolUtil.getTime();
        updateSql =
            "insert into t_job_execute_log (job_id, task_id, user_id, job_name, create_time, "
                + "excute_time,excute_end_time,excute_ready_time,excute_kill_time,update_time,dispatch_namespace,"
                + "choose_run_time,"
                + "job_state"
                + ") values ("
                + jobId
                + ", -1, '"
                + userId
                + "', '"
                + jobName
                + "', now(),null,null,null,null,now(),'"
                + namespace
                + "','"
                + chooseTime
                + "',1)";

        //                updateSql = "insert into t_job_execute_log (task_id,job_id, job_status,
        // schedule_status, create_time, " +
        //
        // "excute_time,excute_end_time,excute_ready_time,excute_kill_time,update_time,dispatch_namespace," +
        //                        "choose_run_time," + "job_state" + ") values (-1," + jobId +
        // ",0,1,now(),null,null,null,null,now(),'" + namespace +
        //                        "','" + chooseTime + "',1)";
      }
      log.info("change the status of " + jobId + " to waiting with sql - " + updateSql);
      dbUtil.executeUpdate(statement, updateSql);
      String scheduleSql =
          "update t_job_schedule set next_fire_time='"
              + nextFireTime
              + "', job_state = 1, "
              + "execute_time = '"
              + chooseTime
              + "' where job_id = "
              + jobId;

      //            String scheduleSql = "update t_job_schedule set next_fire_time='" + nextFireTime
      // + "', job_state = 1 where job_id = " + jobId;
      log.info("update next_fire_time in t_job_schedule with sql - " + scheduleSql);
      dbUtil.executeUpdate(statement, scheduleSql);
      dbUtil.closeConnection(statement);
    }
  }
}
