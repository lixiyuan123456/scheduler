package com.naixue.nxdp.web;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.naixue.nxdp.model.JobExecuteLog;
import com.naixue.nxdp.model.JobSchedule;
import com.naixue.nxdp.model.SearchForm;
import com.naixue.nxdp.model.SearchParam;
import com.naixue.nxdp.service.ScheduleService;
import com.naixue.nxdp.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.naixue.nxdp.dao.JobExecuteLogRepository;
import com.naixue.nxdp.dao.JobScheduleRepository;

/**
 * @author: wangyu @Created by 2018/1/29
 */
@Controller
@RequestMapping("/scheduler/task")
public class ScheduleController extends BaseController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private JobExecuteLogRepository jobExecuteLogRepository;
    @Autowired
    private JobScheduleRepository jobScheduleRepository;

    private static Specification<JobExecuteLog> getJobExecuteLogSpec(final SearchParam searchParam) {
        return new Specification<JobExecuteLog>() {
            @Override
            public Predicate toPredicate(
                    Root<JobExecuteLog> root,
                    CriteriaQuery<?> criteriaQuery,
                    CriteriaBuilder criteriaBuilder) {
                Predicate predicate = null;
                SearchForm searchForm = searchParam.getSearchForm();
                if (searchForm == null) {
                    return null;
                }
                // 默认查询当天的执行记录
                if (StringUtils.isEmpty(searchForm.getCreateDt())) {
                    searchForm.setCreateDt(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                }
                try {
                    Date createDate = new SimpleDateFormat("yyyy-MM-dd").parse(searchForm.getCreateDt());
                    predicate =
                            criteriaBuilder.greaterThanOrEqualTo(
                                    root.get("createTime"), new Timestamp(createDate.getTime()));
                    Date endDate = DateUtils.getEndingOfDay(createDate);
                    predicate =
                            criteriaBuilder.and(
                                    predicate,
                                    criteriaBuilder.lessThan(
                                            root.get("createTime"), new Timestamp(endDate.getTime())));
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                // 判断任务名是否为空
                if (!StringUtils.isEmpty(searchForm.getJobName())) {
                    predicate =
                            criteriaBuilder.and(
                                    predicate,
                                    criteriaBuilder.like(root.get("jobName"), "%" + searchForm.getJobName() + "%"));
                }
                // 判断重要等级是否为-1
                if (searchForm.getJobLevel() != null && searchForm.getJobLevel() != -1) {
                    predicate =
                            criteriaBuilder.and(
                                    predicate, criteriaBuilder.equal(root.get("jobLevel"), searchForm.getJobLevel()));
                }
                // 判断运行状态是否为-1
                if (searchForm.getJobState() != null && searchForm.getJobState() != -1) {
                    predicate =
                            criteriaBuilder.and(
                                    predicate, criteriaBuilder.equal(root.get("jobState"), searchForm.getJobState()));
                }
                // 判断用户名是否为空
                if (searchForm.getUserId() != null
                        && !"-1".equals(searchForm.getUserId())
                        && searchForm.getUserId().trim().length() > 0) {
                    predicate =
                            criteriaBuilder.and(
                                    predicate, criteriaBuilder.equal(root.get("userId"), searchForm.getUserId()));
                }
                // 判断调度级别
                if (searchForm.getScheduleLevel() != null && searchForm.getScheduleLevel() != -1) {
                    Predicate equal =
                            criteriaBuilder.equal(
                                    root.get("jobSchedule").get("scheduleLevel"), searchForm.getScheduleLevel());
                    predicate =
                            predicate == null
                                    ? criteriaBuilder.and(equal)
                                    : criteriaBuilder.and(predicate, equal);
                }
                return predicate;
            }
        };
    }

    @RequestMapping("/api/search-task-run-history")
    @ResponseBody
    public Object searchHistoryTask(SearchParam searchParam) {
        // 获取指定条件的任务明细 及 记录总数
        Pageable pageable =
                new PageRequest(
                        searchParam.getStart() / searchParam.getLength(),
                        searchParam.getLength(),
                        Sort.Direction.DESC,
                        "createTime");
        Page<JobExecuteLog> data =
                jobExecuteLogRepository.findAll(getJobExecuteLogSpec(searchParam), pageable);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("recordsFiltered", data.getTotalElements()); // 返回搜索条件查询结果数
        resultMap.put("start", searchParam.getStart());
        resultMap.put("pageSize", searchParam.getLength());
        resultMap.put(
                "draw",
                searchParam
                        .getDraw()); // 返回前端ajax请求顺序标识数（可能同时存在多个ajax请求，而无法判断dataTable和后端数据的一一映射关系，所以用draw判断）
        resultMap.put("data", data.getContent());
        resultMap.put("recordsTotal", data.getTotalElements()); // 返回没有任何过滤条件的总数
        return resultMap;
    }

    /**
     * 查询可以依赖的任务
     *
     * @param searchParam 搜索参数
     * @return
     */
    @RequestMapping("/api/search-schedule-task")
    @ResponseBody
    public Object searchScheduleTask(SearchParam searchParam) {
        SearchForm searchForm = searchParam.getSearchForm();
        int totalSize = 0;
        List<JobSchedule> jobs = new ArrayList<>();
        // 获取指定条件的任务明细
        if (searchForm.getJobName() != null && searchForm.getJobName().trim().length() > 0) {
            Pageable pageable =
                    new PageRequest(
                            searchParam.getStart() / searchParam.getLength(),
                            searchParam.getLength(),
                            Sort.Direction.DESC,
                            "createTime");
            Page<JobSchedule> dataList =
          /*jobScheduleRepository.findAllByStatusInAndJobNameContains(
          new Integer[] {0, 1, 2}, searchForm.getJobName(), pageable);*/
                    jobScheduleRepository.findSelectableJobSchedules(
                            new Integer[]{0, 1, 2}, "%" + searchForm.getJobName() + "%", pageable);
            totalSize = (int) dataList.getTotalElements();
            jobs = dataList.getContent();
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("recordsFiltered", totalSize); // 返回搜索条件查询结果数
        resultMap.put("start", searchParam.getStart());
        resultMap.put("pageSize", searchParam.getLength());
        resultMap.put(
                "draw",
                searchParam
                        .getDraw()); // 返回前端ajax请求顺序标识数（可能同时存在多个ajax请求，而无法判断dataTable和后端数据的一一映射关系，所以用draw判断）
        resultMap.put("data", jobs);
        resultMap.put("recordsTotal", totalSize); // 返回没有任何过滤条件的总数
        return resultMap;
    }

    /**
     * 获取当前任务的依赖任务信息
     *
     * @param searchParam 搜索条件
     * @return
     */
    @RequestMapping("/api/fetch-dependent-jobs")
    @ResponseBody
    public Object fetchDependentJobs(SearchParam searchParam) {
        SearchForm searchForm = searchParam.getSearchForm();
        int totalSize = 0;
        List<JobSchedule> jobs = new ArrayList<>();
        Integer jobId = searchForm.getJobTaskId();
        Page<JobSchedule> jobList = scheduleService.getDependentJobs(jobId);
        if (jobList != null) {
            totalSize = (int) jobList.getTotalElements();
            jobs = jobList.getContent();
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("recordsFiltered", totalSize); // 返回搜索条件查询结果数
        resultMap.put("start", searchParam.getStart());
        resultMap.put("pageSize", searchParam.getLength());
        resultMap.put(
                "draw",
                searchParam
                        .getDraw()); // 返回前端ajax请求顺序标识数（可能同时存在多个ajax请求，而无法判断dataTable和后端数据的一一映射关系，所以用draw判断）
        resultMap.put("data", jobs);
        resultMap.put("recordsTotal", totalSize); // 返回没有任何过滤条件的总数
        return resultMap;
    }

    /**
     * 查询任务信息，并跳转到详情页
     *
     * @param jobId
     * @return
     */
    @RequestMapping("/detail")
    public ModelAndView taskDetail(@RequestParam(name = "schedulerId") int jobId) {
        // 获取job的详细信息
        JobSchedule jobSchedule = jobScheduleRepository.findOne(jobId);
        List<JobExecuteLog> jobExecuteLogList =
                jobExecuteLogRepository.findTop50ByJobId(
                        jobId, new Sort(Sort.Direction.DESC, "createTime"));

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("schedule", jobSchedule);
        resultMap.put("executes", jobExecuteLogList);
        return new ModelAndView("taskDetail", resultMap);
    }

    /**
     * 获取依赖关系图
     *
     * @param jobId
     * @param isSimple 0为分析模式，1为简单模式
     * @return
     */
    @RequestMapping("/api/getDependentRelationsAction")
    @ResponseBody
    public Object getDependentRelationsAction(Integer jobId, Integer isSimple) {
        //        if(isSimple == 0) {
        //            return "\"<ul class='list-group schedule-relation' style='width:auto;'><li
        // class='list-group-item'><a
        // href=\\\"detail?schedulerId=14520\\\">t_third_report导入hive表（全量）<\\/a><span
        // style='margin-left: 5px' class='badge
        // badge-success'>运行成功<\\/span><br><label><small>调度时间:2018-01-25 20:02:59.0<\\/small><\\/label>
        // <br><label><small>执行时间:2018-01-25 20:03:01.0<\\/small><\\/label>
        // <br><label><small>结束时间:2018-01-25 20:06:18.0<\\/small><\\/label> <\\/li><\\/ul><i
        // style='line-height: 35px' class='glyphicon glyphicon-arrow-right font-green
        // schedule-relation' title='依赖'><\\/i><ul class='list-group schedule-relation'
        // style='width:auto; position: absolute;'><li class='list-group-item'
        // onclick=\\\"viewJobRelation(12316,'zzChannelNewUserByHourChanged')\\\">zzChannelNewUserByHourChanged<span style='margin-left: 5px' class='badge badge-info'>正在运行<\\/span><br><label><small>调度时间:2018-01-25 20:15:00.0<\\/small><\\/label> <br><label><small>执行时间:2018-01-25 20:15:00.0<\\/small><\\/label> <br><label><small>结束时间:2018-01-25 20:15:00.0<\\/small><\\/label> <\\/li><li class='list-group-item' onclick=\\\"viewJobRelation(16057,'iosChannel_Dict')\\\">iosChannel_Dict<span style='margin-left: 5px' class='badge badge-success'>运行成功<\\/span><br><label><small>调度时间:2018-01-25 00:20:00.0<\\/small><\\/label> <br><label><small>执行时间:2018-01-25 00:20:02.0<\\/small><\\/label> <br><label><small>结束时间:2018-01-25 00:22:41.0<\\/small><\\/label> <\\/li><li class='list-group-item' onclick=\\\"viewJobRelation(16058,'生成IOSmuid和来源的对应关系')\\\">生成IOSmuid和来源的对应关系<span style='margin-left: 5px' class='badge badge-success'>运行成功<\\/span><br><label><small>调度时间:2018-01-25 20:06:00.0<\\/small><\\/label> <br><label><small>执行时间:2018-01-25 20:06:19.0<\\/small><\\/label> <br><label><small>结束时间:2018-01-25 20:07:42.0<\\/small><\\/label> <\\/li><\\/ul>\"";
        //        }
        return scheduleService.getSimpleDependentRelation(jobId);
        // String chartStr = scheduleService.getSimpleDependentRelation(jobId);
        // return JSONObject.toJSONString(chartStr);
    }

    @ResponseBody
    @RequestMapping("/api/getDependencyRelationship.do")
    public Object renderDependencyRelationship(Integer jobId) {
        String jsonString =
                JSON.toJSONString(scheduleService.renderG6Data4DependencyRelationship(jobId));
        return JSON.parseObject(jsonString);
    }

    /**
     * 一周分析图
     *
     * @param jobId
     * @return
     */
    @RequestMapping("/api/draw-job-charts")
    @ResponseBody
    public Object getDependentRelationsAction(Integer jobId) {
        // 获取一周执行记录
        // 1. 获取一周前的时间戳
        List<Long[]> runTimeList = scheduleService.getRunTimeList(jobId);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("jobRun", runTimeList);
        return resultMap;
    }

    /**
     * 获取所有的标签
     *
     * @return
     */
    @RequestMapping("/api/job-label-all")
    @ResponseBody
    public Object getJobLabelAll() {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", "success");
        resultMap.put("result", new ArrayList<Object>());
        return resultMap;
    }
}
