package com.naixue.nxdp.attachment.goshawk.model;

import lombok.Data;

import java.util.Date;

/**
 * @author 刘蒙
 */
@Data
public class YarnReportCondition {

    private Date start;

    private Date end;

    private String reportType;
}
