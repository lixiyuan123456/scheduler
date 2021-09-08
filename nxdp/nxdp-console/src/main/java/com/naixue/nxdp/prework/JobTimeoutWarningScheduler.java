package com.naixue.nxdp.prework;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.naixue.nxdp.service.JobExecuteLogService;
import com.naixue.nxdp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.naixue.nxdp.dao.JobScheduleRepository;
import com.naixue.nxdp.model.JobExecuteLog;
import com.naixue.nxdp.model.JobSchedule;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.thirdparty.WXHelper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务超时报警
 *
 * @author zhaichuancheng
 */
@Component
public class JobTimeoutWarningScheduler implements ApplicationRunner {

    private static final Long THREAD_INTERVAL_SECONDS = 600L;

    private static final Long THREAD_DELAY_SECONDS = 0L;
    /**
     * 报警间隔时间
     */
    private static final Long WARN_INTERVAL_SECONDS = 600L;

    // key = jobId
    private static final Map<Integer, Container> TIME_CONTAINER =
            Collections.synchronizedMap(new HashMap<>());

    @Autowired
    private UserService userService;

    @Autowired
    private JobExecuteLogService jobExecuteLogService;

    @Autowired
    private JobScheduleRepository jobScheduleRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(
                new MainJobTimeoutWarningThread(
                        TIME_CONTAINER, jobExecuteLogService, jobScheduleRepository, userService),
                THREAD_DELAY_SECONDS,
                THREAD_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
    }

    @Slf4j
    public static class MainJobTimeoutWarningThread implements Runnable {

        private Map<Integer, Container> timeContainer;

        private JobExecuteLogService jobExecuteLogService;

        private JobScheduleRepository jobScheduleRepository;

        private UserService userService;

        public MainJobTimeoutWarningThread(
                Map<Integer, Container> timeContainer,
                JobExecuteLogService jobExecuteLogService,
                JobScheduleRepository jobScheduleRepository,
                UserService userService) {
            this.timeContainer = timeContainer;
            this.jobExecuteLogService = jobExecuteLogService;
            this.jobScheduleRepository = jobScheduleRepository;
            this.userService = userService;
        }

        private synchronized Container getContainer(Integer jobId) {
            Container container = timeContainer.get(jobId);
            if (container != null) {
                if (!LocalDate.now().format(DateTimeFormatter.ISO_DATE).equals(container.getDate())) {
                    timeContainer.remove(jobId);
                    container = null;
                }
            }
            if (container == null) {
                List<JobExecuteLog> history =
                        jobExecuteLogService.getLast7DaysAutoExecuteHistoryByJobId(jobId);
                List<Long> times = new ArrayList<>();
                if (!CollectionUtils.isEmpty(history)) {
                    for (JobExecuteLog h : history) {
                        times.add(h.getExecuteCostTime());
                    }
                }
                timeContainer.put(jobId, new Container(jobId, times));
            }
            return container;
        }

        @Override
        public void run() {
            List<JobExecuteLog> runningJobs = jobExecuteLogService.getTodayRunningAutoExecuteJobs();
            if (CollectionUtils.isEmpty(runningJobs)) {
                return;
            }
            for (JobExecuteLog runningJob : runningJobs) {
                // 判断是否有结束时间
                if (runningJob.getExecuteEndTime() != null) {
                    continue;
                }
                Container container = getContainer(runningJob.getJobId());
                // 任务开始执行时间
                long start = runningJob.getExecuteTime().getTime() / 1000;
                // 当前时间
                long now = new Date().getTime() / 1000;
                // 上次报警时间
                long lastWarnTime =
                        container.getLastWarnTime() == null ? now : container.getLastWarnTime() / 1000;
                // 报警
                if (container.getAvgExecuteCostTime() != 0 // 平均运行时间不为0
                        && (now - start) > container.getAvgExecuteCostTime() // 当前时间-开始时间 > 平均时间
                        && container.getTimes() < 3 // 报警次数<=3
                        && (now - lastWarnTime > WARN_INTERVAL_SECONDS) // 当前时间-上次报警时间  > 报警间隔时间
                ) {
                    log.info(
                            " jobId = "
                                    + runningJob.getJobId()
                                    + " 平均运行时间  = "
                                    + container.getAvgExecuteCostTime()
                                    + " 当前任务耗时（秒）  = "
                                    + (now - start));
                    JobSchedule jobSchedule = jobScheduleRepository.findOne(runningJob.getJobId());
                    String receiverUserId = jobSchedule == null ? null : jobSchedule.getReceiver();
                    User receiver = userService.getUserByUserId(receiverUserId);
                    // User executor = userService.getUserByUserId(runningJob.getUserId());
                    WXHelper.asyncSendWXAlarmMsg(
                            runningJob.getJobName()
                                    + "("
                                    + runningJob.getJobId()
                                    + ")"
                                    + "执行超时"
                                    + "过去7天平均运行时间="
                                    + container.getAvgExecuteCostTime()
                                    + "当前任务耗时="
                                    + (now - start),
                            receiver.getPyName());
                    // 报警次数 + 1
                    container.setTimes(container.getTimes() + 1);
                    // 更新上次报警时间
                    container.setLastWarnTime(new Date().getTime());
                }
            }
        }
    }

    @Data
    private static class Container {

        private Integer jobId;

        private String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        private List<Long> executeCostTimes = new ArrayList<>();

        // 平均耗时
        private Long avgExecuteCostTime = 0L;

        // 上次报警时间
        private Long lastWarnTime;

        // 报警次数
        private Integer times = 0;

        public Container(Integer jobId, List<Long> executeCostTimes) {
            this.jobId = jobId;
            this.executeCostTimes = executeCostTimes;
        }

        public Long getAvgExecuteCostTime() {
            if (!CollectionUtils.isEmpty(executeCostTimes)) {
                Long sum = 0L;
                for (Long executeCostTime : executeCostTimes) {
                    sum += executeCostTime;
                }
                avgExecuteCostTime = sum / executeCostTimes.size();
            }
            return avgExecuteCostTime;
        }
    }
}
