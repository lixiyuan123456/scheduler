package com.naixue.nxdp.prework;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import com.naixue.nxdp.service.UserService;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.util.CollectionUtils;

import com.naixue.nxdp.cronjob.JobStartTimePointTimeoutAlarmCronJob;
import com.naixue.nxdp.dao.JobExecuteLogRepository;
import com.naixue.nxdp.dao.JobScheduleRepository;
import com.naixue.nxdp.dao.mapper.JobExecuteLogMapper;
import com.naixue.nxdp.model.JobSchedule;
import com.naixue.nxdp.service.JobService;
import com.naixue.nxdp.util.SchedulerUtils;

@Deprecated
// @Component
public class AutostartProcessTrigger implements ApplicationRunner {

    private static final String SYNC_SPARK_STREAMING_RUNNING_STATE_JOB =
            "sync_spark_streaming_running_state_job";
    @Autowired
    private Scheduler scheduler;
    @Autowired
    private JobScheduleRepository jobScheduleRepository;
    @Autowired
    private JobExecuteLogRepository jobExecuteLogRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private JobExecuteLogMapper jobExecuteLogMapper;
    @Autowired
    private Executor taskExecutor;
    @Autowired
    private JobService jobService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // triggerSparkStreamingRunningStateListener();
    }

    // 上线并且勾选延迟报警
    private void triggerOnlineJobExecuteTimePointTimeoutListener() throws Exception {
        List<JobSchedule> onlineJobs =
                jobScheduleRepository.findAllByStatus(JobSchedule.Status.ONLINE.getId());
        if (CollectionUtils.isEmpty(onlineJobs)) {
            return;
        }
        for (JobSchedule onlineJob : onlineJobs) {
            if (onlineJob.getDelayAlarm() == JobSchedule.DelayAlarm.OFF) {
                continue;
            }
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put(
                    JobStartTimePointTimeoutAlarmCronJob.JOB_SCHEDULE_REPOSITORY, jobScheduleRepository);
            dataMap.put(
                    JobStartTimePointTimeoutAlarmCronJob.JOB_EXECUTE_LOG_REPOSITORY, jobExecuteLogRepository);
            dataMap.put(JobStartTimePointTimeoutAlarmCronJob.USER_SERVICE, userService);
            SchedulerUtils.addJob(
                    scheduler,
                    JobStartTimePointTimeoutAlarmCronJob.class,
                    String.valueOf(onlineJob.getJobId()),
                    onlineJob.getRunTime(),
                    dataMap);
        }
    }

    private void triggerSparkStreamingRunningStateListener() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(JobExecuteLogMapper.class.getName(), jobExecuteLogMapper);
        dataMap.put(Executor.class.getName(), taskExecutor);
        dataMap.put(JobService.class.getName(), jobService);
        SchedulerUtils.addJob(
                scheduler,
                SparkStreamingRunningStateListener.class,
                SYNC_SPARK_STREAMING_RUNNING_STATE_JOB,
                "0 0 0/1 * * ?",
                dataMap);
    }
}
