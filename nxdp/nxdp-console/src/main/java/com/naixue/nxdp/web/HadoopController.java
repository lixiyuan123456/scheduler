package com.naixue.nxdp.web;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.model.JobConfig;
import com.naixue.nxdp.model.Queue;
import com.naixue.nxdp.service.HadoopService;
import com.naixue.nxdp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.naixue.nxdp.dao.JobConfigRepository;
import com.naixue.nxdp.dao.QueueRepository;

/**
 * @author: wangyu @Created by 2018/1/30
 */
@Controller
public class HadoopController extends BaseController {

    @Autowired
    private HadoopService hadoopService;
    @Autowired
    private QueueRepository queueRepository;
    @Autowired
    private JobConfigRepository jobConfigRepository;

    @Autowired
    private UserService userService;

    /**
     * 任务编辑页-获取重要等级列表
     *
     * @return
     */
    @RequestMapping("getJobLevelListJson")
    @ResponseBody
    public Object getJobLevelListJson() {
        // 获取重要等级列表
        List<Map<String, Object>> jobLevels = hadoopService.getJobLevelList();
        return JSON.toJSONString(jobLevels);
    }

    /**
     * 根据部门ID获取队列信息, 暂时获取所有队列信息
     *
     * @param deptId
     * @return
     */
    @RequestMapping("/hadoop/api/getHadoopQueueByDept")
    @ResponseBody
    public Object getHadoopQueueByDept(
            @RequestParam(name = "deptId", required = false) Integer deptId) {
        // 获取指定部门的队列信息
        List<Queue> queues = queueRepository.findAll();
        return JSON.toJSONString(queues);
    }

    @ResponseBody
    @RequestMapping("/hadoop/api/getDefaultHadoopQueue")
    public Object getDefaultHadoopQueue(HttpServletRequest request, Integer jobId, Integer jobType) {
        if (jobId != null) {
            JobConfig jobConfig = jobConfigRepository.findOne(jobId);
            if (jobConfig != null) {
                jobType = jobConfig.getType();
            }
        }
        return userService.getBindingQueue(getCurrentUser(request), JobConfig.JobType.getEnum(jobType));
    }

    /**
     * 跳转到不同的编辑页面
     *
     * @param jobId
     * @return
     */
    @RequestMapping("/dev/add/edit")
    public ModelAndView toEdit(@RequestParam(name = "id") Integer jobId) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("id", jobId);
        // 获取jobType
        JobConfig jobConfig = jobConfigRepository.findOne(jobId);
        JobConfig.JobType jobType = JobConfig.JobType.getEnum(jobConfig.getType());
        switch (jobType) {
            case DATA_EXTRACT_SCRIPT:
                modelAndView.setViewName("redirect:/workshop/task-data-extract");
                break;
            case MYSQL_SCRIPT:
                modelAndView.setViewName("redirect:/workshop/task-add-sql");
                break;
            case HIVE:
                modelAndView.setViewName("redirect:/workshop/task-add-sql");
                break;
            case SPARK_SQL:
                modelAndView.setViewName("redirect:/workshop/task-add-sql");
                break;
            case MAPREDUCE:
                modelAndView.setViewName("redirect:/workshop/task-add-mapreduce");
                break;
            case SHELL:
                modelAndView.setViewName("redirect:/workshop/task-add-shell");
                break;
            case SPARK:
                modelAndView.setViewName("redirect:/workshop/task-add-spark");
                break;
            case SPARK_STREAMING:
                modelAndView.setViewName("redirect:/workshop/task-add-spark");
                break;
            case SHELL_IDE:
                modelAndView.setViewName("redirect:/workshop/task-add-shell-ide");
                break;
            case SCHEDULED_JOB:
                modelAndView.setViewName("redirect:/workshop/scheduled-job");
                break;
            default:
                break;
        }
        return modelAndView;
    }
}
