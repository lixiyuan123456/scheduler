package com.naixue.chord;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

public class Main {

    public static void main(String[] args) throws SchedulerException {
        SchedulerFactory factory = new StdSchedulerFactory("quartz.properties");
        Scheduler scheduler = factory.getScheduler();
        System.out.println("Starting..................." + scheduler.getSchedulerName());
        scheduler.start();
        scheduler.deleteJob(new JobKey("a"));
        scheduler.deleteJob(new JobKey("a"));
        scheduler.deleteJob(new JobKey("a"));
        scheduler.deleteJob(new JobKey("a"));
    }
}
