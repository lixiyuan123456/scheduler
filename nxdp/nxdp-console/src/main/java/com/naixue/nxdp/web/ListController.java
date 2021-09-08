package com.naixue.nxdp.web;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.naixue.nxdp.model.JobSchedule;
import com.naixue.nxdp.model.SearchForm;
import com.naixue.nxdp.model.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.naixue.nxdp.dao.JobScheduleRepository;

/**
 * @author wangyu @Created by 2018/1/29
 */
@Controller
@RequestMapping("/list/api")
public class ListController {

    @Autowired
    private JobScheduleRepository jobScheduleRepository;

    private static Specification<JobSchedule> getJobScheduleSpec(final SearchParam searchParam) {
        return new Specification<JobSchedule>() {
            @Override
            public Predicate toPredicate(
                    Root<JobSchedule> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                // 默认仅查询状态为0：草稿，1：上线，2：暂停的任务信息
                List<Integer> statusList = Arrays.asList(0, 1, 2);
                CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get("status"));
                for (Integer s : statusList) {
                    in.value(s);
                }
                Predicate predicate = criteriaBuilder.and(in);

                SearchForm searchForm = searchParam.getSearchForm();
                if (searchForm != null) {
                    // 判断任务名是否为空
                    String jobName = searchForm.getJobName();
                    if (jobName != null && jobName.trim().length() > 0) {
                        Predicate p1 = criteriaBuilder.like(root.get("jobName"), "%" + jobName + "%");
                        Predicate p2 = criteriaBuilder.like(root.get("outValue"), "%" + jobName + "%");
                        predicate = criteriaBuilder.and(criteriaBuilder.or(p1, p2));
            /*predicate =
            criteriaBuilder.and(
                predicate, criteriaBuilder.like(root.get("jobName"), "%" + jobName + "%"));*/
                    }

                    // 判断任务类型是否为-1
                    Integer value = searchForm.getJobType();
                    if (value != null && value != -1) {
                        predicate =
                                criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("jobType"), value));
                    }

                    // 判断重要等级是否为-1
                    value = searchForm.getJobLevel();
                    if (value != null && value != -1) {
                        predicate =
                                criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("jobLevel"), value));
                    }

                    // 判断调度状态是否为-1
                    value = searchForm.getStatus();
                    if (value != null && value != -1) {
                        predicate =
                                criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), value));
                    }

                    // 判断运行状态是否为-1
                    value = searchForm.getJobState();
                    if (value != null && value != -1) {
                        predicate =
                                criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("jobState"), value));
                    }

                    // 判断其它筛选：凌晨任务、支持并行、出错阻塞
                    // 凌晨任务
                    value = searchForm.getIsMonitor();
                    if (value != null && value != -1) {
                        predicate =
                                criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isMonitor"), value));
                    }
                    // 支持并行
                    value = searchForm.getParallelRun();
                    if (value != null && value != -1) {
                        predicate =
                                criteriaBuilder.and(
                                        predicate, criteriaBuilder.equal(root.get("parallelRun"), value));
                    }
                    // 出错阻塞
                    value = searchForm.getErrorRunContinue();
                    if (value != null && value != -1) {
                        predicate =
                                criteriaBuilder.and(
                                        predicate, criteriaBuilder.equal(root.get("errorRunContinue"), value));
                    }

                    // 判断用户名是否为空
                    String userId = searchForm.getUserId();
                    if (userId != null && !"-1".equals(userId) && userId.trim().length() > 0) {
                        predicate =
                                criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("userId"), userId));
                    }

                    // 判断调度级别
                    Integer scheduleLevel = searchForm.getScheduleLevel();
                    if (scheduleLevel != null && scheduleLevel != -1) {
                        Predicate equal = criteriaBuilder.equal(root.get("scheduleLevel"), scheduleLevel);
                        predicate =
                                predicate == null
                                        ? criteriaBuilder.and(equal)
                                        : criteriaBuilder.and(predicate, equal);
                    }
                }
                return predicate;
            }
        };
    }

    /**
     * 列表页 - 返回任务搜索结果
     *
     * @param searchParam 搜索条件
     * @return
     */
    @RequestMapping("/search-task")
    @ResponseBody
    public Object searchTask(SearchParam searchParam) {
        // 获取指定条件的任务明细 及 记录总数
        Pageable pageable =
                new PageRequest(
                        searchParam.getStart() / searchParam.getLength(),
                        searchParam.getLength(),
                        Sort.Direction.DESC,
                        "jobId");
        Page<JobSchedule> data =
                jobScheduleRepository.findAll(getJobScheduleSpec(searchParam), pageable);
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
}
