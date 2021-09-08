package com.naixue.nxdp.model;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @author: wangyu
 * @Created by 2018/1/31
 **/
@Entity
@Table(name = "t_job_rerun_config")
public class JobRerunConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "begin_jobs")
    private String beginJobs;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "type")
    private int type;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "status")
    private int status;

    @Column(name = "run_time")
    private Timestamp runTime;

    @Column(name = "create_time")
    private Timestamp createTime;

    @Column(name = "update_time")
    private Timestamp updateTime;

    public JobRerunConfig() {
    }

    public JobRerunConfig(String beginJobs, String userId, int type, String name, int status, Timestamp runTime, Timestamp createTime, Timestamp updateTime) {
        this.beginJobs = beginJobs;
        this.userId = userId;
        this.type = type;
        this.name = name;
        this.status = status;
        this.runTime = runTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBeginJobs() {
        return beginJobs;
    }

    public void setBeginJobs(String beginJobs) {
        this.beginJobs = beginJobs;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public Timestamp getRunTime() {
        return runTime;
    }

    public void setRunTime(Timestamp runTime) {
        this.runTime = runTime;
    }
}
