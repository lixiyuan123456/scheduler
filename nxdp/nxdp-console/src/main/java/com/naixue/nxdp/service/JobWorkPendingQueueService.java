package com.naixue.nxdp.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.naixue.nxdp.dao.JobExecuteLogRepository;
import com.naixue.nxdp.dao.JobScheduleRepository;
import com.naixue.nxdp.dao.JobWorkPendingQueueRepository;
import com.naixue.nxdp.model.JobConfig;
import com.naixue.nxdp.model.JobExecuteLog;
import com.naixue.nxdp.model.JobSchedule;
import com.naixue.nxdp.model.JobWorkPendingQueue;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.util.CronUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JobWorkPendingQueueService {

    @Autowired
    private JobWorkPendingQueueRepository jobWorkPendingQueueRepository;

    @Autowired
    private JobScheduleRepository jobScheduleRepository;

    @Autowired
    private OldPageService oldPageService;

    @Autowired
    private UserService userService;

    @Autowired
    private JobExecuteLogRepository jobExecuteLogRepository;

    @Autowired
    private Executor taskExecutor;

    @Transactional
    public void addToQueue(
            final User currentUser, final Integer jobId, Date startDate, Date endDate) {
        Assert.notNull(currentUser, "request parameter currentUser is not allowed to be null.");
        Assert.notNull(jobId, "request parameter jobId is not allowed to be null.");
        if (startDate == null) {
            startDate = new Date();
        }
        if (endDate == null) {
            endDate = new Date();
        }
        final JobSchedule jobSchedule = jobScheduleRepository.findByJobId(jobId);
        Assert.notNull(jobSchedule, "job[" + jobId + "] is not exist.");
        // 判断任务类型
        JobConfig.JobType currentJobType = JobConfig.JobType.getEnum(jobSchedule.getJobType());
        if (currentJobType != JobConfig.JobType.DATA_EXTRACT_SCRIPT
                && currentJobType != JobConfig.JobType.HIVE
                && currentJobType != JobConfig.JobType.MAPREDUCE
                && currentJobType != JobConfig.JobType.SHELL
                && currentJobType != JobConfig.JobType.SPARK
                && currentJobType != JobConfig.JobType.SHELL_IDE) {
            throw new RuntimeException("job[" + jobId + "] does not support to choose time run for now.");
        }
        String cron = jobSchedule.getRunTime();
        if (StringUtils.isEmpty(cron)) {
            throw new RuntimeException("job[" + jobId + "] has no cron expression.");
        }
        List<JobWorkPendingQueue> data = new ArrayList<>();
        final String batchNumber = UUID.randomUUID().toString();
        Date nextExecutionTime = startDate;
        do {
            JobWorkPendingQueue workQueue =
                    JobWorkPendingQueue.builder()
                            .jobId(jobId)
                            .chooseRunTime(nextExecutionTime)
                            .operator(currentUser.getId())
                            .batchNumber(batchNumber)
                            .build();
            data.add(workQueue);
        } while ((nextExecutionTime = CronUtils.nextExecutionTime(cron, nextExecutionTime))
                .before(endDate));
        List<JobWorkPendingQueue> newJobWorkQueues = jobWorkPendingQueueRepository.save(data);
        log.info("a set of jobWorkBufferQueues are created yet,now its time to tigger one by one.");
        ThreadPoolTaskExecutor threadpool = (ThreadPoolTaskExecutor) taskExecutor;
        log.info(
                "threadpool:active threads count = {},queue size = {}.",
                threadpool.getActiveCount(),
                threadpool.getThreadPoolExecutor().getQueue().size());
        taskExecutor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        log.info("async trigger method executeJobOneByOne() at once!!!");
                        executeJobOneByOne(batchNumber, newJobWorkQueues);
                        log.info("async trigger method executeJobOneByOne() success!!!");
                    }
                });
    }

    public Page<JobWorkPendingQueue> pageJobWorkPendingQueues(
            JobWorkPendingQueue condition, Integer pageIndex, Integer pageSize) {
        Specification<JobWorkPendingQueue> specification =
                new Specification<JobWorkPendingQueue>() {
                    @Override
                    public Predicate toPredicate(
                            Root<JobWorkPendingQueue> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                        List<Predicate> predicates = new ArrayList<>();
                        if (condition != null && !StringUtils.isEmpty(condition.getBatchNumber())) {
                            predicates.add(cb.equal(root.get("batchNumber"), condition.getBatchNumber()));
                        }
                        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
                    }
                };
        Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(Sort.Direction.DESC, "id"));
        return jobWorkPendingQueueRepository.findAll(specification, pageable);
    }

    public void killByBatchNumber(final String batchNumber) {
        int changeCount = jobWorkPendingQueueRepository.updateStatus2End(batchNumber);
        log.info("kill by batchNumber success,{} changed.", changeCount);
    }

    // 按jobId分组，同一组下按batchNumber划分批次
    // 不用按jobId分组，直接按batchNumber分批，理由是同一个批号不可能出现两个jobId
    public void resumeJobWorkPendingQueues() {
        // status = 0
        List<JobWorkPendingQueue> jobWorkPendingQueues =
                jobWorkPendingQueueRepository.findByStatus(JobWorkPendingQueue.Status.FALSE.getCode());
        if (CollectionUtils.isEmpty(jobWorkPendingQueues)) {
            return;
        }
        // key = jobId
        // Map<Integer, List<JobWorkPendingQueue>> groups = new HashMap<>();
        // key = batchNumber
        Map<String, List<JobWorkPendingQueue>> groups = new HashMap<>();
        for (JobWorkPendingQueue jobWorkPeningQueue : jobWorkPendingQueues) {
            final String batchNumber = jobWorkPeningQueue.getBatchNumber();
      /*if (!groups.containsKey(jobWorkPeningQueue.getJobId())) {
        groups.put(jobWorkPeningQueue.getJobId(), new ArrayList<>());
      }*/
            if (!groups.containsKey(batchNumber)) {
                groups.put(batchNumber, new ArrayList<>());
            }
            groups.get(batchNumber).add(jobWorkPeningQueue);
        }
        log.info("total groups: {}", Arrays.toString(groups.keySet().toArray()));
        // 按批次串行化
        for (Map.Entry<String, List<JobWorkPendingQueue>> group : groups.entrySet()) {
            taskExecutor.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            executeJobOneByOne(group.getKey(), group.getValue());
                        }
                    });
        }
    }

    private void executeJobOneByOne(
            final String batchNumber, final List<JobWorkPendingQueue> jobWorkPendingQueues) {
        if (CollectionUtils.isEmpty(jobWorkPendingQueues)) {
            return;
        }
        final CountDownLatch latch = new CountDownLatch(jobWorkPendingQueues.size());
        ThreadPoolTaskExecutor threadPool = (ThreadPoolTaskExecutor) taskExecutor;
        log.info(
                "ThreadPool[threadPool={},CorePoolSize={},QueueSize={},MaxPoolSize={}]",
                threadPool.getActiveCount(),
                threadPool.getCorePoolSize(),
                threadPool.getThreadPoolExecutor().getQueue().size(),
                threadPool.getMaxPoolSize());
        taskExecutor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        log.info("process thread is created,current group is batchNumber = {}", batchNumber);
                        User operator = userService.getUserByUserId(jobWorkPendingQueues.get(0).getOperator());
                        for (int i = 0; i < jobWorkPendingQueues.size(); i++) {
                            final JobWorkPendingQueue jobWorkPendingQueue = jobWorkPendingQueues.get(i);
                            // 按batchNumber分批次
                            // final String lock = jobWorkPendingQueue.getBatchNumber().intern();
                            final int changedCount =
                                    jobWorkPendingQueueRepository.updateStatusById(
                                            jobWorkPendingQueue.getId(), JobWorkPendingQueue.Status.TRUE.getCode(), 0);
                            log.info(
                                    "now is begin to process jobWorkQueue:{},update t_job_work_queue result is {}",
                                    jobWorkPendingQueue.toString(),
                                    changedCount);
                            if (changedCount == 1) {
                                log.info(
                                        "jobWorkQueue[id={}] is grabbed,so this thread can trigger next execution.",
                                        jobWorkPendingQueue.getId());
                                // add one to t_job_execute_log
                                final JobExecuteLog jobExecuteLog =
                                        oldPageService.triggerJob(
                                                operator,
                                                jobWorkPendingQueue.getJobId(),
                                                jobWorkPendingQueue.getChooseRunTime());
                                while (true) {
                                    try {
                                        Thread.sleep(30000L); // 每次循环停顿三十秒
                                    } catch (Exception e) {
                                        log.error(e.getMessage(), e);
                                    }
                                    // get lastest data
                                    JobExecuteLog newJobExecuteLog =
                                            jobExecuteLogRepository.findOne(jobExecuteLog.getId());
                                    log.info(
                                            "source jobState = {},now jobState = {}.",
                                            jobExecuteLog.getJobState(),
                                            newJobExecuteLog.getJobState());
                                    if (!newJobExecuteLog.getJobState().equals(0) // 未调度
                                            && !newJobExecuteLog.getJobState().equals(1) // 等待信号
                                            && !newJobExecuteLog.getJobState().equals(2) // 正在运行
                                            && !newJobExecuteLog.getJobState().equals(6) // 等待资源
                                    ) {
                                        log.info(
                                                "jobExecuteLog[jobExecuteId={},jobWorkQueueId={}] is end.",
                                                jobExecuteLog.getId(),
                                                jobWorkPendingQueue.getId());
                                        latch.countDown();
                                        break;
                                    }
                                }
                            } else {
                                latch.countDown();
                            }
                        }
                    }
                });
        try {
            latch.await();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
