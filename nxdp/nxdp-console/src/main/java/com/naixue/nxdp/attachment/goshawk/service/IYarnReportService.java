package com.naixue.nxdp.attachment.goshawk.service;

import com.naixue.nxdp.attachment.goshawk.model.ReportMapReduce;
import org.springframework.data.domain.Page;

import com.naixue.nxdp.attachment.goshawk.model.ReportOverOneHour;
import com.naixue.nxdp.attachment.goshawk.model.ReportSpark;
import com.naixue.nxdp.attachment.goshawk.model.YarnReportCondition;

/**
 * @author 刘蒙
 */
public interface IYarnReportService {
    Page<ReportOverOneHour> listOverOneHour(
            YarnReportCondition condition, Integer pageIndex, Integer pageSize);

    Page<ReportMapReduce> listMapReduce(
            YarnReportCondition condition, Integer pageIndex, Integer pageSize);

    Page<ReportSpark> listSpark(
            YarnReportCondition condition, Integer pageIndex, Integer pageSize);
}
