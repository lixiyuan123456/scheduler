package com.naixue.nxdp.web;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.service.RerunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.naixue.nxdp.dao.JobExecuteLogRepository;
import com.naixue.nxdp.dao.JobRerunConfigRepository;
import com.naixue.nxdp.dao.JobRerunDetailRepository;
import com.naixue.nxdp.model.JobExecuteLog;
import com.naixue.nxdp.model.JobRerunConfig;
import com.naixue.nxdp.model.RerunSearchParam;
import com.naixue.nxdp.model.SearchForm;
import com.naixue.nxdp.model.SearchParam;
import com.naixue.nxdp.model.User;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: wangyu
 * @Created by 2018/1/31
 **/
@Controller
@Slf4j
@RequestMapping("/scheduler/task-rerun")
public class RerunController extends BaseController {

    @Autowired
    private RerunService rerunService;
    @Autowired
    private JobRerunConfigRepository jobRerunConfigRepository;
    @Autowired
    private JobRerunDetailRepository jobRerunDetailRepository;
    @Autowired
    private JobExecuteLogRepository jobExecuteLogRepository;

    private static Specification<JobRerunConfig> getJobRerunConfigSpecification(final SearchParam searchParam) {
        return new Specification<JobRerunConfig>() {
            @Override
            public Predicate toPredicate(Root<JobRerunConfig> root, CriteriaQuery<?> criteriaQuery,
                                         CriteriaBuilder criteriaBuilder) {
                Predicate predicate = null;
                SearchForm searchForm = searchParam.getSearchForm();
                if (searchForm != null) {
                    Integer type = searchForm.getType();
                    if (type != null && type != -1) {
                        predicate = criteriaBuilder.equal(root.get("type"), type);
                    }
                    String userId = searchForm.getUserId();
                    if (userId != null && userId.trim().length() > 0 && !"-1".equals(userId)) {
                        if (predicate == null) {
                            predicate = criteriaBuilder.equal(root.get("userId"), userId);
                        } else {
                            predicate = criteriaBuilder.and(predicate,
                                    criteriaBuilder.equal(root.get("userId"), userId));
                        }
                    }
                    Integer status = searchForm.getStatus();
                    if (status != null && status != -1) {
                        if (predicate == null) {
                            predicate = criteriaBuilder.equal(root.get("status"), status);
                        } else {
                            predicate = criteriaBuilder.and(predicate,
                                    criteriaBuilder.equal(root.get("status"), status));
                        }
                    }
                }
                return predicate;
            }
        };
    }

    /**
     * 提交一键重跑任务
     *
     * @param request
     * @param jobIds      所有重跑任务列表
     * @param runTime     指定运行时间
     * @param beginJobIds 首个job信息
     * @return
     */
    @RequestMapping("/api/submit")
    @ResponseBody
    public Object submit(HttpServletRequest request, String jobIds, String runTime, String beginJobIds) {

        Map<String, Object> resultMap = new HashMap<>();
        try {
            User user = getCurrentUser(request);
            Timestamp chooseRunTime = null;
            try {
                chooseRunTime = Timestamp.valueOf(runTime);
            } catch (IllegalArgumentException e1) {
            }
            if (chooseRunTime == null) {
                chooseRunTime = new Timestamp(System.currentTimeMillis());
            }
            resultMap = rerunService.submitRerunJobs(jobIds, chooseRunTime, beginJobIds, user);
        } catch (Exception e) {
            resultMap.put("status", "failure");
            resultMap.put("msg", "重跑任务失败!请重新刷新试试，并联系管理员");
            log.error("/api/submit", e);
        }
        return resultMap;
    }

    /**
     * 查看重跑任务明细，如果任务未提交，则进入job列表编辑页面，否则进入job运行状态列表
     *
     * @param searchParam 包括任务id
     * @return
     */
    @RequestMapping("/detail")
    public ModelAndView getRerunDetail(RerunSearchParam searchParam) {
        // 根据taskId查询任务信息
        Integer taskId = searchParam.getId();

        Map<String, Object> resultMap = rerunService.jobRerunDetail(taskId);
        Integer status = (Integer) resultMap.get("status");
        if (status == 0) {
            return new ModelAndView("taskRerunDetail", resultMap);
        } else if (status == 1) {
            return new ModelAndView("taskRerunningDetail", resultMap);
        }
        return null;
    }

    /**
     * 获取重跑任务列表
     *
     * @param searchParam 搜索条件
     * @return
     */
    @RequestMapping("/api/list")
    @ResponseBody
    public Object taskRerunList1(SearchParam searchParam) {
        // 查询指定条件的历史记录总数
        Pageable pageable = new PageRequest(searchParam.getStart() / searchParam.getLength(), searchParam.getLength(),
                Sort.Direction.DESC, "createTime");
        Page<JobRerunConfig> jobList = jobRerunConfigRepository.findAll(getJobRerunConfigSpecification(searchParam),
                pageable);
        // end 测试数据
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("recordsFiltered", jobList.getTotalElements()); // 返回搜索条件查询结果数
        resultMap.put("start", searchParam.getStart());
        resultMap.put("pageSize", searchParam.getLength());
        resultMap.put("draw", searchParam.getDraw()); // 返回前端ajax请求顺序标识数（可能同时存在多个ajax请求，而无法判断dataTable和后端数据的一一映射关系，所以用draw判断）
        resultMap.put("data", jobList.getContent());
        resultMap.put("recordsTotal", jobList.getTotalElements());
        return resultMap;
    }

    /**
     * 删除重跑任务中的某一个job
     *
     * @param id 重跑job id
     * @return
     */
    @RequestMapping("/api/remove-job")
    @ResponseBody
    public Object removeJob(Integer id) {
        Map<String, Object> resultMap = new HashMap<>();
        String status = "success";
        String msg = "";
        // 更改job的状态，将job逻辑删除
        try {
            Integer code = jobRerunDetailRepository.changeOperationTypeById(-1, id);
            if (code != 1) {
                status = "failure";
                msg = "移除job失败!请重新刷新试试，并联系管理员";
            }
        } catch (Exception e) {
            log.error("/api/remove-job", e);
            status = "failure";
            msg = "移除job失败!请重新刷新试试，并联系管理员";
        }
        resultMap.put("status", status);
        resultMap.put("msg", msg);
        return resultMap;
    }

    /**
     * 恢复重跑任务中的某一个job
     *
     * @param id 重跑job id
     * @return
     */
    @RequestMapping("/api/recover-job")
    @ResponseBody
    public Object recoverJob(Integer id) {
        String status = "success";
        String msg = "";
        // 更改job的状态，将job逻辑恢复
        try {
            Integer code = jobRerunDetailRepository.changeOperationTypeById(0, id);
            if (code == 0) {
                status = "failure";
                msg = "恢复job失败!请重新刷新试试，并联系管理员";
            }
        } catch (Exception e) {
            log.error("/api/recover-job", e);
            status = "failure";
            msg = "移除job失败!请重新刷新试试，并联系管理员";
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", status);
        resultMap.put("msg", msg);
        return resultMap;
    }

    /**
     * 删除选中的所有job
     *
     * @param ids job id列表，以逗号分隔
     * @return
     */
    @RequestMapping("/api/remove-job-all")
    @ResponseBody
    public Object removeJobAll(String ids) {
        String status = "success";
        String msg = "";
        // 更改job的状态，将job逻辑恢复
        if (ids == null || ids.trim().length() == 0) {
            status = "failure";
            msg = "移除job失败!请重新刷新试试，并联系管理员";
        } else {
            try {
                String[] arr = ids.split(",");
                List<Integer> idList = new ArrayList<>();
                for (String s : arr) {
                    idList.add(Integer.valueOf(s));
                }
                Integer code = jobRerunDetailRepository.changeOperationTypeByIdIn(-1, idList);
                if (code == 0) {
                    status = "failure";
                    msg = "移除job失败!请重新刷新试试，并联系管理员";
                }
            } catch (Exception e) {
                status = "failure";
                msg = "移除job失败!请重新刷新试试，并联系管理员";
                log.error("/api/remove-job-all", e);
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", status);
        resultMap.put("msg", msg);
        return resultMap;
    }

    /**
     * 删除taskId重跑任务下，除ids以外的所有job
     *
     * @param ids
     * @param taskId
     * @return
     */
    @RequestMapping("/api/keep-job-all")
    @ResponseBody
    public Object keepJobAll(String ids, Integer taskId) {
        Map<String, Object> resultMap = new HashMap<>(2);
        try {
            resultMap = rerunService.keepJobAll(ids, taskId);
        } catch (Exception e) {
            resultMap.put("status", "failure");
            resultMap.put("msg", "移除job失败!请重新刷新试试，并联系管理员");
            log.error("/api/keep-job-all", e);
        }
        return resultMap;
    }

    /**
     * 重跑Task详情页（已提交）- Kill全部筛选结果
     *
     * @param executeIds
     * @param jobIds
     * @return
     */
    @RequestMapping("/api/cancel-all-jobs")
    @ResponseBody
    public Object cancelAllJobs(@RequestParam("eIds") String executeIds, @RequestParam("sIds") String jobIds) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            resultMap = rerunService.cancelAllJobs(executeIds, jobIds);
        } catch (Exception e) {
            resultMap.put("status", false);
            resultMap.put("statusText", e.getMessage());
            log.error("/api/cancel-all-jobs", e);
        }
        return resultMap;
    }

    /**
     * 获取任务下所有job的执行记录
     *
     * @param taskId
     * @return
     */
    @RequestMapping("/api/fetch-task-execute-logs")
    @ResponseBody
    public Object fetchTaskExecuteLogs(@RequestParam("id") Integer taskId) {
        // 查询该taskId的所有任务信息
        List<JobExecuteLog> executeLogList = jobExecuteLogRepository.findAllByTaskId(taskId);
        Map<String, JobExecuteLog> executeLogMap = new HashMap<>();
        for (JobExecuteLog jobExecuteLog : executeLogList) {
            executeLogMap.put(String.valueOf(jobExecuteLog.getJobId()), jobExecuteLog);
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", "success");
        resultMap.put("taskExecuteLogs", executeLogMap);
        return resultMap;
    }

    /**
     * 重跑Task详情页 - 提交该Task
     *
     * @param taskId
     * @param request
     * @return
     */
    @RequestMapping("/api/submit-task-done")
    @ResponseBody
    public Object submitTaskDone(HttpServletRequest request, @RequestParam("id") Integer taskId) {
        Map<String, Object> resultMap = new HashMap<>();
        // 获取用户信息
        try {
            User user = getCurrentUser(request);
            resultMap = rerunService.submitTask(taskId, user);
        } catch (Exception e) {
            resultMap.put("status", "failure");
            resultMap.put("msg", "提交Task失败!请重新刷新试试，并联系管理员");
            log.error("/api/submit-task-done", e);
        }
        return resultMap;
    }
}
