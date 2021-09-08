package com.naixue.nxdp.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.naixue.nxdp.config.CFG;
import com.naixue.nxdp.cronjob.JobStartTimePointTimeoutAlarmCronJob;
import com.naixue.nxdp.dao.HdpClusterRepository;
import com.naixue.nxdp.dao.JobConfigRepository;
import com.naixue.nxdp.dao.JobDependenciesRepository;
import com.naixue.nxdp.dao.JobExecuteLogRepository;
import com.naixue.nxdp.dao.JobScheduleRepository;
import com.naixue.nxdp.dao.JobUpdateRecordRepository;
import com.naixue.nxdp.model.HdpCluster;
import com.naixue.nxdp.model.JobConfig;
import com.naixue.nxdp.model.JobConfig.JobType;
import com.naixue.nxdp.model.JobDependencies;
import com.naixue.nxdp.model.JobIO;
import com.naixue.nxdp.model.JobSchedule;
import com.naixue.nxdp.model.JobUpdateRecord;
import com.naixue.nxdp.model.Queue;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.util.CronUtils;
import com.naixue.nxdp.util.SchedulerUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DevTaskService {

    private static final String WEBHDFS_ROOT_PATH = CFG.WEBHDFS_ROOT_PATH;
    @Autowired
    private JobConfigRepository jobConfigRepository;
    @Autowired
    private JobUpdateRecordRepository jobUpdateRecordRepository;
    @Autowired
    private HdpClusterRepository hdpClusterRepository;
    @Autowired
    private JobScheduleRepository jobScheduleRepository;
    @Autowired
    private JobExecuteLogRepository jobExecuteLogRepository;
    @Autowired
    private JobDependenciesRepository jobDependenciesRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private WebHDFSService webHDFSService;
    @Autowired
    private Scheduler scheduler;
    @Autowired
    private JobService jobService;
    @Autowired
    private JobIOService jobIOService;
    @Autowired
    private Executor taskExecutor;

    /**
     * 获取任务的所有更新记录，根据更新时间倒排
     *
     * @param jobId
     * @return
     */
    public List<JobUpdateRecord> getUpdateRecordList(Integer jobId) {
        return jobUpdateRecordRepository.findAllByJobId(
                jobId, new Sort(Sort.Direction.DESC, "updateTime"));
    }

    /**
     * 根据jobId获取job信息
     *
     * @param jobId
     * @return
     */
    public JobConfig getJobConfigByJobId(Integer jobId) {
        return jobConfigRepository.findOne(jobId);
    }

    /**
     * 获取所有集群列表
     *
     * @return
     */
    public List<HdpCluster> getAllHdpClusters() {
        return hdpClusterRepository.findAll();
    }

    /**
     * 保存Mysql或Hive任务信息 同时保存在t_job_config表和t_job_schedule表 需要考虑事务
     *
     * @param jobConfig 任务信息
     * @param currUser  当前操作用户
     * @return
     */
    @Transactional
    public Map<String, Object> saveSQLJob(JobConfig jobConfig, User currUser) {
        Map<String, Object> resultMap = new HashMap<>();
        // 1. 判断任务名是否已存在，2表示正常状态，99表示删除
        checkJobName(jobConfig.getJobName(), jobConfig.getId());

        // 2. 解析sql语句并存储到HDFS
        JSONObject detailJson = JSONObject.parseObject(jobConfig.getDetails());
        if (detailJson == null) {
            resultMap.put("status", "error");
            resultMap.put("msg", "任务信息解析失败");
            return resultMap;
        }
        String sql = detailJson.getString("sql");
        String filepath = "";
        String revision = "";
        if (sql != null && sql.trim().length() > 0) {
            filepath = WEBHDFS_ROOT_PATH + "/sql/" + jobConfig.getJobName() + ".sql";
            revision = UUID.randomUUID().toString();
            // 2.1 保存SQL文件
            webHDFSService.uploadScriptStringToWebHDFS(
                    WEBHDFS_ROOT_PATH + "/sql/", jobConfig.getJobName() + ".sql" + "@" + revision, sql);
            resultMap.put("revision", revision);
            detailJson.put("revision", revision);
            detailJson.put("path", filepath);
            jobConfig.setDetails(JSONObject.toJSONString(detailJson));
        }

        // 3. 解析依赖任务列表
        String dependencies = detailJson.getString("dependencies");
        Integer[] dependIdArr = parseDependence(dependencies);

        // 4. 保存job信息
        if (jobConfig.getId() == null) {
            // 4.1 新增job
            Integer jobId = addJob(jobConfig, currUser, dependIdArr);
            resultMap.put("id", jobId);
        } else {
            // 4.2 更新已有job
            updateJob(jobConfig, currUser, dependIdArr);
        }

        // 5. 插入更新记录
        String operatorId = currUser.getId();
        String operatorName = currUser.getName();
        JobUpdateRecord jobUpdateRecord =
                new JobUpdateRecord(
                        jobConfig.getId(),
                        operatorId,
                        operatorName,
                        jobConfig.getJobName(),
                        filepath,
                        revision,
                        Timestamp.valueOf(LocalDateTime.now()));
        jobUpdateRecordRepository.save(jobUpdateRecord);

        resultMap.put("status", "ok");
        return resultMap;
    }

    /**
     * 修改编辑SQL的锁状态
     *
     * @param jobId        任务ID
     * @param codeEditLock 编辑锁的状态
     * @param user         操作用户
     * @return
     */
    public Map<String, Object> editSql(Integer jobId, Integer codeEditLock, User user) {
        Map<String, Object> resultMap = new HashMap<>();
        String status = "ok";
        // 修改任务编辑锁状态
        Integer code =
                jobConfigRepository.updateEditLock(codeEditLock, user.getId(), user.getPyName(), jobId);
        if (code == 0) {
            status = "error";
            resultMap.put("msg", "任务不存在");
        } else {
            // 获取任务最新SQL
            JobConfig jobConfig = jobConfigRepository.findOne(jobId);
            if (jobConfig == null) {
                status = "error";
                resultMap.put("msg", "任务不存在");
            } else {
                JSONObject detailJson = JSONObject.parseObject(jobConfig.getDetails());
                if (detailJson != null) {
                    resultMap.put("sql", detailJson.getString("sql"));
                }
            }
        }
        resultMap.put("status", status);
        return resultMap;
    }

    public Object editShell(Integer jobId, Integer codeEditLock, User user) {
        // 修改任务编辑锁状态
        jobConfigRepository.updateEditLock(codeEditLock, user.getId(), user.getPyName(), jobId);
        // 获取任务最新SQL
        JobConfig jobConfig = jobConfigRepository.findOne(jobId);
        if (jobConfig == null) {
            throw new RuntimeException("jobId=" + jobId + "任务不存在");
        }
        return JSON.parseObject(jobConfig.getDetails());
    }

    /**
     * 保存需要需要上传执行文件的任务信息
     *
     * @param jobConfig 任务信息
     * @param currUser  当前操作用户
     * @return
     */
    @Transactional
    public Map<String, Object> saveFileJob(JobConfig jobConfig, User currUser) {
        Map<String, Object> resultMap = new HashMap<>();
        // 1. 判断任务名是否已存在
        checkJobName(jobConfig.getJobName(), jobConfig.getId());

        // 2. 保存从json解析出来的信息
        Integer[] dependIdArr = {};
        String filepath = "";
        String revision = "";
        String fileName = "";
        Integer uploadFlag = 0;

        JSONObject detailJson = JSONObject.parseObject(jobConfig.getDetails());
        if (detailJson != null) {
            filepath = detailJson.getString("filePath");
            fileName = detailJson.getString("fileName");
            uploadFlag = detailJson.getInteger("uploadFlag");
            // 2.1 解析依赖任务列表
            String dependencies = detailJson.getString("dependencies");
            dependIdArr = parseDependence(dependencies);
        }

        // 3. 保存job信息
        if (jobConfig.getId() == null) {
            // 3.1 新建job
            Integer jobId = addJob(jobConfig, currUser, dependIdArr);
            resultMap.put("id", jobId);
        } else {
            // 3.2 更新已有job
            updateJob(jobConfig, currUser, dependIdArr);
        }

        // 4. 插入更新记录
        if (uploadFlag == 1) {
            String operatorId = currUser.getId();
            String operatorName = currUser.getName();
            JobUpdateRecord jobUpdateRecord =
                    new JobUpdateRecord(
                            jobConfig.getId(),
                            operatorId,
                            operatorName,
                            fileName,
                            filepath,
                            revision,
                            Timestamp.valueOf(LocalDateTime.now()));
            jobUpdateRecordRepository.save(jobUpdateRecord);
        }

        resultMap.put("status", "ok");
        return resultMap;
    }

    @Transactional
    public Integer saveShellJob(User currentUser, JobConfig jobConfig) {
        // 1. 判断任务名是否已存在，2表示正常状态，99表示删除
        checkJobName(jobConfig.getJobName(), jobConfig.getId());
        // 2. 解析shell语句并存储到HDFS
        JSONObject details = JSONObject.parseObject(jobConfig.getDetails());
        String scripts = details.getString("scripts");
        if (StringUtils.isEmpty(scripts)) {
            throw new RuntimeException("脚本内容不允许为空");
        }
        String revision = UUID.randomUUID().toString();
        String filepath = WEBHDFS_ROOT_PATH + "/shell/" + jobConfig.getJobName() + ".sh";
        webHDFSService.uploadScriptStringToWebHDFS(
                WEBHDFS_ROOT_PATH + "/shell/", jobConfig.getJobName() + ".sh", scripts);
        details.put("revision", revision);
        details.put("path", filepath);
        jobConfig.setDetails(JSONObject.toJSONString(details));
        // 3. 解析依赖任务列表
        String dependencies = details.getString("dependencies");
        Integer[] dependIdArr = parseDependence(dependencies);
        Integer jobId = 0;
        // 4. 保存job信息
        if (jobConfig.getId() == null) {
            // 4.1 新增job
            jobId = addJob(jobConfig, currentUser, dependIdArr);
        } else {
            // 4.2 更新已有job
            updateJob(jobConfig, currentUser, dependIdArr);
            jobId = jobConfig.getId();
        }
        // 5. 插入更新记录
        String operatorId = currentUser.getId();
        String operatorName = currentUser.getName();
        JobUpdateRecord jobUpdateRecord =
                new JobUpdateRecord(
                        jobConfig.getId(),
                        operatorId,
                        operatorName,
                        jobConfig.getJobName(),
                        filepath,
                        revision,
                        Timestamp.valueOf(LocalDateTime.now()));
        jobUpdateRecordRepository.save(jobUpdateRecord);
        return jobId;
    }

    @Transactional
    public Integer saveScheduledJob(User currentUser, JobConfig jobConfig) {
        checkJobName(jobConfig.getJobName(), jobConfig.getId());
        JSONObject details = JSONObject.parseObject(jobConfig.getDetails());
        String scripts = details.getString("scripts");
        Assert.hasText(scripts, "脚本内容不允许为空");
        // 3. 解析依赖任务列表
        String dependencies = details.getString("dependencies");
        Integer[] dependIdArr = parseDependence(dependencies);
        Integer jobId = 0;
        // 4. 保存job信息
        if (jobConfig.getId() == null) {
            // 4.1 新增job
            jobId = addJob(jobConfig, currentUser, dependIdArr);
        } else {
            // 4.2 更新已有job
            updateJob(jobConfig, currentUser, dependIdArr);
            jobId = jobConfig.getId();
        }
        // 5. 插入更新记录
        String operatorId = currentUser.getId();
        String operatorName = currentUser.getName();
        JobUpdateRecord jobUpdateRecord =
                new JobUpdateRecord(
                        jobConfig.getId(),
                        operatorId,
                        operatorName,
                        jobConfig.getJobName(),
                        "",
                        "",
                        Timestamp.valueOf(LocalDateTime.now()));
        jobUpdateRecordRepository.save(jobUpdateRecord);
        return jobId;
    }

    /**
     * 上传文件
     *
     * @param jobFile 文件信息
     * @return
     */
    public Map<String, Object> uploadFiles(MultipartFile jobFile) throws IOException {
        Map<String, Object> resultMap = new HashMap<>();
        if (jobFile.isEmpty()) {
            resultMap.put("result", "false");
            resultMap.put("errorMsg", "文件不能为空");
            return resultMap;
        }
        String fileName = jobFile.getOriginalFilename();
        String fileSize = new DecimalFormat("#.#").format(jobFile.getSize() / 1000.0 / 1000);
        String groupId = null;
        InputStream inputStream = jobFile.getInputStream();
        String filePath =
                webHDFSService.uploadFileStreamToWebHDFS(
                        WEBHDFS_ROOT_PATH + "/jar/", fileName, inputStream);
        log.debug("文件上传成功，路径=" + filePath);
    /*try {
      String filePath =
          FileUploadUtils.uploadFileStreamToWebHDFS(
              WEBHDFS_ROOT_PATH + "/jar/", fileName, inputStream);
      log.debug("文件上传成功，路径=" + filePath);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }*/
        resultMap.put("result", "true");
        resultMap.put("newFileName", filePath.replace(WEBHDFS_ROOT_PATH + "/jar/", ""));
        resultMap.put("filePath", filePath);
        resultMap.put("fileName", fileName);
        resultMap.put("groupId", groupId);
        resultMap.put("filesize", fileSize);
        return resultMap;
    }

    public void checkJobName(String jobName, Integer jobId) {
        JobConfig queryJobConfig =
                jobConfigRepository.findFirst1ByJobNameEqualsAndStatusIn(jobName, new Integer[]{2});
        // 如果任务存在并且和当前任务ID不同
        if (queryJobConfig != null && !queryJobConfig.getId().equals(jobId)) {
            throw new RuntimeException("任务名称已存在");
        }
    }

    private void parseIOConfig(final JobConfig source, final JobSchedule target) {
        if (source == null || target == null) {
            return;
        }
        if (StringUtils.isEmpty(source.getDetails())) {
            return;
        }
        JSONObject jsonObject = JSON.parseObject(source.getDetails());
        JSONObject output = jsonObject.getJSONObject("out");
        if (output != null) {
            target.setOutMode(output.getInteger("mode") == null ? 0 : output.getInteger("mode"));
            target.setOutValue(output.getString("value") == null ? "" : output.getString("value"));
        }
    }

    public Integer addJob(JobConfig jobConfig, User currUser, Integer[] dependIdArr) {
        User chargeUser = userService.getUserByUserId(currUser.getId());
        // JobSchedule jobSchedule = ModelUtils.castConfigToSchedule(jobConfig, chargeUser, currUser);
        // 3. 插入新任务
        jobConfig.setCreated(Timestamp.valueOf(LocalDateTime.now()));
        jobConfig.setUpdated(Timestamp.valueOf(LocalDateTime.now()));
        jobConfig.setStatus(2);
        jobConfig.setEditerId(currUser.getId());
        jobConfig.setEditerName(currUser.getPyName());
        // 插入到t_job_config表
        final JobConfig finalJobConfig = jobConfigRepository.save(jobConfig);
        // 插入到t_job_schedule表
        final JobSchedule jobSchedule = castConfigToSchedule(finalJobConfig, chargeUser, currUser);
        jobSchedule.setJobId(finalJobConfig.getId());
        parseIOConfig(finalJobConfig, jobSchedule);
        jobScheduleRepository.save(jobSchedule);
        taskExecutor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        if (jobSchedule != null
                                && jobSchedule.getOutMode() != null
                                && jobSchedule.getOutValue() != null) {
                            JobIO.Type type = JobIO.Type.OUTPUT;
                            jobIOService.parseConfigToJobIOs(
                                    finalJobConfig.getId(),
                                    type,
                                    JobIO.Mode.getEnum(jobSchedule.getOutMode()),
                                    jobSchedule.getOutValue());
                        }
                    }
                });
        // 开启任务执行时间点超时报警
        if (JobSchedule.Status.ONLINE.getId().equals(jobSchedule.getStatus())
                && jobSchedule.getDelayAlarm() == JobSchedule.DelayAlarm.ON
                && !SchedulerUtils.exisit(scheduler, String.valueOf(jobSchedule.getJobId()))) {
            jobService.cron(jobSchedule);
        }
        // 4. 插入依赖信息到依赖表
        if (dependIdArr.length > 0) {
            List<JobDependencies> jobDependenciesList = new ArrayList<>(dependIdArr.length);
            for (Integer dependId : dependIdArr) {
                JobDependencies jobDependencies = new JobDependencies(jobConfig.getId(), dependId);
                jobDependenciesList.add(jobDependencies);
            }
            if (jobDependenciesList.size() > 0) {
                // 保存任务依赖信息
                jobDependenciesRepository.save(jobDependenciesList);
            }
        }
        return jobConfig.getId();
    }

    public void updateJob(JobConfig jobConfig, User currUser, Integer[] dependIdArr) {
        User chargeUser = userService.getUserByUserId(jobConfig.getUserId());
        // JobSchedule jobSchedule = ModelUtils.castConfigToSchedule(jobConfig, chargeUser, currUser);
        // 5. 更新已有任务
        // 更新t_job_config
    /*jobConfigRepository.update(
    jobConfig.getJobName(),
    2,
    jobConfig.getUserId(),
    jobConfig.getDescription(),
    jobConfig.getTags(),
    jobConfig.getDetails(),
    jobConfig.getId());*/
        JobConfig originJobConfig = jobConfigRepository.findOne(jobConfig.getId());
        originJobConfig.setJobName(jobConfig.getJobName());
        originJobConfig.setUserId(jobConfig.getUserId());
        originJobConfig.setDescription(jobConfig.getDescription());
        originJobConfig.setTags(jobConfig.getTags());
        originJobConfig.setDetails(jobConfig.getDetails());
        originJobConfig.setEditerId(currUser.getId());
        originJobConfig.setEditerName(currUser.getPyName());
        // originJobConfig.setOutputMode(jobConfig.getOutputMode());
        // originJobConfig.setOutputModeValue(jobConfig.getOutputModeValue());
        final JobConfig finalJobConfig = jobConfigRepository.save(originJobConfig);
        final JobSchedule jobSchedule = castConfigToSchedule(jobConfig, chargeUser, currUser);
        jobSchedule.setJobId(finalJobConfig.getId());
        parseIOConfig(finalJobConfig, jobSchedule);
        jobScheduleRepository.save(jobSchedule);
        taskExecutor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        if (jobSchedule != null
                                && jobSchedule.getOutMode() != null
                                && jobSchedule.getOutValue() != null) {
                            JobIO.Type type = JobIO.Type.OUTPUT;
                            jobIOService.parseConfigToJobIOs(
                                    finalJobConfig.getId(),
                                    type,
                                    JobIO.Mode.getEnum(jobSchedule.getOutMode()),
                                    jobSchedule.getOutValue());
                        }
                    }
                });
        // 更新t_job_schedule
    /*jobScheduleRepository.update(
    jobSchedule.getDeptId(),
    jobSchedule.getUserId(),
    jobSchedule.getUserName(),
    jobSchedule.getJobName(),
    jobSchedule.getJobDesc(),
    jobSchedule.getStatus(),
    jobSchedule.getJobType(),
    jobSchedule.getDispatchCommand(),
    jobSchedule.getRunTime(),
    jobSchedule.getJobLevel(),
    jobSchedule.getIsMonitor(),
    jobSchedule.getParallelRun(),
    jobSchedule.getErrorRunContinue(),
    jobSchedule.getRetry(),
    jobSchedule.getReceiver(),
    jobSchedule.getHadoopQueueId(),
    jobSchedule.getDelayWarn(),
    jobSchedule.getUpdateId(),
    jobSchedule.getUpdateName(),
    jobSchedule.getSparkVersion(),
    jobSchedule.getNextFireTime(),
    jobSchedule.getScheduleLevel(),
    jobSchedule.getJobId());*/
        // 开启或关闭任务执行时间点超时报警
        if (JobSchedule.Status.OFFLINE.getId().equals(jobSchedule.getStatus())
                || jobSchedule.getDelayAlarm() == JobSchedule.DelayAlarm.OFF) {
            SchedulerUtils.deleteJob(scheduler, String.valueOf(jobSchedule.getJobId()));
        }
        if (JobSchedule.Status.ONLINE.getId().equals(jobSchedule.getStatus())
                && jobSchedule.getDelayAlarm() == JobSchedule.DelayAlarm.ON
                && !SchedulerUtils.exisit(scheduler, String.valueOf(jobSchedule.getJobId()))) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put(
                    JobStartTimePointTimeoutAlarmCronJob.JOB_SCHEDULE_REPOSITORY, jobScheduleRepository);
            dataMap.put(
                    JobStartTimePointTimeoutAlarmCronJob.JOB_EXECUTE_LOG_REPOSITORY, jobExecuteLogRepository);
            dataMap.put(JobStartTimePointTimeoutAlarmCronJob.USER_SERVICE, userService);
            SchedulerUtils.addJob(
                    scheduler,
                    JobStartTimePointTimeoutAlarmCronJob.class,
                    String.valueOf(jobSchedule.getJobId()),
                    jobSchedule.getRunTime(),
                    dataMap);
        }
        // 更新依赖关系
        List<JobDependencies> newDependList = new ArrayList<>(dependIdArr.length);
        for (Integer dependId : dependIdArr) {
            JobDependencies jobDependencies = new JobDependencies(jobConfig.getId(), dependId);
            newDependList.add(jobDependencies);
        }
        List<JobDependencies> oldDependList = jobDependenciesRepository.findByJobId(jobConfig.getId());
        if (oldDependList.size() <= newDependList.size()) {
            for (int i = 0; i < oldDependList.size(); i++) {
                newDependList.get(i).setId(oldDependList.get(i).getId());
            }
            if (newDependList.size() > 0) {
                jobDependenciesRepository.save(newDependList);
            }
        } else {
            List<JobDependencies> deleteJobDependList = new ArrayList<>();
            for (int i = 0; i < oldDependList.size(); i++) {
                if (i < newDependList.size()) {
                    newDependList.get(i).setId(oldDependList.get(i).getId());
                } else {
                    deleteJobDependList.add(oldDependList.get(i));
                }
            }
            jobDependenciesRepository.delete(deleteJobDependList);
            if (newDependList.size() > 0) {
                jobDependenciesRepository.save(newDependList);
            }
        }
    }

    public Integer[] parseDependence(String dependencies) {
        Integer[] dependIdArr = {};
        if (dependencies != null && dependencies.length() > 0) {
            String[] arr = dependencies.split(",");
            dependIdArr = new Integer[arr.length];
            for (int i = 0; i < arr.length; i++) {
                dependIdArr[i] = Integer.valueOf(arr[i]);
            }
        }
        return dependIdArr;
    }

    public JobSchedule castConfigToSchedule(JobConfig jobConfig, User chargeUser, User updateUser) {
        if (jobConfig == null) {
            return null;
        }
        JSONObject detailJson = JSONObject.parseObject(jobConfig.getDetails());
        if (detailJson == null) {
            detailJson = new JSONObject();
        }
        JSONObject scheduleJson = JSONObject.parseObject(detailJson.getString("scheduler"));
        if (scheduleJson == null) {
            scheduleJson = new JSONObject();
        }
        JobSchedule jobSchedule = jobScheduleRepository.findOne(jobConfig.getId());
        if (jobSchedule == null) {
            jobSchedule = new JobSchedule();
            jobSchedule.setJobState(JobSchedule.JobState.VIRGIN.getId());
        }
        jobSchedule.setJobId(jobConfig.getId());
        jobSchedule.setDeptId(chargeUser.getDeptId());
        jobSchedule.setUserId(jobConfig.getUserId());
        jobSchedule.setUserName(chargeUser.getName());
        jobSchedule.setJobName(jobConfig.getJobName());
        jobSchedule.setJobDesc(jobConfig.getDescription());
        jobSchedule.setStatus(scheduleJson.getInteger("status"));
        jobSchedule.setJobType(jobConfig.getType());
        jobSchedule.setDispatchCommand("");
        jobSchedule.setRunTime(scheduleJson.getString("runTime"));
        if (!StringUtils.isEmpty(scheduleJson.getString("runTime"))
                && JobSchedule.Status.ONLINE.getId().equals(jobSchedule.getStatus())) {
            jobSchedule.setNextFireTime(CronUtils.nextExecutionTime(scheduleJson.getString("runTime")));
            // 禁止小时级以下的任务保存
            TimeUnit cycleTime = CronUtils.getFireCycleTime(scheduleJson.getString("runTime"));
            if (cycleTime == TimeUnit.SECONDS) {
                throw new RuntimeException("目前不支持秒级任务，请重新配置CRON表达式");
            }
            if (cycleTime == TimeUnit.MINUTES) {
                throw new RuntimeException("目前不支持分钟级任务，请重新配置CRON表达式");
            }
        }
        jobSchedule.setJobLevel(scheduleJson.getInteger("jobLevel"));
        jobSchedule.setIsMonitor(scheduleJson.getInteger("isMonitor"));
        jobSchedule.setParallelRun(scheduleJson.getInteger("parallelRun"));
        jobSchedule.setErrorRunContinue(scheduleJson.getInteger("errorRunContinue"));
        jobSchedule.setRetry(scheduleJson.getInteger("retry"));
        if (jobSchedule.getRetry() == null) {
            jobSchedule.setRetry(0);
        }
        if (jobSchedule.getRetry() > 3) {
            throw new RuntimeException("重跑次数限制最大值3次");
        }
        jobSchedule.setRetryTimeSpan(scheduleJson.getInteger("retryTimeSpan"));
        if (jobSchedule.getRetryTimeSpan() == null) {
            jobSchedule.setRetryTimeSpan(0);
        }
        if (jobSchedule.getRetryTimeSpan() > 1800) {
            throw new RuntimeException("重跑时间间隔最大值1800秒");
        }
        jobSchedule.setReceiver(scheduleJson.getString("receiver"));
        jobSchedule.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
        jobSchedule.setUpdateTime(Timestamp.valueOf(LocalDateTime.now()));

        if (JobType.SPARK_STREAMING.getId() == jobConfig.getType()) {
            Queue queue = userService.getBindingQueue(chargeUser, JobType.getEnum(jobConfig.getType()));
            jobSchedule.setHadoopQueueId(queue.getId());
        } else {
            jobSchedule.setHadoopQueueId(scheduleJson.getInteger("hadoopQueueId"));
        }
        jobSchedule.setDelayWarn(scheduleJson.getInteger("delayWarn"));
        jobSchedule.setIsDelay(0);
        jobSchedule.setUpdateId(updateUser.getId());
        jobSchedule.setUpdateName(updateUser.getPyName());
        jobSchedule.setSparkVersion(detailJson.getString("sparkVersion"));
        return jobSchedule;
    }
}
