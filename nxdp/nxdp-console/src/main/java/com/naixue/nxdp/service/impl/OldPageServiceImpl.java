package com.naixue.nxdp.service.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.naixue.nxdp.websocket.JobLogReaderWebSocketClient;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.naixue.zzdp.common.util.ShellUtils;
import com.naixue.nxdp.config.CFG;
import com.naixue.nxdp.dao.JobConfigRepository;
import com.naixue.nxdp.dao.JobDependenciesRepository;
import com.naixue.nxdp.dao.JobExecuteLogRepository;
import com.naixue.nxdp.dao.JobScheduleRepository;
import com.naixue.nxdp.model.JobConfig;
import com.naixue.nxdp.model.JobConfig.JobType;
import com.naixue.nxdp.model.JobDependencies;
import com.naixue.nxdp.model.JobExecuteLog;
import com.naixue.nxdp.model.JobSchedule;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.service.JobExecuteLogService;
import com.naixue.nxdp.service.JobService;
import com.naixue.nxdp.service.JobWorkPendingQueueService;
import com.naixue.nxdp.service.OldPageService;
import com.naixue.nxdp.service.UserService;
import com.naixue.nxdp.util.CronUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("OldPageService")
public class OldPageServiceImpl implements OldPageService {

    @Autowired
    private JobConfigRepository jobConfigRepository;

    @Autowired
    private JobScheduleRepository jobScheduleRepository;

    @Autowired
    private JobExecuteLogRepository jobExecuteLogRepository;

    @Autowired
    private JobDependenciesRepository jobDependenciesRepository;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private UserService userService;

    @Autowired
    private Executor taskExecutor;

    @Autowired
    private JobService jobService;

    @Autowired
    private JobWorkPendingQueueService jobWorkQueueService;

    public static void main111(String[] args) throws Exception {
        String cron = "0 0 0/1 * * ? *";
        Date startDate =
                new DateFormatter("yyyy-MM-dd HH:mm:ss").parse("2019-04-23 15:20:00", Locale.CHINA);
        Date endDate =
                new DateFormatter("yyyy-MM-dd HH:mm:ss").parse("2019-04-23 15:25:00", Locale.CHINA);
        Date nextExecutionTime = startDate;
        while ((nextExecutionTime = CronUtils.nextExecutionTime(cron, nextExecutionTime))
                .before(endDate)) {
            System.out.println(
                    new DateFormatter("yyyy-MM-dd HH:mm:ss").print(nextExecutionTime, Locale.CHINA));
        }
        do {
            System.out.println(
                    new DateFormatter("yyyy-MM-dd HH:mm:ss").print(nextExecutionTime, Locale.CHINA));
        } while ((nextExecutionTime = CronUtils.nextExecutionTime(cron, nextExecutionTime))
                .before(endDate));
    }

    /**
     * ??????job????????????????????????t_job_config??????t_job_schedule???
     *
     * @param jobId
     * @param status ????????????
     * @return
     */
    @Transactional
    @Override
    public void changeJobStatus(Integer jobId, Integer status) {
        // ??????t_job_config?????????
        // // ????????????
        JobConfig jobConfig = jobConfigRepository.findOne(jobId);
        Assert.notNull(jobConfig, "JobConfig?????????jobId=" + jobId);
        // // ????????????
        JSONObject detailJson = JSONObject.parseObject(jobConfig.getDetails());
        if (detailJson == null || !detailJson.containsKey("scheduler")) {
            throw new RuntimeException("jobId=" + jobId + "??????????????????????????????JSON??????");
        }
        JSONObject scheduleJson = detailJson.getJSONObject("scheduler");
        scheduleJson.put("status", status + "");
        detailJson.put("scheduler", scheduleJson);
        String details = detailJson.toJSONString();
        // ??????1.??????????????????????????? 2.?????????????????????
        if (JobSchedule.Status.DELETE.getId() == status) {
            JobSchedule jobSchedule = jobScheduleRepository.findOne(jobId);
            Assert.notNull(jobSchedule, "JobSchedule?????????jobId=" + jobId);
            if (JobSchedule.Status.ONLINE.getId() == jobSchedule.getStatus()) {
                throw new RuntimeException("???????????????????????????????????????");
            }
        }
        if (JobSchedule.Status.OFFLINE.getId() == status
                || JobSchedule.Status.DELETE.getId() == status) {
            List<JobDependencies> dependencies = jobDependenciesRepository.findByDependentJobId(jobId);
            if (!CollectionUtils.isEmpty(dependencies)) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < dependencies.size(); i++) {
                    if (i == dependencies.size() - 1) {
                        sb.append(dependencies.get(i).getJobId());
                    } else {
                        sb.append(dependencies.get(i).getJobId() + ",");
                    }
                }
                throw new RuntimeException("???????????????jobId=" + sb.toString() + "??????");
            }
        }
        JobConfig.JobStatus jobStatus =
                JobSchedule.Status.DELETE.getId() == status
                        ? JobConfig.JobStatus.DELETE
                        : JobConfig.JobStatus.NORMAL;
        jobConfigRepository.updateStatusAndDetails(jobStatus.getId(), details, jobId);
        // ??????t_job_schedule?????????
        jobScheduleRepository.updateStatusByJobId(status, jobId);
        jobService.cron(jobId);
    }

    /**
     * ???????????????????????????t_job_schedule???????????????t_job_execute_log??????
     *
     * @param jobId
     * @param chooseRunTime
     * @param user
     * @return
     */
    @Transactional
    @Override
    public String runJobManual(Integer jobId, Date chooseRunTime, User user) throws RuntimeException {
        try {
            triggerJob(user, jobId, chooseRunTime);
            return "true";
        } catch (Exception e) {
            return "false";
        }
    }

    @Transactional
    @Override
    public JobExecuteLog triggerJob(final User currentUser, final Integer jobId, Date chooseRunTime)
            throws RuntimeException {
        // 1 ??????????????????
        Integer code = jobScheduleRepository.updateJobStateByJobId(1, jobId);
        if (code == 0) {
            throw new RuntimeException("trigger job[" + jobId + "] execution failure.");
        }
        // 2 ??????????????????
        // 2.1 ??????????????????
        JobSchedule jobSchedule = jobScheduleRepository.findOne(jobId);
        // 2.2 ??????t_job_execute_log??????
        if (chooseRunTime == null) {
            chooseRunTime = new Date();
        }
        JobExecuteLog jobExecuteLog =
                JobExecuteLogService.castScheduleToExecuteLog(jobSchedule, 0, chooseRunTime, currentUser);
        // 2.3 ??????????????????
        return jobExecuteLogRepository.save(jobExecuteLog);
    }

    /**
     * ??????kill???????????????t_job_schedule?????????t_job_execute_log????????????
     *
     * @param jobId
     * @param executeId ????????????ID
     * @return
     */
    @Transactional
    @Override
    public void killRunningJob(Integer jobId, Integer executeId) throws Exception {
        Assert.notNull(jobId, "????????????jobId???????????????");
        final JobSchedule jobSchedule = jobScheduleRepository.findOne(jobId);
        // ???????????????????????????kill??????????????????bug????????????job???????????????????????????????????????????????????????????????jobexecutelog??????????????????
    /*if (!JobSchedule.JobState.RUNNING.getId().equals(jobSchedule.getJobState())) {
      throw new RuntimeException("????????????JobState=" + jobSchedule.getJobState() + "?????????kill");
    }*/
        // ?????????????????????????????????kill
        final JobType currentJobType = JobConfig.JobType.getEnum(jobSchedule.getJobType());
        if (!currentJobType.equals(JobConfig.JobType.HIVE)
                && !currentJobType.equals(JobConfig.JobType.MAPREDUCE)
                && !currentJobType.equals(JobConfig.JobType.SPARK)
                && !currentJobType.equals(JobConfig.JobType.SPARK_STREAMING)) {
            throw new RuntimeException(currentJobType.getName() + "???????????????kill");
        }
        JobExecuteLog jobExecuteLog = null;
        if (executeId == null) { // ????????????????????????id??????????????????????????????????????????jobid?????????running???????????????
            jobExecuteLog =
                    jobExecuteLogRepository.findFirst1ByJobIdAndJobStateOrderByIdAsc(
                            jobId, JobSchedule.JobState.RUNNING.getId());
            if (jobExecuteLog == null) {
                throw new RuntimeException("?????????????????????????????????????????????????????????");
            }
            executeId = jobExecuteLog.getId();
        } else { // ????????????????????????id?????????????????????????????????id???????????????????????????running
            jobExecuteLog = jobExecuteLogRepository.findOne(executeId);
            if (jobExecuteLog == null) {
                throw new RuntimeException("?????????????????????????????????????????????????????????");
            }
            // ?????????????????????????????????kill
            if (!JobSchedule.JobState.RUNNING.getId().equals(jobExecuteLog.getJobState())) {
                throw new RuntimeException("??????????????????=" + jobExecuteLog.getJobState() + "????????????kill");
            }
        }
        // ??????t_job_schedule???????????????killing??????
        jobScheduleRepository.updateJobStateByJobId(JobSchedule.JobState.KILLING.getId(), jobId);
        // ??????t_job_execute_log???????????????killing??????
        jobExecuteLogRepository.updateStateById(JobSchedule.JobState.KILLING.getId(), executeId);
        // ????????????????????????kill job
        /*taskExecutor.execute(new KillJobThread(jobScheduleRepository, jobExecuteLogRepository, jobId, executeId));*/
        final JobExecuteLog finalJobExecuteLog = jobExecuteLog;
        final Integer finalExecuteId = executeId;
        log.debug("kill start >>> jobId={},jobExecuteId={}", jobId, executeId);
        taskExecutor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 1 KILL
                            String url =
                                    String.format(
                                            CFG.WEBSOCKET_HADOOP_JOB_EXECUTE_LOG_READER_URL + UUID.randomUUID(),
                                            finalJobExecuteLog.getTargetServer());
                            new JobKiller(url, jobId, finalExecuteId);
                            // 2 ??????t_job_schedule??????
                            jobScheduleRepository.updateJobStateByJobId(
                                    JobSchedule.JobState.KILLED.getId(), jobId);
                            // 3 ??????t_job_execute_log??????
                            jobExecuteLogRepository.updateStateById(
                                    JobSchedule.JobState.KILLED.getId(), finalExecuteId);
                        } catch (Exception e) {
                            throw new RuntimeException(e.toString(), e);
                        }
                    }
                });
    }

  /*public static void main(String[] args) throws URISyntaxException {
    String url = String.format("ws://%s:8081/websocket/" + UUID.randomUUID(), "127.0.0.1");
    new JobKiller(url, 35072, 253109);
  }*/

    /**
     * ????????????job?????????????????????
     *
     * @param jobId
     * @return
     */
    @Override
    public List<Map<String, Object>> getJobAllChildren(Integer jobId) {
        // ????????????????????????????????????ID
        List<Map<String, Object>> childList = new ArrayList<>();
        Set<Integer> allChildList = new HashSet<>();
        Integer[] jobIds = new Integer[]{jobId};
        List<Integer> jobIdList = new ArrayList<>();
        // ????????????????????????????????????????????????????????????
        while (true) {
            jobIdList.clear();
            List<JobDependencies> jobList = jobDependenciesRepository.findByDependentJobIdIn(jobIds);
            for (JobDependencies job : jobList) {
                if (!allChildList.contains(job.getJobId())) {
                    allChildList.add(job.getJobId());
                    jobIdList.add(job.getJobId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("jobId", job.getJobId());
                    map.put("parentId", job.getDependentJobId());
                    childList.add(map);
                }
            }
            if (jobIdList.size() == 0) {
                break;
            } else {
                jobIds = new Integer[jobIdList.size()];
                jobIds = jobIdList.toArray(jobIds);
            }
        }
        return childList;
    }

    @Transactional
    @Override
    public void runJobWithTimeSpan(User currentUser, Integer jobId, Date startDate, Date endDate)
            throws RuntimeException {
    /*Assert.notNull(jobId, "jobId???????????????");
    Assert.notNull(startDate, "startDate???????????????");
    Assert.notNull(endDate, "endDate???????????????");
    JobSchedule jobSchedule = jobScheduleRepository.findByJobId(jobId);
    Assert.notNull(jobSchedule, "??????[" + jobId + "]?????????");
    // ??????????????????
    JobType currentJobType = JobConfig.JobType.getEnum(jobSchedule.getJobType());
    if (currentJobType != JobConfig.JobType.DATA_EXTRACT_SCRIPT
        && currentJobType != JobConfig.JobType.HIVE
        && currentJobType != JobConfig.JobType.MAPREDUCE
        && currentJobType != JobConfig.JobType.SHELL
        && currentJobType != JobConfig.JobType.SPARK
        && currentJobType != JobConfig.JobType.SHELL_IDE) {
      throw new RuntimeException("??????[" + jobId + "]???????????????[" + currentJobType + "]?????????????????????????????????");
    }
    String cron = jobSchedule.getRunTime();
    if (StringUtils.isEmpty(cron)) {
      throw new RuntimeException("??????[" + jobId + "]?????????CRON??????????????????????????????????????????????????????????????????");
    }
    log.debug(
        "??????[jobId={},cron={}],startDate={},endDate={}",
        jobId,
        cron,
        new DateFormatter("yyyy-MM-dd HH:mm:ss").print(startDate, Locale.CHINA),
        new DateFormatter("yyyy-MM-dd HH:mm:ss").print(endDate, Locale.CHINA));*/
    /*Date nextExecutionTime = startDate;
    do {
      log.debug(
          "??????[jobId={},cron={},currentExecutionTime={}]",
          jobId,
          cron,
          new DateFormatter("yyyy-MM-dd HH:mm:ss").print(nextExecutionTime, Locale.CHINA));
      runJobManual(jobId, nextExecutionTime, currentUser);
    } while ((nextExecutionTime = CronUtils.nextExecutionTime(cron, nextExecutionTime))
        .before(endDate));*/
        jobWorkQueueService.addToQueue(currentUser, jobId, startDate, endDate);
    }

    @Slf4j
    public static class JobKiller extends JobLogReaderWebSocketClient {

        private static final Pattern pattern = Pattern.compile("application_\\d+_\\d+");

        private Integer jobId, jobExecuteId;

        private Set<String> exists = Collections.synchronizedSet(new HashSet<String>());

        public JobKiller(String url, Integer jobId, Integer jobExecuteId) throws URISyntaxException {
            super(new URI(url), jobExecuteId + "");
            this.jobId = jobId;
            this.jobExecuteId = jobExecuteId;
            log.debug(
                    "kill job websocket client is started......url={},jobId={},jobExecuteId={}",
                    url,
                    jobId,
                    jobExecuteId);
        }

        @Override
        public void onMessage(String message) {
            log.info("kill job websocket client received message = {}", message);
            try (BufferedReader reader = new BufferedReader(new StringReader(message));) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    while (matcher.find()) {
                        String applicationId = matcher.group();
                        if (!exists.contains(applicationId)) {
                            exists.add(applicationId);
                            log.info(
                                    "kill job >>> JobId={},JobExecuteId={},ApplicationId={}",
                                    jobId,
                                    jobExecuteId,
                                    applicationId);
                            // ShellUtils.exec(" yarn application -kill " + applicationId);
                            ShellUtils.exec("yarn application -kill " + applicationId);
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e.toString(), e);
                throw new RuntimeException(e);
            }
        }
    }

    @Deprecated
    @Slf4j
    @Data
    public static class KillJobThread implements Runnable {

        private static final String SCRIPT_LOG_PATH = CFG.HADOOP_JOB_EXECUTE_LOG_FILE_PATH;

        private static final Pattern pattern = Pattern.compile("application_\\d+_\\d+");

        private static final long KEEP_ALIVE_TIME = 1200000L; // 20??????

        private static final long THREAD_START_TIME = System.currentTimeMillis();

        private JobScheduleRepository jobScheduleRepository;

        private JobExecuteLogRepository jobExecuteLogRepository;

        private Integer jobId, executeId;

        private String logPath;

        private RandomAccessFile raf;

        private volatile Long cursor = 0L;

        private Set<String> foundApplicationIds = new HashSet<>();

        KillJobThread(
                JobScheduleRepository jobScheduleRepository,
                JobExecuteLogRepository jobExecuteLogRepository,
                Integer jobId,
                Integer executeId) {
            this.jobScheduleRepository = jobScheduleRepository;
            this.jobExecuteLogRepository = jobExecuteLogRepository;
            this.jobId = jobId;
            this.executeId = executeId;
            try {
                this.logPath = String.format(SCRIPT_LOG_PATH, executeId);
                this.raf = new RandomAccessFile(this.logPath, "r");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        @Transactional
        @Override
        public void run() {
            log.info("kill job ????????????,jobId = " + jobId + " executeId = " + executeId);
            while (System.currentTimeMillis() - THREAD_START_TIME > KEEP_ALIVE_TIME) {
                JobSchedule jobSchedule = jobScheduleRepository.findOne(jobId);
                if (!JobSchedule.JobState.KILLING.getId().equals(jobSchedule.getJobState())) {
                    log.info("?????????kill?????????JobState??????KILLING??????, ????????????kill");
                    break;
                }
                try {
                    log.info(
                            "jobId = " + jobId + " executeId = " + executeId + " ??????????????? " + cursor + " ?????????????????????");
                    raf.seek(cursor);
                    String line = "";
                    while ((line = raf.readLine()) != null) {
                        log.debug("jobId = " + jobId + " executeId = " + executeId + "???????????? = " + line);
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            String applicationId = matcher.group();
                            if (foundApplicationIds.contains(applicationId)) {
                                continue;
                            }
                            foundApplicationIds.add(applicationId);
                            log.info(
                                    "jobId = "
                                            + jobId
                                            + " executeId = "
                                            + executeId
                                            + " applicationId = "
                                            + applicationId);
                            if (!StringUtils.isEmpty(applicationId)) {
                                // ShellUtils.exec(" yarn application -kill " + applicationId);
                                ShellUtils.exec("yarn", "application", "-kill", applicationId);
                            }
                        }
                    }
                    cursor = raf.getFilePointer();
                    Thread.sleep(30000L);
                } catch (Exception e) {
                    throw new RuntimeException(e.toString(), e);
                }
            }
            // 2 ??????t_job_schedule??????
            jobScheduleRepository.updateJobStateByJobId(JobSchedule.JobState.KILLED.getId(), jobId);
            // 3 ??????t_job_execute_log??????
            jobExecuteLogRepository.updateStateById(JobSchedule.JobState.KILLED.getId(), executeId);
            log.info("jobId = " + jobId + " executeId = " + executeId + "?????????kill");
        }
    }
}
