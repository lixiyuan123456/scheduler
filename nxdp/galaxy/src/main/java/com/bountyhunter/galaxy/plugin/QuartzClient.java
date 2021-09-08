package com.bountyhunter.galaxy.plugin;

import org.apache.curator.framework.CuratorFramework;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzClient {

    private static final String ZK_HOST = "10.126.98.197:2181";

    private static CuratorFramework zkClient;

    static {
        zkClient = ZkClient.newClient(ZK_HOST);
        zkClient.start();
    }

    public static void main(String[] args) throws Exception {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        for (int i = 0; i < 5; i++) {
            final String id = "quartz_" + i;
            scheduler.scheduleJob(
                    JobBuilder.newJob(SimpleJob.class).withIdentity(id).build(),
                    TriggerBuilder.newTrigger()
                            .withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * * * ? *"))
                            .build());
        }
    }

    public static class SimpleJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println(context.getJobDetail().getKey().getName());
        }
    }
}
