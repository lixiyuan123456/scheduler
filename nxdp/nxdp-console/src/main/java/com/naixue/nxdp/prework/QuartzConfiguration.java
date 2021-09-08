package com.naixue.nxdp.prework;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfiguration {

    @Bean
    public SchedulerFactory getSchedulerFactory() {
        return new StdSchedulerFactory();
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public Scheduler getScheduler(SchedulerFactory schedulerFactory) throws SchedulerException {
        return schedulerFactory.getScheduler();
    }
}
