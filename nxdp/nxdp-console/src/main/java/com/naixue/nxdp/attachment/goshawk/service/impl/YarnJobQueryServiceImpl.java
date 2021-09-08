package com.naixue.nxdp.attachment.goshawk.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.naixue.nxdp.attachment.goshawk.dao.GoshawkYarnJobMapReduceRepository;
import com.naixue.nxdp.attachment.goshawk.dao.GoshawkYarnJobSparkRepository;
import com.naixue.nxdp.attachment.goshawk.model.QueryCondition;
import com.naixue.nxdp.attachment.goshawk.model.YarnJobMapReduce;
import com.naixue.nxdp.attachment.goshawk.model.YarnJobSpark;
import com.naixue.nxdp.attachment.goshawk.service.IYarnJobQueryService;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 刘蒙
 */
@SuppressWarnings("Duplicates")
@Service
public class YarnJobQueryServiceImpl implements IYarnJobQueryService {

    @Autowired
    private GoshawkYarnJobSparkRepository sparkRepository;

    @Autowired
    private GoshawkYarnJobMapReduceRepository mrRepository;

    @Override
    public Page<YarnJobSpark> listSpark(QueryCondition condition, Integer pageIndex, Integer pageSize) {
        Specification<YarnJobSpark> specification = (root, query, cb) -> {
            if (condition == null) {
                return null;
            }
            List<Predicate> data = new ArrayList<>();

            // 结束时间
            if (condition.getStart() != null && condition.getEnd() != null) {
                data.add(cb.between(root.get("finishTime"), condition.getStart(), condition.getEnd()));
            }

            // 运行类型
            if (!StringUtils.isEmpty(condition.getRunType())) {
                if (!condition.getRunType().equals("-1")) {
                    data.add(cb.equal(root.get("appScheduleType"), condition.getRunType()));
                }
            }

            // 队列
            if (!StringUtils.isEmpty(condition.getQueue())) {
                if (!condition.getQueue().equals("-1")) {
                    data.add(cb.equal(root.get("queueName"), condition.getQueue()));
                }
            }

            // 部门
            if (!StringUtils.isEmpty(condition.getDept())) {
                if (!condition.getDept().equals("-1")) {
                    data.add(cb.equal(root.get("userName"), condition.getDept()));
                }
            }

            // executor内存
            if (condition.getExecutorMemory() != null) {
                if (condition.getExecutorMemory() > 0) {
                    data.add(cb.equal(root.get("executorMemtory"), condition.getExecutorMemory()));
                }
            }

            // 总内存
            if (condition.getTotalMemory() != null) {
                if (condition.getTotalMemory() > 0) {
                    data.add(cb.equal(root.get("executorMemotryTotal"), condition.getTotalMemory()));
                }
            }

            // cpu
            if (condition.getCpu() != null) {
                if (condition.getCpu() > 0) {
                    data.add(cb.equal(root.get("executorCpucoreTotal"), condition.getCpu()));
                }
            }

            // 运行时长
            if (condition.getDuration() != null) {
                if (condition.getDuration() > 0) {
                    data.add(cb.equal(root.get("executeTime"), condition.getDuration()));
                }
            }

            // 任务名
            if (!StringUtils.isEmpty(condition.getJobName())) {
                if (!condition.getJobName().equals("-1")) {
                    data.add(cb.like(root.get("jobName"), "%" + condition.getJobName() + "%"));
                }
            }

            // 负责人
            if (!StringUtils.isEmpty(condition.getUser())) {
                if (!condition.getUser().equals("-1")) {
                    data.add(cb.equal(root.get("authorName"), condition.getUser()));
                }
            }

            // 任务名
            if (!StringUtils.isEmpty(condition.getJobId())) {
                if (!condition.getJobId().equals("-1")) {
                    data.add(cb.like(root.get("jobId"), "%" + condition.getJobId() + "%"));
                }
            }

            return cb.and(data.toArray(new Predicate[0]));
        };
        return sparkRepository.findAll(specification,
                new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "finishTime"))));
    }

    @Override
    public Page<YarnJobMapReduce> listMapReduce(QueryCondition condition, Integer pageIndex, Integer pageSize) {
        Specification<YarnJobMapReduce> specification = (root, query, cb) -> {
            if (condition == null) {
                return null;
            }
            List<Predicate> data = new ArrayList<>();

            // 结束时间
            if (condition.getStart() != null && condition.getEnd() != null) {
                data.add(cb.between(root.get("finishTime"), condition.getStart(), condition.getEnd()));
            }

            // 运行类型
            if (!StringUtils.isEmpty(condition.getRunType())) {
                if (!condition.getRunType().equals("-1")) {
                    data.add(cb.equal(root.get("appScheduleType"), condition.getRunType()));
                }
            }

            // 队列
            if (!StringUtils.isEmpty(condition.getQueue())) {
                if (!condition.getQueue().equals("-1")) {
                    data.add(cb.equal(root.get("queueName"), condition.getQueue()));
                }
            }

            // 部门
            if (!StringUtils.isEmpty(condition.getDept())) {
                if (!condition.getDept().equals("-1")) {
                    data.add(cb.equal(root.get("userName"), condition.getDept()));
                }
            }

            // mapper数量
            if (condition.getNumMapper() != null) {
                if (condition.getNumMapper() > 0) {
                    data.add(cb.equal(root.get("mapTaskNumber"), condition.getNumMapper()));
                }
            }

            // reducer数量
            if (condition.getNumReducer() != null) {
                if (condition.getNumReducer() > 0) {
                    data.add(cb.equal(root.get("reduceTaskNumber"), condition.getNumReducer()));
                }
            }

            // 运行时长
            if (condition.getDuration() != null) {
                if (condition.getDuration() > 0) {
                    data.add(cb.equal(root.get("executeTime"), condition.getDuration()));
                }
            }

            // 任务名
            if (!StringUtils.isEmpty(condition.getJobName())) {
                if (!condition.getJobName().equals("-1")) {
                    data.add(cb.like(root.get("jobName"), "%" + condition.getJobName() + "%"));
                }
            }

            // 负责人
            if (!StringUtils.isEmpty(condition.getUser())) {
                if (!condition.getUser().equals("-1")) {
                    data.add(cb.equal(root.get("authorName"), condition.getUser()));
                }
            }

            // 任务名
            if (!StringUtils.isEmpty(condition.getJobId())) {
                if (!condition.getJobId().equals("-1")) {
                    data.add(cb.like(root.get("jobId"), "%" + condition.getJobId() + "%"));
                }
            }

            return cb.and(data.toArray(new Predicate[0]));
        };
        return mrRepository.findAll(specification,
                new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "finishTime"))));
    }
}
