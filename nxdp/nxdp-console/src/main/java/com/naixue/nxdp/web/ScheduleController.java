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
                // ?????????????????????????????????
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
                // ???????????????????????????
                if (!StringUtils.isEmpty(searchForm.getJobName())) {
                    predicate =
                            criteriaBuilder.and(
                                    predicate,
                                    criteriaBuilder.like(root.get("jobName"), "%" + searchForm.getJobName() + "%"));
                }
                // ???????????????????????????-1
                if (searchForm.getJobLevel() != null && searchForm.getJobLevel() != -1) {
                    predicate =
                            criteriaBuilder.and(
                                    predicate, criteriaBuilder.equal(root.get("jobLevel"), searchForm.getJobLevel()));
                }
                // ???????????????????????????-1
                if (searchForm.getJobState() != null && searchForm.getJobState() != -1) {
                    predicate =
                            criteriaBuilder.and(
                                    predicate, criteriaBuilder.equal(root.get("jobState"), searchForm.getJobState()));
                }
                // ???????????????????????????
                if (searchForm.getUserId() != null
                        && !"-1".equals(searchForm.getUserId())
                        && searchForm.getUserId().trim().length() > 0) {
                    predicate =
                            criteriaBuilder.and(
                                    predicate, criteriaBuilder.equal(root.get("userId"), searchForm.getUserId()));
                }
                // ??????????????????
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
        // ????????????????????????????????? ??? ????????????
        Pageable pageable =
                new PageRequest(
                        searchParam.getStart() / searchParam.getLength(),
                        searchParam.getLength(),
                        Sort.Direction.DESC,
                        "createTime");
        Page<JobExecuteLog> data =
                jobExecuteLogRepository.findAll(getJobExecuteLogSpec(searchParam), pageable);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("recordsFiltered", data.getTotalElements()); // ?????????????????????????????????
        resultMap.put("start", searchParam.getStart());
        resultMap.put("pageSize", searchParam.getLength());
        resultMap.put(
                "draw",
                searchParam
                        .getDraw()); // ????????????ajax????????????????????????????????????????????????ajax????????????????????????dataTable????????????????????????????????????????????????draw?????????
        resultMap.put("data", data.getContent());
        resultMap.put("recordsTotal", data.getTotalElements()); // ???????????????????????????????????????
        return resultMap;
    }

    /**
     * ???????????????????????????
     *
     * @param searchParam ????????????
     * @return
     */
    @RequestMapping("/api/search-schedule-task")
    @ResponseBody
    public Object searchScheduleTask(SearchParam searchParam) {
        SearchForm searchForm = searchParam.getSearchForm();
        int totalSize = 0;
        List<JobSchedule> jobs = new ArrayList<>();
        // ?????????????????????????????????
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
        resultMap.put("recordsFiltered", totalSize); // ?????????????????????????????????
        resultMap.put("start", searchParam.getStart());
        resultMap.put("pageSize", searchParam.getLength());
        resultMap.put(
                "draw",
                searchParam
                        .getDraw()); // ????????????ajax????????????????????????????????????????????????ajax????????????????????????dataTable????????????????????????????????????????????????draw?????????
        resultMap.put("data", jobs);
        resultMap.put("recordsTotal", totalSize); // ???????????????????????????????????????
        return resultMap;
    }

    /**
     * ???????????????????????????????????????
     *
     * @param searchParam ????????????
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
        resultMap.put("recordsFiltered", totalSize); // ?????????????????????????????????
        resultMap.put("start", searchParam.getStart());
        resultMap.put("pageSize", searchParam.getLength());
        resultMap.put(
                "draw",
                searchParam
                        .getDraw()); // ????????????ajax????????????????????????????????????????????????ajax????????????????????????dataTable????????????????????????????????????????????????draw?????????
        resultMap.put("data", jobs);
        resultMap.put("recordsTotal", totalSize); // ???????????????????????????????????????
        return resultMap;
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param jobId
     * @return
     */
    @RequestMapping("/detail")
    public ModelAndView taskDetail(@RequestParam(name = "schedulerId") int jobId) {
        // ??????job???????????????
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
     * ?????????????????????
     *
     * @param jobId
     * @param isSimple 0??????????????????1???????????????
     * @return
     */
    @RequestMapping("/api/getDependentRelationsAction")
    @ResponseBody
    public Object getDependentRelationsAction(Integer jobId, Integer isSimple) {
        //        if(isSimple == 0) {
        //            return "\"<ul class='list-group schedule-relation' style='width:auto;'><li
        // class='list-group-item'><a
        // href=\\\"detail?schedulerId=14520\\\">t_third_report??????hive???????????????<\\/a><span
        // style='margin-left: 5px' class='badge
        // badge-success'>????????????<\\/span><br><label><small>????????????:2018-01-25 20:02:59.0<\\/small><\\/label>
        // <br><label><small>????????????:2018-01-25 20:03:01.0<\\/small><\\/label>
        // <br><label><small>????????????:2018-01-25 20:06:18.0<\\/small><\\/label> <\\/li><\\/ul><i
        // style='line-height: 35px' class='glyphicon glyphicon-arrow-right font-green
        // schedule-relation' title='??????'><\\/i><ul class='list-group schedule-relation'
        // style='width:auto; position: absolute;'><li class='list-group-item'
        // onclick=\\\"viewJobRelation(12316,'zzChannelNewUserByHourChanged')\\\">zzChannelNewUserByHourChanged<span style='margin-left: 5px' class='badge badge-info'>????????????<\\/span><br><label><small>????????????:2018-01-25 20:15:00.0<\\/small><\\/label> <br><label><small>????????????:2018-01-25 20:15:00.0<\\/small><\\/label> <br><label><small>????????????:2018-01-25 20:15:00.0<\\/small><\\/label> <\\/li><li class='list-group-item' onclick=\\\"viewJobRelation(16057,'iosChannel_Dict')\\\">iosChannel_Dict<span style='margin-left: 5px' class='badge badge-success'>????????????<\\/span><br><label><small>????????????:2018-01-25 00:20:00.0<\\/small><\\/label> <br><label><small>????????????:2018-01-25 00:20:02.0<\\/small><\\/label> <br><label><small>????????????:2018-01-25 00:22:41.0<\\/small><\\/label> <\\/li><li class='list-group-item' onclick=\\\"viewJobRelation(16058,'??????IOSmuid????????????????????????')\\\">??????IOSmuid????????????????????????<span style='margin-left: 5px' class='badge badge-success'>????????????<\\/span><br><label><small>????????????:2018-01-25 20:06:00.0<\\/small><\\/label> <br><label><small>????????????:2018-01-25 20:06:19.0<\\/small><\\/label> <br><label><small>????????????:2018-01-25 20:07:42.0<\\/small><\\/label> <\\/li><\\/ul>\"";
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
     * ???????????????
     *
     * @param jobId
     * @return
     */
    @RequestMapping("/api/draw-job-charts")
    @ResponseBody
    public Object getDependentRelationsAction(Integer jobId) {
        // ????????????????????????
        // 1. ???????????????????????????
        List<Long[]> runTimeList = scheduleService.getRunTimeList(jobId);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("jobRun", runTimeList);
        return resultMap;
    }

    /**
     * ?????????????????????
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
