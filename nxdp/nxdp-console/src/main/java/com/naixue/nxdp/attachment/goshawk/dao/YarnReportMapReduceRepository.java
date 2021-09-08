package com.naixue.nxdp.attachment.goshawk.dao;

import com.naixue.nxdp.attachment.goshawk.model.ReportMapReduce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 苍蝇系统-yarn管理-报表查询
 * <p>
 * MR任务task数过多清单
 *
 * @author 刘蒙
 */
public interface YarnReportMapReduceRepository extends
        JpaRepository<ReportMapReduce, Integer>,
        JpaSpecificationExecutor<ReportMapReduce> {
}
