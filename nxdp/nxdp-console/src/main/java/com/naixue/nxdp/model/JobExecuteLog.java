package com.naixue.nxdp.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.naixue.nxdp.config.CFG;
import org.springframework.util.StringUtils;

import lombok.Data;

@Data
@Entity
@Table(name = "t_job_execute_log")
public class JobExecuteLog implements Serializable {

    public static final Integer AUTO_EXECUTE_JOB_ID = -1;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "job_id")
    private Integer jobId;

    @Column(name = "task_id")
    private Integer taskId;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Transient
    private String userName;

    @Column(name = "job_name")
    private String jobName;

    @Column(name = "execute_host")
    private String executeHost;

    @Column(name = "job_state")
    private Integer jobState;

    @Column(name = "job_level")
    private Integer jobLevel;

    @Column(name = "dispatch_command")
    private String dispatchCommand;

    @Column(name = "type")
    private Integer type;

    @Column(name = "retry")
    private Integer retry;

    @Column(name = "choose_run_time")
    private Date chooseRunTime;

    @Column(name = "excute_submit_time")
    private Date executeSubmitTime;

    @Column(name = "excute_time")
    private Date executeTime;

    @Column(name = "excute_end_time")
    private Date executeEndTime;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    @Column(name = "exit_code")
    private Integer exitCode;

    @Column(name = "target_server")
    private String targetServer;

    @Transient
    private Long executeCostTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "job_id",
            referencedColumnName = "job_id",
            insertable = false,
            updatable = false)
    private JobSchedule jobSchedule;

    @Transient
    private String logReaderWs = CFG.WEBSOCKET_HADOOP_JOB_EXECUTE_LOG_READER_URL + UUID.randomUUID();

    public JobExecuteLog() {
    }

    public JobExecuteLog(
            Integer jobId,
            Integer taskId,
            String userId,
            String jobName,
            String executeHost,
            Integer jobState,
            Integer jobLevel,
            String dispatchCommand,
            Integer type,
            Integer retry,
            Date chooseRunTime,
            Date createTime,
            Date updateTime) {
        this.jobId = jobId;
        this.taskId = taskId;
        this.userId = userId;
        this.jobName = jobName;
        this.executeHost = executeHost;
        this.jobState = jobState;
        this.jobLevel = jobLevel;
        this.dispatchCommand = dispatchCommand;
        this.type = type;
        this.retry = retry;
        this.chooseRunTime = chooseRunTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Long getExecuteCostTime() {
        Date startTime = getExecuteTime();
        Date endTime = getExecuteEndTime();
        if (endTime != null && startTime != null) {
            executeCostTime = (endTime.getTime() - startTime.getTime()) / 1000;
        }
        return executeCostTime;
    }

    public String getTargetServer() {
        if (StringUtils.isEmpty(this.targetServer)) {
            return "10.148.16.86";
        }
        return this.targetServer;
    }

    public static enum ExecuteType {
        AUTO(1, "系统调度"),

        MANUAL(2, "手动调度"),

        SINGLE(3, "一次性调度");

        private Integer id;
        private String name;
        ExecuteType(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public static ExecuteType getEnum(Integer id) {
            for (ExecuteType e : ExecuteType.values()) {
                if (e.getId().equals(id)) {
                    return e;
                }
            }
            return null;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
