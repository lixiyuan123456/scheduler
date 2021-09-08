package com.naixue.nxdp.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.model.JobConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.naixue.nxdp.dao.JobConfigRepository;

/**
 * @author: wangyu @Created by 2018/1/29 主要用于各页面之间的跳转
 */
@Controller
@RequestMapping("/workshop")
public class WorkshopController {

    @Autowired
    private JobConfigRepository jobConfigRepository;

    @RequestMapping("/index")
    public String start(HttpServletRequest request) {
        // request.setAttribute("jobTypes", JobConfig.JobType.values());
        return "start";
    }

    /**
     * 跳转到任务列表页
     *
     * @return
     */
    @RequestMapping("/task-list")
    public String taskList() {
        return "taskList";
    }

    /**
     * 跳转到运行历史页
     *
     * @return
     */
    @RequestMapping("/task-run-history")
    public String runHistory() {
        return "taskRunHistory";
    }

    @RequestMapping("/task-data-extract")
    public ModelAndView taskDataExtract(HttpServletRequest request, Integer jobId) {
        return new ModelAndView("add/dataExtract", new ModelMap().addAttribute("jobId", jobId));
    }

    /**
     * 跳转到Hive或Mysql添加页
     *
     * @param type
     * @param jobId
     * @return
     */
    @RequestMapping("/task-add-sql")
    public ModelAndView taskAddSql(
            Integer type, @RequestParam(name = "id", required = false) Integer jobId) {
        Map<String, Object> resultMap = new HashMap<>();
        if (jobId != null) {
            // 获取jobType
            JobConfig jobConfig = jobConfigRepository.findOne(jobId);
            if (jobConfig != null) {
                type = jobConfig.getType();
            }
            resultMap.put("jobId", jobId);
        }
        resultMap.put("type", type);
        resultMap.put("JOB_TYPE", type);
        return new ModelAndView("add/hiveAdd", resultMap);
    }

    /**
     * 跳转到MapReduce添加页
     *
     * @param jobId
     * @return
     */
    @RequestMapping("/task-add-mapreduce")
    public ModelAndView taskAddMapReduce(
            @RequestParam(name = "id", required = false) Integer jobId, Integer type) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("jobId", jobId);
        resultMap.put("type", type);
        return new ModelAndView("add/mapreduceAdd", resultMap);
    }

    /**
     * 跳转到Spark添加页
     *
     * @param jobId
     * @return
     */
    @RequestMapping("/task-add-spark")
    public ModelAndView taskAddSpark(
            @RequestParam(name = "id", required = false) Integer jobId, Integer type) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("jobId", jobId);
        resultMap.put("type", type);
        resultMap.put("JOB_TYPE", type);
        return new ModelAndView("add/sparkAdd", resultMap);
    }

    @RequestMapping("/task-add-shell")
    public String indexShell(
            HttpServletRequest request,
            @RequestParam(name = "id", required = false) Integer jobId,
            @RequestParam(name = "type", required = false) Integer jobType) {
        request.setAttribute("jobId", jobId);
        request.setAttribute("jobType", jobType);
        return "add/shell";
    }

    @RequestMapping("/task-add-shell-ide")
    public String indexShellIde(
            HttpServletRequest request,
            @RequestParam(name = "id", required = false) Integer jobId,
            @RequestParam(name = "type", required = false) Integer jobType) {
        request.setAttribute("jobId", jobId);
        request.setAttribute("jobType", jobType);
        return "add/shell-ide";
    }

    @RequestMapping("/scheduled-job")
    public String scheduledJob(
            HttpServletRequest request,
            @RequestParam(name = "id", required = false) Integer jobId,
            @RequestParam(name = "type", required = false) Integer jobType) {
        request.setAttribute("jobId", jobId);
        request.setAttribute("jobType", jobType);
        return "add/scheduled-job";
    }

    /**
     * 跳转到任务重跑页
     *
     * @return
     */
    @RequestMapping("/task-rerun-list")
    public String taskRerunList() {
        return "taskRerunList";
    }

    /**
     * 跳转到内置变量页
     *
     * @return
     */
    @RequestMapping("/vars")
    public String vars() {
        return "builtin";
    }
}
