package com.naixue;

import static org.quartz.CronScheduleBuilder.cronSchedule;

import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.SqlSession;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.naixue.dp.scheduler.QuartzJob;
import com.naixue.dp.scheduler.SchedulerWatcher;
import com.naixue.scheduler.jdbc.support.MybatisManager;
import com.naixue.scheduler.mapper.JobScheduleMapper;
import com.naixue.scheduler.model.JobSchedule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

  public static void main(String[] args) {
    try {
      SchedulerFactory factory = new StdSchedulerFactory("quartz.properties");
      Scheduler scheduler = factory.getScheduler();
      scheduler.start();
      // new Thread(new JobHandler(scheduler), "JOB-HANDLER-THREAD").start();
      // new Thread(new SchedulerWatcher(scheduler), "schedulerWatcherThread").start();
      ThreadPool.getThreadPool().submit(new JobHandler(scheduler));
      ThreadPool.getThreadPool().submit(new SchedulerWatcher(scheduler));
    } catch (Exception e) {
      log.error(e.toString(), e);
      throw new RuntimeException(e.toString(), e);
    }
  }

  @Slf4j
  private static class JobHandler implements Runnable {

    private Scheduler scheduler;

    public JobHandler(Scheduler scheduler) {
      this.scheduler = scheduler;
    }

    public String generateJobKey(JobSchedule job) {
      Objects.requireNonNull(job, "入参job不允许为空");
      return job.getJobId() + "#" + job.getRunTime();
    }

    public List<JobSchedule> getOnlineOrLoadedInQuartzJobs() {
      SqlSession session = MybatisManager.getSqlSessionFactory().openSession();
      try {
        JobScheduleMapper mapper = session.getMapper(JobScheduleMapper.class);
        List<JobSchedule> onlineJobs = mapper.findOnlineJobs();
        List<JobSchedule> loadedInQuartzJobs = mapper.findLoadedInQuartzJobs();
        onlineJobs.addAll(loadedInQuartzJobs);
        return onlineJobs;
      } catch (Exception e) {
        log.error(e.toString(), e);
        throw new RuntimeException(e.toString(), e);
      } finally {
        session.close();
      }
    }

    public void addJobToQuartz(JobSchedule job) {
      // 开启事务
      SqlSession session = MybatisManager.getSqlSessionFactory().openSession(false);
      try {
        // 当前线程直接更新表的加载状态然后判断受影响的条数是否等于1，若等于1则说明当前线程抢到了资源，然后添加任务
        JobScheduleMapper mapper = session.getMapper(JobScheduleMapper.class);
        int affectedRows =
            mapper.changeLoadInQuartzStatus(
                job.getJobId(), job.getVersion(), JobSchedule.LOADED_IN_QUARTZ_STATUS);
        if (affectedRows == 1) {
          JobDetail jobDetail =
              JobBuilder.newJob(QuartzJob.class).withIdentity(generateJobKey(job)).build();
          jobDetail.getJobDataMap().put("jobId", job.getJobId());
          jobDetail.getJobDataMap().put("userId", job.getUserId());
          jobDetail.getJobDataMap().put("jobName", job.getJobName());
          CronTrigger trigger =
              TriggerBuilder.newTrigger().withSchedule(cronSchedule(job.getRunTime())).build();
          scheduler.scheduleJob(jobDetail, trigger);
        }
        // 提交事务
        session.commit();
      } catch (Exception e) {
        // 事务回滚
        session.rollback();
        log.error(e.toString(), e);
        // 报警
      } finally {
        session.close();
      }
    }

    public void deleteJobFromQuartz(JobSchedule job) {
      // 开启事务
      SqlSession session = MybatisManager.getSqlSessionFactory().openSession(false);
      try {
        // 任务加载到Quartz后则更新表的状态
        JobScheduleMapper mapper = session.getMapper(JobScheduleMapper.class);
        int affectedRows =
            mapper.changeLoadInQuartzStatus(
                job.getJobId(), job.getVersion(), JobSchedule.UNLOAD_IN_QUARTZ_STATUS);
        if (affectedRows == 1) {
          scheduler.deleteJob(new JobKey(generateJobKey(job)));
        }
        // 提交事务
        session.commit();
      } catch (Exception e) {
        // 事务回滚
        session.rollback();
        log.error(e.toString(), e);
        // 报警
      } finally {
        session.close();
      }
    }

    public void handleJobs() {
      List<JobSchedule> jobs = getOnlineOrLoadedInQuartzJobs();
      if (CollectionUtils.isEmpty(jobs)) {
        return;
      }
      for (JobSchedule job : jobs) {
        // 下线任务：已加载到Quartz并且任务状态为下线
        if (job.getLoadedInQuartz() && job.getStatus() == JobSchedule.Status.OFFLINE.getId()) {
          deleteJobFromQuartz(job);
        }
        // 上线任务：未加载到Quartz并且任务状态为上线
        else if (!job.getLoadedInQuartz() && job.getStatus() == JobSchedule.Status.ONLINE.getId()) {
          addJobToQuartz(job);
        }
        // 前端强制要求更改CRON表达式时必须先下线再上线【目前暂时不提供实现】
        // 更新任务：已加载到Quartz并且任务状态为上线并且CRON改变
        else if (job.getLoadedInQuartz() && job.getStatus() == JobSchedule.Status.ONLINE.getId()) {
        }
      }
    }

    @Override
    public void run() {
      while (true) {
        handleJobs();
        /** 暂停60秒 */
        try {
          Thread.sleep(60000);
        } catch (InterruptedException e) {
          log.error(e.toString(), e);
        }
      }
    }
  }
}
