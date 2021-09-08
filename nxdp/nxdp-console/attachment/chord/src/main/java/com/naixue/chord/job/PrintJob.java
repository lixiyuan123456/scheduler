package com.naixue.chord.job;

import java.util.List;
import java.util.Set;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;

public class PrintJob implements Job {

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      System.out.println("PrintJob Start.................");
      Scheduler scheduler = context.getScheduler();
      List<String> groupNames = context.getScheduler().getJobGroupNames();
      for (String groupName : groupNames) {
        Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName));
        for (JobKey jobKey : jobKeys) {
          // log.error("======" + scheduler.getJobDetail(jobKey).getJobClass());
          System.out.println("======" + scheduler.getJobDetail(jobKey).getJobClass());
        }
      }
    } catch (SchedulerException e) {
      e.printStackTrace();
    }
  }
}
