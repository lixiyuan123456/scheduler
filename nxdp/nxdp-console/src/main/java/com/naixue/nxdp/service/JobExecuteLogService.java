package com.naixue.nxdp.service;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.base.Strings;
import com.naixue.nxdp.dao.JobExecuteLogRepository;
import com.naixue.nxdp.dao.JobScheduleRepository;
import com.naixue.nxdp.model.JobExecuteLog;
import com.naixue.nxdp.model.JobSchedule;
import com.naixue.nxdp.model.JobSchedule.JobScheduleLevel;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.util.CronUtils;

@Service
public class JobExecuteLogService {

    @Autowired
    private JobExecuteLogRepository jobExecuteLogRepository;

    @Autowired
    private JobScheduleRepository jobScheduleRepository;

    public static JobExecuteLog castScheduleToExecuteLog(
            JobSchedule jobSchedule, Integer taskId, Date chooseRunTime, User user) {
        if (jobSchedule == null) {
            return null;
        }
        // 3 = 单独运行一个Job
        int type = 3;
        if (taskId == -1) { // 系统自动运行
            type = 1;
        } else if (taskId > 0) { // 手动调起重跑
            type = 2;
        }
        JobExecuteLog jobExecuteLog =
                new JobExecuteLog(
                        jobSchedule.getJobId(),
                        taskId,
                        user.getId(),
                        jobSchedule.getJobName(),
                        "",
                        1,
                        jobSchedule.getJobState(),
                        jobSchedule.getDispatchCommand(),
                        type,
                        jobSchedule.getRetry(),
                        chooseRunTime,
                        new Date(),
                        new Date());
        jobExecuteLog.setTargetServer("10.148.16.86");
        return jobExecuteLog;
    }

    /**
     * 最近7天所有自动调度的任务历史
     *
     * @return
     */
    public List<JobExecuteLog> getLast7DaysAutoExecuteHistory() {
        Date start =
                Date.from(LocalDate.now().minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        return jobExecuteLogRepository.findByTaskIdAndExecuteTimeBetween(
                JobExecuteLog.AUTO_EXECUTE_JOB_ID, start, end);
    }

    /**
     * 最近7天某个自动调度的任务历史
     *
     * @param jobId
     * @return
     */
    public List<JobExecuteLog> getLast7DaysAutoExecuteHistoryByJobId(Integer jobId) {
        Date start =
                Date.from(LocalDate.now().minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        return jobExecuteLogRepository.findByJobIdAndTaskIdAndExecuteTimeBetweenOrderByCreateTimeAsc(
                jobId, JobExecuteLog.AUTO_EXECUTE_JOB_ID, start, end);
    }

    public List<JobExecuteLog> getTodayAutoExecuteHistoryByJobId(Integer jobId) {
        Date start = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = new Date();
        return jobExecuteLogRepository.findByJobIdAndTaskIdAndExecuteTimeBetweenOrderByCreateTimeAsc(
                jobId, JobExecuteLog.AUTO_EXECUTE_JOB_ID, start, end);
    }

    /**
     * 今天自动调度的任务
     *
     * @return
     */
    public List<JobExecuteLog> getTodayAutoExecuteHistory() {
        Date start = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = new Date();
        return jobExecuteLogRepository.findByTaskIdAndExecuteTimeBetween(
                JobExecuteLog.AUTO_EXECUTE_JOB_ID, start, end);
    }

    /**
     * 今天正在运行的任务
     *
     * @return
     */
    public List<JobExecuteLog> getTodayRunningAutoExecuteJobs() {
        Date start = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        // 当前正在运行的任务：jobSate = JobSchedule.JobState.RUNNING
        return jobExecuteLogRepository.findByTaskIdAndJobStateAndExecuteTimeGreaterThanEqual(
                JobExecuteLog.AUTO_EXECUTE_JOB_ID, JobSchedule.JobState.RUNNING.getId(), start);
    }

    public JobExecuteLog getJobExecuteLogByJobIdAndTime(
            final Integer jobId, final String endTimestamp) {
        Assert.notNull(jobId, "request parameter jobId is not allowed to be null.");
        JobSchedule jobSchedule = jobScheduleRepository.findByJobId(jobId);
        Assert.notNull(jobSchedule, "this jobId is not exist.");
        JobScheduleLevel level = jobSchedule.getJobScheduleLevel();
        if (level == JobScheduleLevel.NULL) {
            throw new RuntimeException("not support no cron expression job.");
        }
        if (level == JobScheduleLevel.SECOND) {
            throw new RuntimeException("not support a second level job.");
        }
        Date endDate =
                Strings.isNullOrEmpty(endTimestamp)
                        ? new Date()
                        : new Date(Long.parseLong(endTimestamp) * 1000);
        Date beginDate = CronUtils.lastExecutionTime(jobSchedule.getRunTime(), endDate);
        JobExecuteLog jobExecuteLog =
                jobExecuteLogRepository.findFirst1ByJobIdAndExecuteTimeBetween(jobId, beginDate, endDate);
        Assert.notNull(
                jobExecuteLog,
                MessageFormat.format(
                        "job[jobId={0},begin={1},end={2}] execute log is not exist.",
                        jobId, beginDate, endDate));
        return jobExecuteLog;
    }
}
