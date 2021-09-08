package com.naixue.nxdp.cronjob;

import java.util.Date;
import java.util.List;

import com.naixue.nxdp.service.UserService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.util.CollectionUtils;

import com.naixue.nxdp.dao.JobExecuteLogRepository;
import com.naixue.nxdp.dao.JobScheduleRepository;
import com.naixue.nxdp.model.JobExecuteLog;
import com.naixue.nxdp.model.JobSchedule;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.thirdparty.WXHelper;
import com.naixue.nxdp.util.CronUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobStartTimePointTimeoutAlarmCronJob implements Job {

    public static final String JOB_SCHEDULE_REPOSITORY = "jobScheduleRepository";
    public static final String JOB_EXECUTE_LOG_REPOSITORY = "jobExecuteLogRepository";
    public static final String USER_SERVICE = "userService";
    private static final String CURRENT_JOB = "CURRENT_JOB";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        final JobScheduleRepository jobScheduleRepository =
                (JobScheduleRepository) context.getJobDetail().getJobDataMap().get(JOB_SCHEDULE_REPOSITORY);
        final JobExecuteLogRepository jobExecuteLogRepository =
                (JobExecuteLogRepository)
                        context.getJobDetail().getJobDataMap().get(JOB_EXECUTE_LOG_REPOSITORY);
        final Integer jobId = Integer.parseInt(context.getJobDetail().getKey().getName());
        final JobSchedule jobSchedule = jobScheduleRepository.findOne(jobId);
        if (jobSchedule == null) {
            log.debug("job[{}] is not exist.", jobId);
            return;
        }
        if (jobSchedule.getStatus() != JobSchedule.Status.ONLINE.getId()) {
            log.debug("job[{}] is offline.", jobId);
            return;
        }
        if (jobSchedule.getDelayAlarm() == JobSchedule.DelayAlarm.OFF) {
            log.debug("job[{}] does not need to alarm for delay.", jobId);
            return;
        }
        context.getJobDetail().getJobDataMap().put(CURRENT_JOB, jobSchedule);
        // 本次执行时间
        Date currentRunTime = CronUtils.lastExecutionTime(jobSchedule.getRunTime());
        // 下次执行时间
        Date nextRunTime = CronUtils.nextExecutionTime(jobSchedule.getRunTime());
        // 上次执行时间
        // Date lastExecutionTime = CronUtils.lastExecutionTime(jobSchedule.getRunTime());
        // 下次执行时间 - 本次执行时间 < 1小时的中断监听
        if (nextRunTime.getTime() - currentRunTime.getTime() < 3600 * 1000) {
            return;
        }

    /*
    JobExecuteLogService jobExecuteLogService =
        (JobExecuteLogService) jobDataMap.get(JOB_EXECUTE_LOG_SERVICE_KEY);
    List<JobExecuteLog> history =
        jobExecuteLogService.getLast7DaysAutoExecuteHistoryByJobId(jobSchedule.getJobId());
    if (CollectionUtils.isEmpty(history)) { // 没有执行历史记录历史则中断监听
      interrupt();
      return;
    }
    List<Long> costTimeList = new ArrayList<>();
    for (JobExecuteLog log : history) {
      if (log.getChooseRunTime() != null && log.getExecuteTime() != null) {
        costTimeList.add(log.getExecuteTime().getTime() - log.getChooseRunTime().getTime());
      }
    }*/
        // 计算平均耗时
        // Long avg = costTimeList.stream().collect(Collectors.averagingLong(x -> x)).longValue();
        // log.info("任务id=" + jobSchedule.getJobId() + "近7天cron时间到真正开始执行时间的平均耗时=" + avg + "毫秒");

        // 判断当前时间 - 本次执行时间 <= 15分钟
        while (new Date().getTime() - currentRunTime.getTime() <= 15 * 60 * 1000) {
            try {
                Thread.sleep(120000L); // 休息2分钟
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        // 判断当前任务在本次执行时间至当前时间内是否在运行历史log表中存在
        List<JobExecuteLog> newestRunHistory =
                jobExecuteLogRepository.findByJobIdAndTaskIdAndExecuteTimeBetweenOrderByCreateTimeAsc(
                        jobSchedule.getJobId(), JobExecuteLog.AUTO_EXECUTE_JOB_ID, currentRunTime, new Date());
        if (CollectionUtils.isEmpty(newestRunHistory)) { // 报警
            notify(context);
            return;
        }
        Date executeTime = newestRunHistory.get(0).getExecuteTime();
        if (executeTime == null) { // 报警
            notify(context);
        }
    }

    private void notify(JobExecutionContext context) {
        JobSchedule jobSchedule = (JobSchedule) context.getJobDetail().getJobDataMap().get(CURRENT_JOB);
        UserService userService =
                (UserService) context.getJobDetail().getJobDataMap().get(USER_SERVICE);
        User user = userService.getUserByUserId(jobSchedule.getUserId());
        WXHelper.asyncSendWXMsg(
                "任务id=" + jobSchedule.getJobId() + "任务名称=" + jobSchedule.getJobName() + "开始执行时间点延迟",
                user.getPyName(),
                "zhaichuancheng");
    }
}
