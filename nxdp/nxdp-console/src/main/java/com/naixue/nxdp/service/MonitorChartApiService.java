package com.naixue.nxdp.service;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.naixue.nxdp.dao.DailyStatisticRepository;
import com.naixue.nxdp.dao.JobConfigRepository;
import com.naixue.nxdp.dao.JobExecuteLogRepository;
import com.naixue.nxdp.dao.JobScheduleRepository;
import com.naixue.nxdp.dao.ServerConfigRepository;
import com.naixue.nxdp.dao.UserRepository;
import com.naixue.nxdp.model.DailyStatistic;
import com.naixue.nxdp.model.JobConfig;
import com.naixue.nxdp.model.JobExecuteLog;
import com.naixue.nxdp.model.JobSchedule;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.model.JobConfig.JobType;
import com.naixue.nxdp.model.JobExecuteLog.ExecuteType;
import com.naixue.nxdp.model.JobSchedule.Status;
import com.naixue.nxdp.util.DateUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
public class MonitorChartApiService {

    @Autowired
    private UserRepository userRepository;

    // @Autowired private UserDepartmentRepository userDepartmentRepository;

    @Autowired
    private JobConfigRepository jobConfigRepository;

    @Autowired
    private JobScheduleRepository jobScheduleRepository;

    @Autowired
    private JobExecuteLogRepository jobExecuteLogRepository;

    @Autowired
    private ServerConfigRepository serverConfigRepository;

    @Autowired
    private DailyStatisticRepository dailyStatisticRepository;

    public Object getDpData() {
        Long usersCount = userRepository.count();
        long jobConfigsCount = jobConfigRepository.count();
        long serverConfigsCount = serverConfigRepository.count();
        return Arrays.asList(
                new DpData("用户数", usersCount),
                new DpData("调度任务数", jobConfigsCount),
                new DpData("元数据配置数", serverConfigsCount));
    }

    public Object createChartData(Chart chart, User currentUser, Angle angle) {
        Assert.notNull(chart, "请求参数：chart不允许为空。");
        Assert.notNull(currentUser, "请求参数：currentUser不允许为空。");
        if (chart == Chart.JOB_RUNNING_STATUS) {
            Assert.notNull(angle, "请求参数：angle不允许为空。");
            List<JobSchedule> list = getJobSchedulesByCurrentUser(currentUser, angle);
            return collectEachJobRunningStatusNumber(currentUser.getId(), list);
        }
        if (chart == Chart.JOB_NUMBER_COUNT) {
            List<JobSchedule> list = getJobSchedulesByCurrentUser(currentUser, Angle.PLATFORM);
            return collectEachJobTypeNumber(list, currentUser);
        }
        if (chart == Chart.FAILURE_JOB_LIST) {
            Assert.notNull(angle, "请求参数：angle不允许为空。");
            List<JobSchedule> list = getJobSchedulesByCurrentUser(currentUser, angle);
            return collectFailureJob(list);
        }
        return null;
    }

    public Object countEachJobNumber4Radar(User currentUser) {
        Map<String, Object> result = new HashMap<>();
        List<JobSchedule> list = getJobSchedulesByCurrentUser(currentUser, Angle.PLATFORM);
        List<JobTypeNumber> jobTypeNumbers = collectEachJobTypeNumber(list, currentUser);
        if (CollectionUtils.isEmpty(jobTypeNumbers)) {
            return result;
        }
        List<Map<String, Object>> taskTypeList = new LinkedList<>();
        List<Integer> myTaskList = new LinkedList<>();
        List<Integer> dpTaskList = new LinkedList<>();
        for (JobTypeNumber jobTypeNumber : jobTypeNumbers) {
            if (jobTypeNumber.getAngle() == Angle.PLATFORM) {
                Map<String, Object> taskTypeMap = new HashMap<>();
                taskTypeMap.put("name", jobTypeNumber.getJobType().getName());
                taskTypeMap.put("max", jobTypeNumber.getValue());
                taskTypeList.add(taskTypeMap);
                dpTaskList.add(jobTypeNumber.getValue());
            }
            if (jobTypeNumber.getAngle() == Angle.ME) {
                myTaskList.add(jobTypeNumber.getValue());
            }
        }
        result.put("taskType", taskTypeList);
        result.put("myTask", myTaskList);
        result.put("dpTask", dpTaskList);
        return result;
    }

    public List<DailyStatisticData> dailyStatistic() {
        List<DailyStatisticData> result = new LinkedList<>();
        String start = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ISO_DATE);
        String end = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        List<DailyStatistic> dailyStatistics =
                dailyStatisticRepository.findByDateBetweenOrderByDateAsc(start, end);
        if (CollectionUtils.isEmpty(dailyStatistics)) {
            return result;
        }
        for (DailyStatistic dailyStatistic : dailyStatistics) {

            DailyStatisticData data1 = new DailyStatisticData();
            data1.setName(dailyStatistic.getDate());
            data1.setValue(dailyStatistic.getTotalJobCount());
            data1.setGroup("总任务数");
            data1.setChart(DailyStatisticChart.DAILY_JOB_STATISTIC);
            result.add(data1);

            DailyStatisticData data2 = new DailyStatisticData();
            data2.setName(dailyStatistic.getDate());
            data2.setValue(dailyStatistic.getAutoJobExecTimes());
            data2.setGroup("系统调度次数");
            data2.setChart(DailyStatisticChart.DAILY_JOB_STATISTIC);
            result.add(data2);

            DailyStatisticData data3 = new DailyStatisticData();
            data3.setName(dailyStatistic.getDate());
            data3.setValue(dailyStatistic.getManualJobExecTimes());
            data3.setGroup("手工调度次数");
            data3.setChart(DailyStatisticChart.DAILY_JOB_STATISTIC);
            result.add(data3);

            DailyStatisticData data7 = new DailyStatisticData();
            data7.setName(dailyStatistic.getDate());
            data7.setValue(dailyStatistic.getTotalJobExecTimes());
            data7.setGroup("任务总调度次数");
            data7.setChart(DailyStatisticChart.DAILY_JOB_STATISTIC);
            result.add(data7);

            DailyStatisticData data4 = new DailyStatisticData();
            data4.setName(dailyStatistic.getDate());
            data4.setValue(dailyStatistic.getTotalUserCount());
            data4.setGroup("用户总数");
            data4.setChart(DailyStatisticChart.DAILY_USER_STATISTIC);
            result.add(data4);

            DailyStatisticData data5 = new DailyStatisticData();
            data5.setName(dailyStatistic.getDate());
            data5.setValue(dailyStatistic.getActiveUserCount());
            data5.setGroup("活跃用户");
            data5.setChart(DailyStatisticChart.DAILY_USER_STATISTIC);
            result.add(data5);

            DailyStatisticData data6 = new DailyStatisticData();
            data6.setName(dailyStatistic.getDate());
            data6.setValue(dailyStatistic.getNewUserCount());
            data6.setGroup("新增用户");
            data6.setChart(DailyStatisticChart.DAILY_USER_STATISTIC);
            result.add(data6);

            // List<DailyStatisticData> data = DailyStatisticData.data(chartEnum, dailyStatistic);
            // result.addAll(data);
        }
        return result;
    }

    private List<JobSchedule> getJobSchedulesByCurrentUser(User currentUser, Angle angle) {
        List<JobSchedule> jobSchedules = new ArrayList<>();
        switch (angle) {
            case PLATFORM:
                jobSchedules = jobScheduleRepository.findAllByStatus(Status.ONLINE.getId());
                break;
            case DEPARTMENT:
                jobSchedules =
                        jobScheduleRepository.findAllByStatusAndDeptId(
                                Status.ONLINE.getId(), currentUser.getDeptId());
                break;
            case ME:
                jobSchedules =
                        jobScheduleRepository.findAllByStatusAndUserId(
                                Status.ONLINE.getId(), currentUser.getId());
                break;
            default:
                break;
        }
        return jobSchedules;
    }

    private List<JobRunningStatusNumber> collectEachJobRunningStatusNumber(
            String userId, List<JobSchedule> jobSchedules) {
        List<JobRunningStatusNumber> result = new LinkedList<>();
        Map<JobRunningStatusNumber.RunningStatus, JobRunningStatusNumber> container =
                new LinkedHashMap<>();
        for (MonitorChartApiService.JobRunningStatusNumber.RunningStatus status :
                MonitorChartApiService.JobRunningStatusNumber.RunningStatus
                        .values()) {
            container.put(status, new JobRunningStatusNumber(status));
        }
        result.addAll(container.values());
        if (CollectionUtils.isEmpty(jobSchedules)) {
            return result;
        }
    /*for (JobSchedule.JobState e : JobSchedule.JobState.values()) {
    	JobRunningStatusNumber jobRunningStatusNumber = new JobRunningStatusNumber(e);
    	com.zhuanzhuan.service.MonitorChartApiService.JobRunningStatusNumber.RunningStatus status = jobRunningStatusNumber
    			.getStatus();
    	if (status != null) {
    		container.put(status, jobRunningStatusNumber);
    	}
    }*/
        for (JobSchedule jobSchedule : jobSchedules) {
            MonitorChartApiService.JobRunningStatusNumber.RunningStatus
                    currentRunningStatus = JobRunningStatusNumber.getRunningStatus(jobSchedule);
            container
                    .get(currentRunningStatus)
                    .setValue(container.get(currentRunningStatus).getValue() + 1);
        }
        return result;
    }

    private List<JobTypeNumber> collectEachJobTypeNumber(
            List<JobSchedule> jobSchedules, User currentUser) {
        List<JobTypeNumber> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(jobSchedules)) {
            return result;
        }
        for (Angle angle : Angle.values()) {
            Map<JobConfig.JobType, JobTypeNumber> container = new HashMap<>(50);
            for (JobConfig.JobType e : JobConfig.JobType.values()) {
                container.put(e, new JobTypeNumber(e).setAngle(angle));
            }
            result.addAll(container.values());
            for (JobSchedule jobSchedule : jobSchedules) {
                JobType currentJobType = JobConfig.JobType.getEnum(jobSchedule.getJobType());
                if (angle == Angle.PLATFORM) {
                    container.get(currentJobType).setValue(container.get(currentJobType).getValue() + 1);
                } else if (angle == Angle.DEPARTMENT
                        && jobSchedule.getDeptId().equals(currentUser.getDeptId())) {
                    container.get(currentJobType).setValue(container.get(currentJobType).getValue() + 1);
                } else if (angle == Angle.ME && jobSchedule.getUserId().equals(currentUser.getId())) {
                    container.get(currentJobType).setValue(container.get(currentJobType).getValue() + 1);
                }
            }
        }
        return result;
    }

    @Deprecated
    private List<FailureJob> collectFailureJob(List<JobSchedule> jobSchedules) {
        List<FailureJob> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(jobSchedules)) {
            return result;
        }
        for (JobSchedule jobSchedule : jobSchedules) {
            if (JobSchedule.JobState.FAILURE.getId().equals(jobSchedule.getJobState())) {
                FailureJob failureJob =
                        new FailureJob(
                                jobSchedule.getJobId(),
                                jobSchedule.getJobName(),
                                jobSchedule.getUserId(),
                                jobSchedule.getUserName(),
                                null,
                                null);
                result.add(failureJob);
            }
        }
        return result;
    }

    public List<FailureJob> collectFailureJob(User currentUser, Angle angle) {
        Assert.notNull(currentUser, "请求参数：currentUser不允许为空。");
        Assert.notNull(angle, "请求参数：angle不允许为空。");
        long milli =
                LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0))
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli();
        List<JobExecuteLog> jobExecuteLogs = null;
        switch (angle) {
            case PLATFORM:
                jobExecuteLogs =
                        jobExecuteLogRepository.findByJobStateAndExecuteTimeGreaterThanEqual(
                                JobSchedule.JobState.FAILURE.getId(), new Timestamp(milli));
                break;
            case DEPARTMENT:
                List<String> userIds = getUserIdsInOneDepartment(currentUser);
                if (!CollectionUtils.isEmpty(userIds)) {
                    jobExecuteLogs =
                            jobExecuteLogRepository.findByJobStateAndExecuteTimeGreaterThanEqual(
                                    JobSchedule.JobState.FAILURE.getId(), new Timestamp(milli));
                    Iterator<JobExecuteLog> iterator = jobExecuteLogs.iterator();
                    while (iterator.hasNext()) {
                        if (!userIds.contains(iterator.next().getUserId())) {
                            iterator.remove();
                        }
                    }
                }
                break;
            case ME:
                jobExecuteLogs =
                        jobExecuteLogRepository.findByUserIdAndExecuteTimeGreaterThanEqualAndJobState(
                                currentUser.getId(), new Timestamp(milli), JobSchedule.JobState.FAILURE.getId());
                break;
            default:
                break;
        }
        List<FailureJob> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(jobExecuteLogs)) {
            return result;
        }
        for (JobExecuteLog jobExecuteLog : jobExecuteLogs) {
            ExecuteType executeType = JobExecuteLog.ExecuteType.getEnum(jobExecuteLog.getType());
            FailureJob failureJob =
                    new FailureJob(
                            jobExecuteLog.getJobId(),
                            jobExecuteLog.getJobName(),
                            jobExecuteLog.getUserId(),
                            StringUtils.isEmpty(jobExecuteLog.getUserName())
                                    ? userRepository.findOne(jobExecuteLog.getUserId()).getName()
                                    : jobExecuteLog.getUserName(),
                            executeType == null ? null : executeType.getName(),
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                    .format(new Date(jobExecuteLog.getExecuteTime().getTime())));
            result.add(failureJob);
        }
        return result;
    }

    public LinkedList<UserOwnedJobsNumber> sortUsersByOwnedJobsCount(User currentUser, Angle angle) {
        Assert.notNull(currentUser, "请求参数：currentUser不允许为空。");
        Assert.notNull(angle, "请求参数：angle不允许为空。");
        List<JobSchedule> jobSchedules = null;
        if (angle == Angle.PLATFORM) {
            jobSchedules = jobScheduleRepository.findAllByStatus(JobSchedule.Status.ONLINE.getId());
        } else if (angle == Angle.DEPARTMENT) {
            jobSchedules =
                    jobScheduleRepository.findAllByStatusAndDeptId(
                            JobSchedule.Status.ONLINE.getId(), currentUser.getDeptId());
        }
        LinkedList<UserOwnedJobsNumber> result = new LinkedList<>();
        if (CollectionUtils.isEmpty(jobSchedules)) {
            return result;
        }
        Map<String, UserOwnedJobsNumber> container = mapUserOwnedJobsNumber(currentUser, angle);
        for (JobSchedule jobSchedule : jobSchedules) {
            if (container.containsKey(jobSchedule.getUserId())) {
                UserOwnedJobsNumber userOwnedJobsNumber = container.get(jobSchedule.getUserId());
                userOwnedJobsNumber.setJob_num(userOwnedJobsNumber.getJob_num() + 1);
            }
        }
        if (!CollectionUtils.isEmpty(container)) {
            result.addAll(container.values());
            Collections.sort(result);
        }
        return result;
    }

    public DailyJobNumber countDailyJobNumber() {

        return null;
    }

    private List<String> getUserIdsInOneDepartment(User currentUser) {
        List<String> result = new ArrayList<>();
        List<User> users = userRepository.findByDeptId(currentUser.getDeptId());
        if (CollectionUtils.isEmpty(users)) {
            return result;
        }
        for (User user : users) {
            result.add(user.getId());
        }
        return result;
    }

    /**
     * key:userId
     *
     * @return
     */
    private Map<String, UserOwnedJobsNumber> mapUserOwnedJobsNumber(User currentUser, Angle angle) {
        Map<String, UserOwnedJobsNumber> result = new HashMap<>(100);
        List<User> users = null;
        // String currentUserDepartmentName =
        // userDepartmentRepository.findOne(currentUser.getDeptId()).getName();
        if (Angle.PLATFORM == angle) {
            users = userRepository.findAll();
        } else if (Angle.DEPARTMENT == angle) {
            users = userRepository.findByDeptId(currentUser.getDeptId());
        }
        if (CollectionUtils.isEmpty(users)) {
            return result;
        }
        // Map<Integer, UserDepartment> userDepartmentContainer = new HashMap<>(30);
        for (User user : users) {
      /*if (!userDepartmentContainer.containsKey(user.getDeptId())) {
        userDepartmentContainer.put(
            user.getDeptId(), userDepartmentRepository.findOne(user.getDeptId()));
      }*/
            result.put(
                    user.getId(),
                    new UserOwnedJobsNumber(
                            user.getId(),
                            user.getName(),
                            // userDepartmentContainer.get(user.getDeptId()).getName(),
                            user.getDeptName(),
                            0));
        }
        return result;
    }

    public static enum Angle {
        PLATFORM(1, "平台"),

        DEPARTMENT(2, "部门"),

        ME(3, "我的");

        private Integer id;
        private String name;

        Angle(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public static Angle getEnum(Integer id) {
            for (Angle e : Angle.values()) {
                if (e.getId().equals(id)) {
                    return e;
                }
            }
            return null;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * zzdp首页图表
     *
     * @author zhaichuancheng
     */
    public static enum Chart {
        JOB_RUNNING_STATUS,

        JOB_NUMBER_COUNT,

        FAILURE_JOB_LIST;
    }

    public static enum DailyStatisticChart {
        DAILY_JOB_STATISTIC,
        DAILY_USER_STATISTIC;
    }

    @Data
    @AllArgsConstructor
    private static class DpData {

        private String name;

        private Long value;
    }

    @Data
    private static class JobRunningStatusNumber {

        private RunningStatus status;
        private String name;
        private Integer value = 0;

        JobRunningStatusNumber(RunningStatus status) {
            this.status = status;
        }

        public static RunningStatus getRunningStatus(JobSchedule jobSchedule) {
      /*JobExecuteLog current = null;
      if (!CollectionUtils.isEmpty(jobExecuteLogs)) {
        for (JobExecuteLog e : jobExecuteLogs) {
          if (jobSchedule.getExecuteTime().getTime() == e.getExecuteTime().getTime()) {
            current = e;
          }
        }
      }*/
            if (jobSchedule.getExecuteTime() == null
                    || jobSchedule.getExecuteTime().getTime()
                    < DateUtils.getBeginningOfDay(new Date()).getTime()) {
                return RunningStatus.NULL;
            }
            if (jobSchedule.getJobState() == JobSchedule.JobState.WAITING_SIGNAL.getId()
                    || jobSchedule.getJobState() == JobSchedule.JobState.RUNNING.getId()) {
                return RunningStatus.RUNNING;
            }
            if (jobSchedule.getJobState() == JobSchedule.JobState.SUCCESS.getId()) {
                return RunningStatus.SUCCESS;
            }
            if (jobSchedule.getJobState() == JobSchedule.JobState.FAILURE.getId()) {
                return RunningStatus.FAILURE;
            }
            return null;
        }

        public String getName() {
            switch (getStatus()) {
                case NULL:
                    name = "还没开始";
                    break;
                case RUNNING:
                    name = "正在运行";
                    break;
                case SUCCESS:
                    name = "运行成功";
                    break;
                case FAILURE:
                    name = "运行出错";
                    break;
                default:
                    break;
            }
            return name;
        }

    /*public static RunningStatus getRunningStatus(JobSchedule.JobState jobState) {
      if (jobState == JobSchedule.JobState.VIRGIN) {
        return RunningStatus.NULL;
      }
      if (jobState == JobSchedule.JobState.WAITING_SIGNAL
          || jobState == JobSchedule.JobState.RUNNING) {
        return RunningStatus.RUNNING;
      }
      if (jobState == JobSchedule.JobState.SUCCESS) {
        return RunningStatus.SUCCESS;
      }
      if (jobState == JobSchedule.JobState.FAILURE) {
        return RunningStatus.FAILURE;
      }
      return null;
    }*/

        private static enum RunningStatus {
            NULL,
            RUNNING,
            SUCCESS,
            FAILURE;
        }
    }

    @Data
    private static class JobTypeNumber {

        private JobConfig.JobType jobType;

        private Angle angle;

        private String name;

        private Integer value = 0;

        private String group;

        JobTypeNumber(JobConfig.JobType jobType) {
            this.jobType = jobType;
        }

        public JobTypeNumber setAngle(Angle angle) {
            this.angle = angle;
            return this;
        }

        public String getName() {
            name = jobType.getName();
            return name;
        }

        public String getGroup() {
            group = getAngle().getName();
            return group;
        }
    }

    @Data
    @AllArgsConstructor
    private static class FailureJob {

        private Integer job_id;

        private String job_name;

        private String user_id;

        private String user_name;

        private String run_type;

        private String exec_time;
    }

    @Data
    @AllArgsConstructor
    private static class UserOwnedJobsNumber implements Comparable<UserOwnedJobsNumber> {

        private String user_id;

        private String user_name;

        private String dept_name;

        private Integer job_num = 0;

        @Override
        public int compareTo(UserOwnedJobsNumber o) {
            return o.job_num - this.job_num;
        }
    }

    @Data
    private static class DailyJobNumber {

        private List<Integer> jobs;

        private List<String> dates;

        private List<Integer> manualSchedulerJobs;

        private List<Integer> autoSchedulerJobs;
    }

    @Slf4j
    @Data
    public static class DailyStatisticData {

        private String name;

        private String group;

        private Integer value;

        private DailyStatisticChart chart;

        public static List<DailyStatisticData> data(
                DailyStatisticChart chart, DailyStatistic dailyStatistic) {
            List<DailyStatisticData> result = new LinkedList<>();
            Method[] methods = dailyStatistic.getClass().getDeclaredMethods();
            if (methods != null && methods.length > 0) {
                for (Method method : methods) {
                    DailyStatisticData data =
                            fieldBind(chart, dailyStatistic, dailyStatistic.getDate(), method);
                    if (data != null) {
                        result.add(data);
                    }
                }
            }
            return result;
        }

        private static DailyStatisticData fieldBind(
                DailyStatisticChart chart, DailyStatistic object, String date, Method method) {
            DailyStatisticData data = new DailyStatisticData();
            Integer value = 0;
            try {
                if (chart == DailyStatisticChart.DAILY_JOB_STATISTIC) {
                    if (method.getName().equals("getTotalJobCount")) {
                        data.setGroup("总任务数");
                        value = (Integer) method.invoke(object, "getTotalJobCount");
                    } else if (method.getName().equals("getAutoJobExecTimes")) {
                        data.setGroup("系统调度次数");
                        value = (Integer) method.invoke(object, "getAutoJobExecTimes");
                    } else if (method.getName().equals("getManualJobExecTimes")) {
                        data.setGroup("手动调度次数");
                        value = (Integer) method.invoke(object, "getManualJobExecTimes");
                    } else if (method.getName().equals("getTotalJobExecTimes")) {
                        data.setGroup("总调度次数");
                        value = (Integer) method.invoke(object, "getTotalJobExecTimes");
                    } else {
                        data = null;
                    }
                } else if (chart == DailyStatisticChart.DAILY_USER_STATISTIC) {
                    if (method.getName().equals("getTotalUserCount")) {
                        data.setGroup("用户总数");
                        value = (Integer) method.invoke(object, "getTotalUserCount");
                    } else if (method.getName().equals("getActiveUserCount")) {
                        data.setGroup("活跃用户");
                        value = (Integer) method.invoke(object, "getActiveUserCount");
                    } else if (method.getName().equals("getNewUserCount")) {
                        data.setGroup("新增用户");
                        value = (Integer) method.invoke(object, "getNewUserCount");
                    } else {
                        data = null;
                    }
                }
            } catch (Exception e) {
                log.error(e.toString(), e);
            }
            if (data != null) {
                data.setName(date);
                data.setValue(value);
            }
            return data;
        }
    }
}
