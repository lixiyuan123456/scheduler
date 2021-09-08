package com.naixue.nxdp.web;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.model.User;
import com.naixue.nxdp.service.MonitorChartApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/monitor/chart/api")
public class MonitorChartApiController extends BaseController {

    @Autowired
    private MonitorChartApiService monitorChartApiService;

    @RequestMapping("/dp-data")
    public Object getDpData() {
        Object dpData = monitorChartApiService.getDpData();
        return success("success", "data", dpData);
    }

    //任务运行状态 当前任务运行状态
    @RequestMapping("/run-jobs-status")
    public Object countEachJobRunningStatusNumber(HttpServletRequest request,
                                                  @RequestParam(name = "queryType") Integer angle) {
        User currentUser = getCurrentUser(request);
        MonitorChartApiService.Angle e = MonitorChartApiService.Angle.getEnum(angle);
        Object data = monitorChartApiService.createChartData(MonitorChartApiService.Chart.JOB_RUNNING_STATUS, currentUser, e);
        return success("success", "values", data);
    }

    //任务数量统计 不同类型任务数量统计
    @RequestMapping("/task-type-jobs")
    public Object countEachJobTypeNumber(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        Object data = monitorChartApiService.createChartData(MonitorChartApiService.Chart.JOB_NUMBER_COUNT, currentUser, null);
        return success("success", "taskType", data);
    }

    @RequestMapping("/dp-error-jobs")
    public Object listFailureJobs(HttpServletRequest request,
                                  @RequestParam(name = "queryType") Integer angle) {
        User currentUser = getCurrentUser(request);
        MonitorChartApiService.Angle e = MonitorChartApiService.Angle.getEnum(angle);
        Object data = monitorChartApiService.collectFailureJob(currentUser, e);
        return success("success", "errorJobs", data);
    }

    @RequestMapping("/dp-user-jobs-rank")
    public Object sortUsersByOwnedJobsCount(HttpServletRequest request,
                                            @RequestParam(name = "queryType") Integer angle) {
        User currentUser = getCurrentUser(request);
        MonitorChartApiService.Angle e = MonitorChartApiService.Angle.getEnum(angle);
        Object data = monitorChartApiService.sortUsersByOwnedJobsCount(currentUser, e);
        return success("success", "value", data);
    }

    @RequestMapping("/dp-scheduler-summary")
    public Object countDailyJobNumber() {
        Object data = monitorChartApiService.countDailyJobNumber();
        return success("success", "value", data);
    }

    @RequestMapping("/task-type-radar")
    public Object countEachJobNumber4Radar(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        Object data = monitorChartApiService.countEachJobNumber4Radar(currentUser);
        return success("success", "value", data);
    }

    @RequestMapping("/daily-statistic")
    public Object dailyStatistic() {
        Object dailyStatistic = monitorChartApiService.dailyStatistic();
        return success("success", "value", dailyStatistic);
    }
}
