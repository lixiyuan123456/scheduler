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
        // ??????????????????
        Date currentRunTime = CronUtils.lastExecutionTime(jobSchedule.getRunTime());
        // ??????????????????
        Date nextRunTime = CronUtils.nextExecutionTime(jobSchedule.getRunTime());
        // ??????????????????
        // Date lastExecutionTime = CronUtils.lastExecutionTime(jobSchedule.getRunTime());
        // ?????????????????? - ?????????????????? < 1?????????????????????
        if (nextRunTime.getTime() - currentRunTime.getTime() < 3600 * 1000) {
            return;
        }

    /*
    JobExecuteLogService jobExecuteLogService =
        (JobExecuteLogService) jobDataMap.get(JOB_EXECUTE_LOG_SERVICE_KEY);
    List<JobExecuteLog> history =
        jobExecuteLogService.getLast7DaysAutoExecuteHistoryByJobId(jobSchedule.getJobId());
    if (CollectionUtils.isEmpty(history)) { // ?????????????????????????????????????????????
      interrupt();
      return;
    }
    List<Long> costTimeList = new ArrayList<>();
    for (JobExecuteLog log : history) {
      if (log.getChooseRunTime() != null && log.getExecuteTime() != null) {
        costTimeList.add(log.getExecuteTime().getTime() - log.getChooseRunTime().getTime());
      }
    }*/
        // ??????????????????
        // Long avg = costTimeList.stream().collect(Collectors.averagingLong(x -> x)).longValue();
        // log.info("??????id=" + jobSchedule.getJobId() + "???7???cron????????????????????????????????????????????????=" + avg + "??????");

        // ?????????????????? - ?????????????????? <= 15??????
        while (new Date().getTime() - currentRunTime.getTime() <= 15 * 60 * 1000) {
            try {
                Thread.sleep(120000L); // ??????2??????
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        // ??????????????????????????????????????????????????????????????????????????????log????????????
        List<JobExecuteLog> newestRunHistory =
                jobExecuteLogRepository.findByJobIdAndTaskIdAndExecuteTimeBetweenOrderByCreateTimeAsc(
                        jobSchedule.getJobId(), JobExecuteLog.AUTO_EXECUTE_JOB_ID, currentRunTime, new Date());
        if (CollectionUtils.isEmpty(newestRunHistory)) { // ??????
            notify(context);
            return;
        }
        Date executeTime = newestRunHistory.get(0).getExecuteTime();
        if (executeTime == null) { // ??????
            notify(context);
        }
    }

    private void notify(JobExecutionContext context) {
        JobSchedule jobSchedule = (JobSchedule) context.getJobDetail().getJobDataMap().get(CURRENT_JOB);
        UserService userService =
                (UserService) context.getJobDetail().getJobDataMap().get(USER_SERVICE);
        User user = userService.getUserByUserId(jobSchedule.getUserId());
        WXHelper.asyncSendWXMsg(
                "??????id=" + jobSchedule.getJobId() + "????????????=" + jobSchedule.getJobName() + "???????????????????????????",
                user.getPyName(),
                "zhaichuancheng");
    }
}
