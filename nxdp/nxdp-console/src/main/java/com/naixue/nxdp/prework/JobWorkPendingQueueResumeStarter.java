package com.naixue.nxdp.prework;

import com.naixue.nxdp.service.JobWorkPendingQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JobWorkPendingQueueResumeStarter implements ApplicationRunner {

    @Autowired
    private JobWorkPendingQueueService jobWorkPendingQueueService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("process job work queue is start.");
        jobWorkPendingQueueService.resumeJobWorkPendingQueues();
        log.info("process job work queue is end.");
    }
}
