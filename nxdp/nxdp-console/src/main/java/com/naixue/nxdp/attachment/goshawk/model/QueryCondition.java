package com.naixue.nxdp.attachment.goshawk.model;

import lombok.Data;

import java.util.Date;

/**
 * @author 刘蒙
 */
@Data
public class QueryCondition {
    private Date start;
    private Date end;
    private String runType;
    private String appType;
    private String queue;
    private String dept;
    private Integer executorMemory;
    private Integer totalMemory;
    private Integer cpu;
    private Integer duration;
    private Integer numMapper;
    private Integer numReducer;
    private String jobName;
    private String user;
    private String jobId;

    private String orderField;
    private String orderType;
}