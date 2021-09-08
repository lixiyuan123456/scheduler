package com.naixue.chord;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

import com.naixue.chord.job.SimpleJob;

public class Application {

    public static void main(String[] args) throws SchedulerException {
        SchedulerFactory factory = new StdSchedulerFactory("quartz.properties");
        Scheduler scheduler = factory.getScheduler();
        // scheduler.start();

        // scheduler.deleteJob(new JobKey("printJob"));

        Trigger trigger3 =
                TriggerBuilder.newTrigger()
                        .withIdentity(new TriggerKey("trigger2"))
                        .withSchedule(CronScheduleBuilder.cronSchedule("0/20 * * * * ? *"))
                        .startNow()
                        .build();

        JobDetail jobDetail3 =
                JobBuilder.newJob(SimpleJob.class).withIdentity(new JobKey("job2")).build();
        scheduler.scheduleJob(jobDetail3, trigger3);
        // scheduler.deleteJob(new JobKey("job1"));
        // scheduler.deleteJob(new JobKey("zhaicc"));
    }
}
