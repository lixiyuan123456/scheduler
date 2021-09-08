package com.naixue.chord.job;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SimpleJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String ip = "";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) { // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String name = context.getJobDetail().getKey().getName();
        String fireTime = fireTime(context.getTrigger().getNextFireTime());
        switch (name) {
            case "job1":
                System.out.println(now() + "###" + fireTime + "###" + ip + name);
                break;
            case "job2":
                System.out.println(now() + "###" + fireTime + "###" + ip + name);
                break;
            default:
                System.out.println(now() + "###" + fireTime + "###" + ip + "other");
                break;
        }
    }

    public String now() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(new Date());
    }

    public String fireTime(Date fireTime) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(fireTime);
    }
}
