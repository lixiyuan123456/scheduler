package com.naixue.scheduler.model;

import java.util.Date;

import lombok.Data;

@Data
public class JobSchedule {

  public static final int UNLOAD_IN_QUARTZ_STATUS = 0;

  public static final int LOADED_IN_QUARTZ_STATUS = 1;

  private Integer jobId;

  private Integer deptId;

  private String userId;

  private String userName;

  private String jobName;

  private String jobDesc;

  private Integer status;

  private Integer jobState;

  private Integer jobType;

  private String dispatchCommand;

  private String runTime;

  private Date executeTime;

  private Date nextFireTime;

  private Integer jobCategory;

  private Integer jobLevel;

  private Integer isMonitor; // 是否报警

  private Integer parallelRun;

  private Integer errorRunContinue;

  private Integer retry;

  private Integer retryTimeSpan;

  private String receiver;

  private String labels;

  private Date createTime;

  private Date updateTime;

  private Integer hadoopQueueId;

  private String colExtend;

  private Integer delayWarn;

  private Integer isDelay;

  private String updateId;

  private String updateName;

  private String jarPath;

  private String sparkVersion;

  private Integer scheduleLevel;

  /** 0:未载入 1:已载入 */
  private Integer loadInQuartzStatus;

  private boolean loadedInQuartz;

  private Integer version;

  public boolean getLoadedInQuartz() {
    if (this.loadInQuartzStatus == 1) {
      this.loadedInQuartz = true;
    }
    return this.loadedInQuartz;
  }

  public static enum Status {
    DRAFT(0, "草稿"),

    ONLINE(1, "上线"),

    OFFLINE(2, "暂停"),

    DELETE(3, "删除");

    Status(Integer id, String name) {
      this.id = id;
      this.name = name;
    }

    private Integer id;

    private String name;

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

    public static Status getEnum(Integer id) {
      for (Status e : Status.values()) {
        if (e.id.equals(id)) {
          return e;
        }
      }
      return null;
    }
  }

  /** job调度级别 */
  public static enum JobScheduleLevel {
    MINUTE(1, "分钟级"),
    HOUR(2, "小时级"),
    DAY(3, "天级");

    JobScheduleLevel(Integer id, String name) {
      this.id = id;
      this.name = name;
    }

    private Integer id;
    private String name;

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

    public static JobScheduleLevel getEnum(Integer id) {
      for (JobScheduleLevel e : JobScheduleLevel.values()) {
        if (e.getId().equals(id)) {
          return e;
        }
      }
      return null;
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

    INTERRUPT(10, "运行中断"),

    KILLING(99, "killing");

    JobState(Integer id, String name) {
      this.id = id;
      this.name = name;
    }

    private Integer id;

    private String name;

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

    public static JobState getEnum(Integer id) {
      for (JobState e : JobState.values()) {
        if (e.getId().equals(id)) {
          return e;
        }
      }
      return null;
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
