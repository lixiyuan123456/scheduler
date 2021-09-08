package com.naixue.nxdp.model;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.common.base.Strings;
import com.naixue.nxdp.util.CronUtils;

import lombok.Data;

/**
 * @author: wangyu @Created by 2018/1/24
 */
@Data
@Entity
@Table(name = "t_job_schedule")
public class JobSchedule implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "job_id")
    private Integer jobId;

    @Column(name = "dept_id")
    private Integer deptId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "job_name")
    private String jobName;

    @Column(name = "job_desc", length = 2000)
    private String jobDesc;

    @Column(name = "status")
    private Integer status;

    @Column(name = "job_state")
    private Integer jobState;

    @Column(name = "job_type")
    private Integer jobType;

    @Column(name = "dispatch_command")
    private String dispatchCommand;

    @Column(name = "run_time", length = 60)
    private String runTime;

    @Column(name = "execute_time")
    private Date executeTime;

    @Column(name = "next_fire_time")
    private Date nextFireTime;

    @Column(name = "job_category")
    private Integer jobCategory;

    @Column(name = "job_level")
    private Integer jobLevel;

    @Column(name = "is_monitor")
    private Integer isMonitor; // 是否报警

    @Transient
    private Monitor monitor;

    @Column(name = "parallel_run")
    private Integer parallelRun;

    @Column(name = "error_run_continue")
    private Integer errorRunContinue;

    @Column(name = "retry")
    private Integer retry;

    @Column(name = "retry_time_span")
    private Integer retryTimeSpan;

    @Column(name = "receiver", length = 1000)
    private String receiver;

    @Column(name = "labels", length = 256)
    private String labels;

    @Column(name = "create_time", insertable = false, updatable = false)
    private Date createTime;

    @Column(name = "update_time", insertable = false, updatable = false)
    private Date updateTime;

    @Column(name = "hadoop_queue_id")
    private Integer hadoopQueueId;

    @Column(name = "col_extend")
    private String colExtend;

    @Column(name = "delay_warn")
    private Integer delayWarn;

    @Transient
    private DelayAlarm delayAlarm;

    @Column(name = "is_delay")
    private Integer isDelay;

    @Column(name = "update_id", length = 50)
    private String updateId;

    @Column(name = "update_name", length = 100)
    private String updateName;

    @Column(name = "jar_path")
    private String jarPath;

    @Column(name = "spark_version")
    private String sparkVersion;

    @Column(name = "out_mode")
    private Integer outMode = 0;

    @Column(name = "out_value")
    private String outValue = "";

    @Column(name = "schedule_level")
    private Integer scheduleLevel;

    @Transient
    private JobScheduleLevel jobScheduleLevel;

    /**
     * cron表达式的调度级别:天, 小时, 分钟
     */
    public static JobScheduleLevel scheduleLevel(String cron) {
        if (Strings.isNullOrEmpty(cron)) {
            return JobScheduleLevel.NULL;
        }
        TimeUnit cycleTime = CronUtils.getFireCycleTime(cron);
        if (cycleTime == TimeUnit.SECONDS) {
            return JobScheduleLevel.SECOND;
        } else if (cycleTime == TimeUnit.MINUTES) {
            return JobScheduleLevel.MINUTE;
        } else if (cycleTime == TimeUnit.HOURS) {
            return JobScheduleLevel.HOUR;
        }
        return JobScheduleLevel.DAY;
    }

    /**
     * 非数据库字段*
     */
    public void setRunTime(String runTime) {
        this.runTime = runTime;
        JobScheduleLevel jobScheduleLevel = scheduleLevel(runTime);
        this.scheduleLevel = jobScheduleLevel != null ? jobScheduleLevel.getId() : 0;
        this.jobScheduleLevel = jobScheduleLevel;
    }

    public void setDelayWarn(Integer delayWarn) {
        this.delayWarn = delayWarn;
        if (this.delayWarn == null) { // 默认报警
            this.delayAlarm = DelayAlarm.ON;
        } else if (this.delayWarn == 0) { // 不报警
            this.delayAlarm = DelayAlarm.OFF;
        } else if (this.delayWarn == 1) { // 报警
            this.delayAlarm = DelayAlarm.ON;
        }
    }

    public DelayAlarm getDelayAlarm() {
        return this.delayAlarm;
    }

    public static enum Status {
        DRAFT(0, "草稿"),

        ONLINE(1, "上线"),

        OFFLINE(2, "暂停"),

        DELETE(3, "删除");

        private Integer id;
        private String name;

        Status(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public static Status getEnum(Integer id) {
            for (Status e : Status.values()) {
                if (e.id.equals(id)) {
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

    /**
     * job调度级别
     */
    public static enum JobScheduleLevel {
        NULL(-1, "未配置"),
        SECOND(0, "秒级"),
        MINUTE(1, "分钟级"),
        HOUR(2, "小时级"),
        DAY(3, "天级");

        private Integer id;
        private String name;
        JobScheduleLevel(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public static JobScheduleLevel getEnum(Integer id) {
            for (JobScheduleLevel e : JobScheduleLevel.values()) {
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

    public static enum JobState {
        VIRGIN(0, "未调度"),

        WAITING_SIGNAL(1, "等待信号"),

        RUNNING(2, "正在运行"),

        SUCCESS(3, "运行成功"),

        FAILURE(4, "运行失败"),

        KILLED(5, "被kill"),

        WAITING_RESOURCE(6, "等待资源"),

        PARSE_FAILURE(7, "解析失败"),

        PAUSE(8, "暂停"),

        RESUBMIT(9, "重复提交"),

        INTERRUPT(10, "运行中断"), // 运行中断或运行超时3天后被标记为运行中断

        KILLING(99, "killing");

        private Integer id;
        private String name;

        JobState(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public static JobState getEnum(Integer id) {
            for (JobState e : JobState.values()) {
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

    public static enum Monitor {
        OFF,
        ON;
    }

    public static enum DelayAlarm {
        OFF, // 0不告警
        ON; // 1告警
    }
}
