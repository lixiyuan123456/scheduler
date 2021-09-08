package com.naixue.nxdp.prework;

import java.util.List;

import com.naixue.nxdp.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.naixue.nxdp.dao.JobScheduleRepository;
import com.naixue.nxdp.model.JobSchedule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CronJobStartTimePointTimeoutAlarmCronJobStarter implements ApplicationRunner {

    @Autowired
    private JobScheduleRepository jobScheduleRepository;

    @Autowired
    private JobService jobService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("CronJobStartTimePointTimeoutAlarmCronJobStarter is started.");
        doWork();
        log.info("CronJobStartTimePointTimeoutAlarmCronJobStarter is end.");
    }

    // 上线并且勾选延迟报警
    private void doWork() throws Exception {
        List<JobSchedule> onlineJobs =
                jobScheduleRepository.findAllByStatus(JobSchedule.Status.ONLINE.getId());
        if (CollectionUtils.isEmpty(onlineJobs)) {
            return;
        }
        for (JobSchedule onlineJob : onlineJobs) {
            if (onlineJob.getDelayAlarm() == JobSchedule.DelayAlarm.OFF) {
                continue;
            }
            jobService.cron(onlineJob);
        }
    }
}
