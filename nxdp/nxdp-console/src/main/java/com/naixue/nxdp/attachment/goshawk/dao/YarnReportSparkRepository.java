package com.naixue.nxdp.attachment.goshawk.dao;

import com.naixue.nxdp.attachment.goshawk.model.ReportSpark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 苍蝇系统-yarn管理-报表查询
 * <p>
 * spark任务消耗资源过多清单
 *
 * @author 刘蒙
 */
public interface YarnReportSparkRepository extends
        JpaRepository<ReportSpark, Integer>,
        JpaSpecificationExecutor<ReportSpark> {
}
