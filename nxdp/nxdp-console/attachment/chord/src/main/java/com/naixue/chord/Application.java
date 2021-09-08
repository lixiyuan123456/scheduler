package com.naixue.chord;

import java.util.Arrays;

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

  public static void main1(String[] args) throws SchedulerException {
    SchedulerFactory factory = new StdSchedulerFactory("quartz.properties");
    Scheduler scheduler = factory.getScheduler();
    // scheduler.start();

    // scheduler.deleteJob(new JobKey("printJob"));

    Trigger trigger3 =
        TriggerBuilder.newTrigger()
            .withIdentity(new TriggerKey("trigger6"))
            .withSchedule(CronScheduleBuilder.cronSchedule("0/15 * * * * ?"))
            .startNow()
            .build();

    JobDetail jobDetail3 =
        JobBuilder.newJob(SimpleJob.class).withIdentity(new JobKey("job6")).build();
    scheduler.scheduleJob(jobDetail3, trigger3);

    scheduler.deleteJob(new JobKey("job3"));
    scheduler.deleteJob(new JobKey("job4"));
    // scheduler.deleteJob(new JobKey("job1"));
    // scheduler.deleteJob(new JobKey("zhaicc"));
  }

  public static void main(String[] args) {
    String[] array = new String[] {"a", "b", "c"};
    System.out.println(Arrays.toString(array));
  }
}
