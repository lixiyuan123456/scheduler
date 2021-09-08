package com.naixue.nxdp.prework;

import com.naixue.nxdp.service.DailyStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
public class DailyStatisticScheduler {

    @Autowired
    private DailyStatisticService dailyStatisticService;

    @Scheduled(cron = "0 0 1 * * ?")
    void dailyStatistic() {
        log.info("------>DailyStatisticScheduler执行开始");
        dailyStatisticService.dailyStatistic();
        log.info("------>DailyStatisticScheduler执行结束");
    }
}
