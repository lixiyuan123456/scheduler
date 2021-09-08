package com.naixue.nxdp.attachment.goshawk.model;

import java.io.Serializable;
import java.util.Date;

public class ClusterYarnMapreduce implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Integer id;

    private String jobId;

    private String jobName;

    private String authorName;

    private Integer mapTaskNumber;

    private Integer reduceTaskNumber;

    private Long executeTime;

    private Date startTime;

    private Date finishTime;

    private String appType;

    private String appScheduleType;

    private java.sql.Date insertTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Integer getMapTaskNumber() {
        return mapTaskNumber;
    }

    public void setMapTaskNumber(Integer mapTaskNumber) {
        this.mapTaskNumber = mapTaskNumber;
    }

    public Integer getReduceTaskNumber() {
        return reduceTaskNumber;
    }

    public void setReduceTaskNumber(Integer reduceTaskNumber) {
        this.reduceTaskNumber = reduceTaskNumber;
    }

    public Long getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Long executeTime) {
        this.executeTime = executeTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getAppScheduleType() {
        return appScheduleType;
    }

    public void setAppScheduleType(String appScheduleType) {
        this.appScheduleType = appScheduleType;
    }

    public java.sql.Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(java.sql.Date insertTime) {
        this.insertTime = insertTime;
    }

}
