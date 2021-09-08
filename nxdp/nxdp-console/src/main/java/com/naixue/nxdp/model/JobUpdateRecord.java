package com.naixue.nxdp.model;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @author: wangyu
 * @Created by 2018/1/30
 **/
@Entity
@Table(name = "t_job_update_record")
public class JobUpdateRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "job_id")
    private int jobId;

    @Column(name = "operator_id", length = 50)
    private String operatorId;

    @Column(name = "operator_name", length = 40)
    private String operatorName;

    @Column(name = "file_name", length = 200)
    private String fileName;

    @Column(name = "file_path", length = 200)
    private String filepath;

    @Column(name = "revision", length = 50)
    private String revision;

    @Column(name = "update_time")
    private Timestamp updateTime;

    public JobUpdateRecord() {
    }

    public JobUpdateRecord(int jobId, String operatorId, String operatorName, String fileName, String filepath, String revision, Timestamp updateTime) {
        this.jobId = jobId;
        this.operatorId = operatorId;
        this.fileName = fileName;
        this.operatorName = operatorName;
        this.filepath = filepath;
        this.revision = revision;
        this.updateTime = updateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
