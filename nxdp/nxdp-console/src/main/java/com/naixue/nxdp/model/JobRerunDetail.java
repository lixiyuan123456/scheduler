package com.naixue.nxdp.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * @author: wangyu
 * @Created by 2018/1/25
 **/

@Entity
@Table(name = "t_job_rerun_detail")
public class JobRerunDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "task_id")
    private Integer taskId;

    @Column(name = "job_id")
    private Integer jobId;

    @Column(name = "operation_type")
    private Integer operationType;

    @Column(name = "create_time")
    private Timestamp createTime;

    @Column(name = "update_time")
    private Timestamp updateTime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false)
    private JobSchedule jobSchedule;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "task_id", referencedColumnName = "task_id", insertable = false, updatable = false),
            @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false)})
    private JobExecuteLog jobExecuteLog;

    public JobRerunDetail() {
    }

    public JobRerunDetail(Integer taskId, Integer jobId, Integer operationType, Timestamp createTime,
                          Timestamp updateTime) {
        this.taskId = taskId;
        this.jobId = jobId;
        this.operationType = operationType;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public Integer getOperationType() {
        return operationType;
    }

    public void setOperationType(Integer operationType) {
        this.operationType = operationType;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public JobSchedule getJobSchedule() {
        return jobSchedule;
    }

    public void setJobSchedule(JobSchedule jobSchedule) {
        this.jobSchedule = jobSchedule;
    }

    public JobExecuteLog getJobExecuteLog() {
        return jobExecuteLog;
    }

    public void setJobExecuteLog(JobExecuteLog jobExecuteLog) {
        this.jobExecuteLog = jobExecuteLog;
    }
}
