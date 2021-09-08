package com.naixue.nxdp.web;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.model.JobDependencies;
import com.naixue.nxdp.model.JobExecuteLog;
import com.naixue.nxdp.model.JobSchedule;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.service.OldPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.naixue.nxdp.dao.JobDependenciesRepository;
import com.naixue.nxdp.dao.JobExecuteLogRepository;
import com.naixue.nxdp.dao.JobScheduleRepository;

/**
 * @author: wangyu @Created by 2018/1/29
 */
@Controller
@RequestMapping("/old-page/dev/task")
public class OldPageController extends BaseController {

    @Autowired
    private OldPageService oldPageService;

    @Autowired
    private JobScheduleRepository jobScheduleRepository;

    @Autowired
    private JobDependenciesRepository jobDependenciesRepository;

    @Autowired
    private JobExecuteLogRepository jobExecuteLogRepository;

    /**
     * 将指定任务的修改为指定状态
     *
     * @param jobId
     * @param status
     * @return
     */
    @RequestMapping("/changeSchedulerStatusAction")
    @ResponseBody
    public Object changeSchedulerStatusAction(
            @RequestParam(name = "schedulerId") Integer jobId,
            @RequestParam(name = "schedulerStatus") Integer status) {
        // 修改任务状态，1：上线，2：下线，3：删除
        oldPageService.changeJobStatus(jobId, status);
        return success("data", "true");
    }

    /**
     * 判断指定任务是否被依赖
     *
     * @param jobId
     * @return
     */
    @RequestMapping("/checkJobHaveChilds")
    @ResponseBody
    public String checkJobHaveChilds(Integer jobId) {
        if (jobId == null) {
            return "false";
        }
        // 判断该任务是否被其它任务所依赖
        // boolean dependentFlag = jobDependenciesRepository.existsByDependentJobIdEquals(jobId);
        List<JobDependencies> dependencies = jobDependenciesRepository.findByDependentJobId(jobId);
        if (!CollectionUtils.isEmpty(dependencies)) {
            for (JobDependencies jd : dependencies) {
                Integer childJobId = jd.getJobId();
                JobSchedule schedule = jobScheduleRepository.findByJobId(childJobId);
                if (schedule.getStatus() == JobSchedule.Status.ONLINE.getId()) {
                    return "true";
                }
            }
        }
        return "false";
    }

    @ResponseBody
    @RequestMapping("/checkJobHaveChildsByDevId")
    public Object checkJobHaveChildsByDevId(Integer id) {
        return checkJobHaveChilds(id);
    }

    /**
     * 手动执行一个job，默认设置为等待信号状态
     *
     * @param jobId
     * @param detail
     * @param request
     * @return
     */
    @RequestMapping("/runJobAloneByManualAction")
    @ResponseBody
    public String runJobAloneByManualAction(
            HttpServletRequest request,
            @RequestParam(name = "schedulerId") Integer jobId,
            String detail) {
        User user = getCurrentUser(request);
        // 从detail中解析运行时间
        Timestamp chooseRunTime = null;
        JSONObject detailJson = JSONObject.parseObject(detail);
        if (detailJson != null) {
            chooseRunTime = detailJson.getTimestamp("runDateTime");
        }
        String result = oldPageService.runJobManual(jobId, chooseRunTime, user);
        return result;
    }

    @ResponseBody
    @RequestMapping("/runJobAloneWithTimeSpan.do")
    public Object runJobAloneWithTimeSpan(
            HttpServletRequest request,
            @RequestParam(name = "schedulerId") Integer jobId,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
        User currentUser = getCurrentUser(request);
        oldPageService.runJobWithTimeSpan(currentUser, jobId, startDate, endDate);
        return success();
    }

    /**
     * 手动kill掉一个任务
     *
     * @param jobId
     * @param executeId
     * @return
     */
    @RequestMapping("/killRunningJob")
    @ResponseBody
    public Object killRunningJob(
            @RequestParam(name = "schedulerId") Integer jobId,
            @RequestParam(name = "excuteId") Integer executeId)
            throws Exception {
        oldPageService.killRunningJob(jobId, executeId);
        return "true";
    }

    /**
     * 获取当前任务的所有直接或间接子任务
     *
     * @param jobId
     * @return
     */
    @RequestMapping("/getJobAllChildsByJson")
    @ResponseBody
    public String getJobAllChildsByJson(Integer jobId) {
        List<Map<String, Object>> childList = oldPageService.getJobAllChildren(jobId);
        Map<String, Object> map = new HashMap<>();
        map.put("jobId", jobId);
        childList.add(map);
        return JSON.toJSONString(JSON.toJSONString(childList));
    }

    /**
     * 重跑指定的执行记录
     *
     * @param request
     * @param executeId
     * @return
     */
    @RequestMapping("/reRunJobAloneByManualAction")
    @ResponseBody
    public Object reRunJobAloneByManualAction(HttpServletRequest request, Integer executeId) {

        User user = getCurrentUser(request);
        JobExecuteLog executeLog = jobExecuteLogRepository.findOne(executeId);
        String result =
                oldPageService.runJobManual(executeLog.getJobId(), executeLog.getChooseRunTime(), user);
        return result;
    }

    /**
     * 获取该job下正在等待的所有执行记录
     */
    @RequestMapping("/getPendingJobs")
    @ResponseBody
    public Object getPendingJobs(@RequestParam(name = "schedulerId") Integer jobId) {
        List<JobExecuteLog> jobExecuteLogList =
                jobExecuteLogRepository.findAllByJobIdAndJobState(jobId, 1);
        return jobExecuteLogList;
    }

    /**
     * 停止正在等待信号的任务
     *
     * @param executeId
     * @return todo 一期不支持kill掉未处于运行状态的任务
     */
    @RequestMapping("/stopPendingJob")
    @ResponseBody
    public Object stopPendingJob(@RequestParam(name = "excuteId") Integer executeId)
            throws Exception {
        // JobExecuteLog executeLog = jobExecuteLogRepository.findOne(executeId);
        // oldPageService.killRunningJob(executeLog.getJobId(), executeId);
        // return "true";
        throw new IllegalAccessException("目前不支持kill处于等待信号的任务");
    }
}
