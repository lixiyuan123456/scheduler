package com.naixue.nxdp.prework;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.naixue.nxdp.service.JobService;
import com.naixue.nxdp.websocket.JobLogReaderWebSocketClient;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.naixue.nxdp.config.CFG;
import com.naixue.nxdp.dao.mapper.JobExecuteLogMapper;
import com.naixue.nxdp.model.JobConfig;
import com.naixue.nxdp.model.JobExecuteLog;
import com.naixue.nxdp.model.JobSchedule;
import com.naixue.nxdp.util.YarnUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SparkStreamingRunningStateListener implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("SparkStreamingRunningStatusListener is beginning......");
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        JobExecuteLogMapper jobExecuteLogMapper =
                (JobExecuteLogMapper) jobDataMap.get(JobExecuteLogMapper.class.getName());
        Executor taskExecutor = (Executor) jobDataMap.get(Executor.class.getName());
        JobService jobService = (JobService) jobDataMap.get(JobService.class.getName());
        List<JobExecuteLog> jobExecuteLogs =
                jobExecuteLogMapper.findJobExecuteLogs(
                        JobConfig.JobType.SPARK_STREAMING, JobSchedule.JobState.RUNNING);
        if (CollectionUtils.isEmpty(jobExecuteLogs)) {
            return;
        }
        for (JobExecuteLog jobExecuteLog : jobExecuteLogs) {
            taskExecutor.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String url =
                                        String.format(
                                                CFG.WEBSOCKET_HADOOP_JOB_EXECUTE_LOG_READER_URL + UUID.randomUUID(),
                                                jobExecuteLog.getTargetServer());
                                new SyncSparkStreamingRunningState(
                                        url, jobExecuteLog.getJobId(), jobExecuteLog.getId(), jobService);
                            } catch (Exception e) {
                                throw new RuntimeException(e.toString(), e);
                            }
                        }
                    });
        }
    }

    @Slf4j
    public static class SyncSparkStreamingRunningState extends JobLogReaderWebSocketClient {

        private static final Pattern pattern = Pattern.compile("application_\\d+_\\d+");
        private Integer jobId;
        private Integer jobExecuteId;
        private JobService jobService;

        public SyncSparkStreamingRunningState(
                final String webSocketUrl,
                final Integer jobId,
                final Integer jobExecuteId,
                final JobService jobService)
                throws URISyntaxException {
            super(new URI(webSocketUrl), jobExecuteId + "");
            this.jobId = jobId;
            this.jobExecuteId = jobExecuteId;
            this.jobService = jobService;
        }

        @Override
        public void onMessage(String message) {
            try {
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        new ByteArrayInputStream(message.getBytes()), Charset.defaultCharset()));
                String line = "";
                String applicationId = null;
                while (!StringUtils.isEmpty(line = reader.readLine())) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        applicationId = matcher.group();
                        break;
                    }
                }
                if (StringUtils.isEmpty(applicationId)) {
                    throw new RuntimeException("No applicationId is found");
                }
                String re =
                        YarnUtils.queryApplicationStateById(CFG.YARN, applicationId);
                if (StringUtils.isEmpty(re)) {
                    throw new RuntimeException("Yarn rest api returns nothing");
                }
                JSONObject json = JSON.parseObject(re);
                String state = json.getString("state");
                if (state.equals("FAILED")) { // 运行失败
                    log.info("Spark streaming[applicationId={}] is failed!!!", applicationId);
                    jobService.syncJobState(jobId, jobExecuteId, JobSchedule.JobState.FAILURE);
                    log.info("任务[jobId={}]任务状态同步到{}!!!", jobId, JobSchedule.JobState.FAILURE.getId());
                } else if (state.equals("KILLED")) { // KILLED
                    log.info("Spark streaming[applicationId={}] is killed!!!", applicationId);
                    jobService.syncJobState(jobId, jobExecuteId, JobSchedule.JobState.KILLED);
                    log.info("任务[jobId={}]任务状态同步到{}!!!", jobId, JobSchedule.JobState.KILLED.getId());
                } else { // 运行中
                    log.info("Spark streaming[applicationId={}] is running!!!", applicationId);
                }
            } catch (Exception e) {
                throw new RuntimeException(e.toString(), e);
            }
        }
    }
}
