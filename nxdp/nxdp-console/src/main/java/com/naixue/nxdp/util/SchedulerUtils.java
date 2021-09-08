package com.naixue.nxdp.util;

import java.util.Map;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SchedulerUtils {

    public static <T extends Job> void addJob(
            Scheduler scheduler, Class<T> clazz, String key, String cron, Map<String, Object> dataMap) {
        try {
            JobBuilder jobBuilder = JobBuilder.newJob(clazz).withIdentity(new JobKey(key));
            if (dataMap != null) {
                jobBuilder.usingJobData(new JobDataMap(dataMap));
            }
            JobDetail jobDetail = jobBuilder.build();
            Trigger trigger =
                    TriggerBuilder.newTrigger()
                            .withIdentity(new TriggerKey(key))
                            .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                            .build();
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("添加任务：" + "jobKey=" + key + "\t" + "cron=" + cron);
        } catch (SchedulerException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static void deleteJob(Scheduler scheduler, String key) {
        try {
            scheduler.deleteJob(new JobKey(key));
            log.info("删除任务：" + "jobKey=" + key + "\t");
        } catch (SchedulerException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static boolean exisit(Scheduler scheduler, String key) {
        try {
            return scheduler.checkExists(new JobKey(key));
        } catch (SchedulerException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static void main(String[] args) throws Exception {
        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();
        SchedulerUtils.addJob(scheduler, SimpleJob.class, "111111111111", "0/1 * * * * ? *", null);
        SchedulerUtils.addJob(scheduler, SimpleJob.class, "222222222222", "0/3 * * * * ? *", null);
        Thread.sleep(10000);
        SchedulerUtils.deleteJob(scheduler, "abc");
    }

    public static class SimpleJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println(context.getJobDetail().getKey());
        }
    }
}
