package com.naixue.nxdp.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.naixue.nxdp.cronjob.JobStartTimePointTimeoutAlarmCronJob;
import com.naixue.nxdp.dao.JobExecuteLogRepository;
import com.naixue.nxdp.dao.JobScheduleRepository;
import com.naixue.nxdp.model.JobExecuteLog;
import com.naixue.nxdp.model.JobSchedule;
import com.naixue.nxdp.util.SchedulerUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JobService {

    @Autowired
    private JobScheduleRepository jobScheduleRepository;

    @Autowired
    private JobExecuteLogRepository jobExecuteLogRepository;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private UserService userService;

    @Autowired
    private Executor taskExecutor;

    public JobSchedule findJobScheduleByJobId(Integer jobId) {
        return jobScheduleRepository.findByJobId(jobId);
    }

    @Transactional
    public void syncJobState(
            Integer jobId, Integer jobExecuteId, JobSchedule.JobState targetJobState) {
        Assert.notNull(targetJobState, "任务目标状态不允许为空");
        JobSchedule jobSchedule = jobScheduleRepository.findByJobId(jobId);
        Assert.notNull(jobSchedule, "JobSchedule[jobId={}] is not exist!!!");
        jobSchedule.setJobState(targetJobState.getId());
        jobScheduleRepository.save(jobSchedule);
        JobExecuteLog jobExecuteLog = jobExecuteLogRepository.findOne(jobExecuteId);
        Assert.notNull(jobExecuteLog, "JobExecuteLog[jobExecuteId={}] is not exist!!!");
        jobExecuteLog.setJobState(targetJobState.getId());
        jobExecuteLogRepository.save(jobExecuteLog);
    }

    public void cron(final Integer jobId) {
        final JobSchedule jobSchedule = jobScheduleRepository.findOne(jobId);
        cron(jobSchedule);
    }

    public void cron(final JobSchedule jobSchedule) {
        taskExecutor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        // 开启或关闭任务执行时间点超时报警
                        if (!JobSchedule.Status.ONLINE.getId().equals(jobSchedule.getStatus())) {
                            SchedulerUtils.deleteJob(scheduler, String.valueOf(jobSchedule.getJobId()));
                            return;
                        }
                        if (jobSchedule.getDelayAlarm() == JobSchedule.DelayAlarm.OFF) {
                            SchedulerUtils.deleteJob(scheduler, String.valueOf(jobSchedule.getJobId()));
                            return;
                        }
                        if (JobSchedule.Status.ONLINE.getId().equals(jobSchedule.getStatus())
                                && jobSchedule.getDelayAlarm() == JobSchedule.DelayAlarm.ON
                                && !SchedulerUtils.exisit(scheduler, String.valueOf(jobSchedule.getJobId()))) {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put(
                                    JobStartTimePointTimeoutAlarmCronJob.JOB_SCHEDULE_REPOSITORY,
                                    jobScheduleRepository);
                            dataMap.put(
                                    JobStartTimePointTimeoutAlarmCronJob.JOB_EXECUTE_LOG_REPOSITORY,
                                    jobExecuteLogRepository);
                            dataMap.put(JobStartTimePointTimeoutAlarmCronJob.USER_SERVICE, userService);
                            SchedulerUtils.addJob(
                                    scheduler,
                                    JobStartTimePointTimeoutAlarmCronJob.class,
                                    String.valueOf(jobSchedule.getJobId()),
                                    jobSchedule.getRunTime(),
                                    dataMap);
                        }
                    }
                });
    }
}
