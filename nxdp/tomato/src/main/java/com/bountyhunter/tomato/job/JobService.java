package com.bountyhunter.tomato.job;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class JobService {

    public void newJob() throws Exception {
        JobDetail job1 = JobBuilder.newJob(MonitorJob.class).withIdentity(new JobKey("job1")).build();
        CronTrigger trigger1 =
                TriggerBuilder.newTrigger()
                        .withSchedule(CronScheduleBuilder.cronSchedule("0/3 * * * * ?"))
                        .build();
        JobDetail job2 = JobBuilder.newJob(MonitorJob.class).withIdentity(new JobKey("job2")).build();
        CronTrigger trigger2 =
                TriggerBuilder.newTrigger()
                        .withSchedule(CronScheduleBuilder.cronSchedule("0/12 * * * * ?"))
                        .build();
        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.scheduleJob(job1, trigger1);
        scheduler.scheduleJob(job2, trigger2);
        scheduler.start();
        Thread.sleep(30000);
        scheduler.deleteJob(new JobKey("job1"));
    }

    public static class MonitorJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDetail jobDetail = context.getJobDetail();
            System.out.println(jobDetail.getKey());
        }
    }
}
