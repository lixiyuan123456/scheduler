package com.naixue.nxdp.util;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.quartz.CronExpression;
import org.quartz.TriggerUtils;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.util.CollectionUtils;

public class CronUtils {

    public static Date nextExecutionTime(String cron) {
        return nextExecutionTime(cron, new Date());
    }

    public static Date nextExecutionTime(String cron, Date from) {
        try {
            CronExpression cronExpression = new CronExpression(cron);
            return cronExpression.getNextValidTimeAfter(from);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static TimeUnit getFireCycleTime(String cron) {
        Date from = new Date();
        Date start = nextExecutionTime(cron, from);
        Date end = nextExecutionTime(cron, start);
        long cycleTime = (end.getTime() - start.getTime()) / 1000;
        if (cycleTime < 60) {
            return TimeUnit.SECONDS;
        } else if (cycleTime >= 60 && cycleTime < 3600) {
            return TimeUnit.MINUTES;
        } else if (cycleTime >= 3600 && cycleTime < 3600 * 24) {
            return TimeUnit.HOURS;
        }
        return TimeUnit.DAYS;
    }

    public static Date lastExecutionTime(String cron) {
        return lastExecutionTime(cron, new Date());
    }

    public static Date lastExecutionTime(String cron, Date end) {
        try {
            Date start = DateUtils.getBeginningOfDay(DateUtils.getDate(end, -7, TimeUnit.DAYS));
            CronTriggerImpl cronTrigger = new CronTriggerImpl();
            cronTrigger.setCronExpression(cron);
            List<Date> fireTimes = TriggerUtils.computeFireTimesBetween(cronTrigger, null, start, end);
            return CollectionUtils.isEmpty(fireTimes) ? null : new LinkedList<>(fireTimes).getLast();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static List<Date> last7DaysExecutionTimes(String cron) {
        try {
            Date start = DateUtils.getBeginningOfDay(DateUtils.getDate(-7, TimeUnit.DAYS));
            Date end = DateUtils.getDate(0, TimeUnit.MINUTES);
            CronTriggerImpl cronTrigger = new CronTriggerImpl();
            cronTrigger.setCronExpression(cron);
            return TriggerUtils.computeFireTimesBetween(cronTrigger, null, start, end);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

  /*public static Date nextExecutionTimeBySpring(String cron) {
    return new CronTrigger(cron).nextExecutionTime(new SimpleTriggerContext());
  }*/
}
