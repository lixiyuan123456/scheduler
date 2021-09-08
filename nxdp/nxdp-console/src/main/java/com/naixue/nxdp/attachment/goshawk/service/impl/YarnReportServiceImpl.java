package com.naixue.nxdp.attachment.goshawk.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.naixue.nxdp.attachment.goshawk.dao.YarnReportMapReduceRepository;
import com.naixue.nxdp.attachment.goshawk.dao.YarnReportOverOneHourRepository;
import com.naixue.nxdp.attachment.goshawk.dao.YarnReportSparkRepository;
import com.naixue.nxdp.attachment.goshawk.model.ReportMapReduce;
import com.naixue.nxdp.attachment.goshawk.model.ReportOverOneHour;
import com.naixue.nxdp.attachment.goshawk.model.ReportSpark;
import com.naixue.nxdp.attachment.goshawk.model.YarnReportCondition;
import com.naixue.nxdp.attachment.goshawk.service.IYarnReportService;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 刘蒙
 */
@Service
public class YarnReportServiceImpl implements IYarnReportService {

    @Autowired
    private YarnReportOverOneHourRepository oneHourRepository;

    @Autowired
    private YarnReportMapReduceRepository mrRepository;

    @Autowired
    private YarnReportSparkRepository sparkRepository;

    @Override
    public Page<ReportOverOneHour> listOverOneHour(YarnReportCondition condition, Integer pageIndex, Integer pageSize) {
        return oneHourRepository.findAll(getSpec(condition), new PageRequest(pageIndex, pageSize));
    }

    @Override
    public Page<ReportMapReduce> listMapReduce(YarnReportCondition condition, Integer pageIndex, Integer pageSize) {
        return mrRepository.findAll(getSpec(condition), new PageRequest(pageIndex, pageSize));
    }

    @Override
    public Page<ReportSpark> listSpark(YarnReportCondition condition, Integer pageIndex, Integer pageSize) {
        return sparkRepository.findAll(getSpec(condition), new PageRequest(pageIndex, pageSize));
    }

    private <T> Specification<T> getSpec(YarnReportCondition condition) {
        return (root, query, cb) -> {
            if (condition == null) {
                return null;
            }
            List<Predicate> data = new ArrayList<>();

            // 插入记录时间
            if (condition.getStart() != null && condition.getEnd() != null) {
                data.add(cb.between(root.get("gmtCreate"), condition.getStart(), condition.getEnd()));
            }

            return cb.and(data.toArray(new Predicate[0]));
        };
    }
}
