package com.naixue.nxdp.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.naixue.nxdp.dao.DailyStatisticRepository;
import com.naixue.nxdp.dao.JobExecuteLogRepository;
import com.naixue.nxdp.dao.JobScheduleRepository;
import com.naixue.nxdp.dao.UserRepository;
import com.naixue.nxdp.model.DailyStatistic;
import com.naixue.nxdp.model.JobExecuteLog;
import com.naixue.nxdp.model.JobSchedule;

@Service
public class DailyStatisticService {

    @Autowired
    private JobScheduleRepository jobScheduleRepository;

    @Autowired
    private JobExecuteLogRepository jobExecuteLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DailyStatisticRepository dailyStatisticRepository;

    @Transactional
    public void dailyStatistic() {
        String date = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE);
        dailyStatistic(date);
    }

    @Transactional
    public void dailyStatistic(String date) {
        Assert.hasText(date, "请求参数date不允许为空");
        LocalDate day = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        // 计算时间区间
        Instant startInstant =
                LocalDateTime.of(day, LocalTime.of(0, 0, 0)).atZone(ZoneId.systemDefault()).toInstant();
        Instant endInstant =
                LocalDateTime.of(day, LocalTime.of(23, 59, 59)).atZone(ZoneId.systemDefault()).toInstant();
        Date start = Date.from(startInstant);
        Date end = Date.from(endInstant);

        // 计算指标
        Integer totalJobCount = ((Long) jobScheduleRepository.countByCreateTimeLessThanEqual(end)).intValue();

        Integer autoJobExecTimes =
                jobExecuteLogRepository.countByTypeAndExecuteTimeBetween(
                        JobExecuteLog.ExecuteType.AUTO.getId(), start, end);
        Integer manualJobExecTimes =
                jobExecuteLogRepository.countByTypeAndExecuteTimeBetween(
                        JobExecuteLog.ExecuteType.MANUAL.getId(), start, end);
        //总调度次数
        Integer totalJobExecTimes = jobExecuteLogRepository.countDistinctJobIdByExecuteTimeBetween(start, end);


        Integer totalUserCount = Integer.valueOf(userRepository.count() + "");
        // 计算当日活跃用户(当日执行任务的用户+当日创建任务的用户)
        Set<String> activeUserIds = new HashSet<>();
        List<JobSchedule> newJobSchedules = jobScheduleRepository.findByCreateTimeBetween(start, end);
        for (JobSchedule newJobSchedule : newJobSchedules) {
            activeUserIds.add(newJobSchedule.getUserId());
        }
        List<JobExecuteLog> newJobExecuteLogs =
                jobExecuteLogRepository.findByCreateTimeBetween(start, end);
        for (JobExecuteLog newJobExecuteLog : newJobExecuteLogs) {
            activeUserIds.add(newJobExecuteLog.getUserId());
        }
        int activeUserCount = activeUserIds.size();
        // 昨日新增用户数
        int newUserCount = userRepository.findByCreateTimeBetween(start, end).size();

        // 删除DailyStatistic在date日期的数据
        dailyStatisticRepository.deleteByDate(date);

        DailyStatistic data = new DailyStatistic();
        data.setDate(date);
        data.setTotalJobCount(totalJobCount);
        data.setAutoJobExecTimes(autoJobExecTimes);
        data.setManualJobExecTimes(manualJobExecTimes);
        data.setTotalUserCount(totalUserCount);
        data.setActiveUserCount(activeUserCount);
        data.setNewUserCount(newUserCount);
        data.setTotalJobExecTimes(totalJobExecTimes);

        dailyStatisticRepository.save(data);
    }
}
