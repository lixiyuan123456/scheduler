package com.naixue.nxdp.model;

public class SearchForm {
    private Integer jobType; // 任务类型
    private Integer jobLevel; // 重要等级
    private String userId; // 开发者
    private Integer status; // 调度状态
    private Integer jobState; // 运行状态
    private Integer isMonitor; // 其它筛选-凌晨任务
    private Integer parallelRun; // 其它筛选-出错阻塞
    private Integer errorRunContinue; // 其它筛选-支持并行
    private String jobName; // 任务关键字
    private String labels; // 选择标签
    private Integer jobTaskId;
    private String createDt; // 调度时间
    private Integer type;
    private Integer scheduleLevel; // 调度级别

    public Integer getJobType() {
        return jobType;
    }

    public void setJobType(Integer jobType) {
        this.jobType = jobType;
    }

    public Integer getJobLevel() {
        return jobLevel;
    }

    public void setJobLevel(Integer jobLevel) {
        this.jobLevel = jobLevel;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getJobState() {
        return jobState;
    }

    public void setJobState(Integer jobState) {
        this.jobState = jobState;
    }

    public Integer getIsMonitor() {
        return isMonitor;
    }

    public void setIsMonitor(Integer isMonitor) {
        this.isMonitor = isMonitor;
    }

    public Integer getParallelRun() {
        return parallelRun;
    }

    public void setParallelRun(Integer parallelRun) {
        this.parallelRun = parallelRun;
    }

    public Integer getErrorRunContinue() {
        return errorRunContinue;
    }

    public void setErrorRunContinue(Integer errorRunContinue) {
        this.errorRunContinue = errorRunContinue;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public Integer getJobTaskId() {
        return jobTaskId;
    }

    public void setJobTaskId(Integer jobTaskId) {
        this.jobTaskId = jobTaskId;
    }

    public String getCreateDt() {
        return createDt;
    }

    public void setCreateDt(String createDt) {
        this.createDt = createDt;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getScheduleLevel() {
        return scheduleLevel;
    }

    public void setScheduleLevel(Integer scheduleLevel) {
        this.scheduleLevel = scheduleLevel;
    }
}
