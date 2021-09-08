package com.naixue.nxdp.attachment.goshawk.dao;

import com.naixue.nxdp.attachment.goshawk.model.ReportOverOneHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 苍蝇系统-yarn管理-报表查询
 * <p>
 * spark和MR任务运行超过一小时清单
 *
 * @author 刘蒙
 */
public interface YarnReportOverOneHourRepository extends
        JpaRepository<ReportOverOneHour, Integer>,
        JpaSpecificationExecutor<ReportOverOneHour> {
}
