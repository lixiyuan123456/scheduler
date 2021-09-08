package com.naixue.dp.model;

import lombok.Data;

/** Created by sunzhiwei on 2018/1/23. */
@Data
public class JobModel implements Comparable<Object> {
  private int jobId;
  private String scheduleTime; // 数据库中run_time
  private int jobType;
  private String parent;
  private int jobLevel;
  private String queryName;
  private String namespace;
  private String moblies;
  private int type;
  private int retry;
  private int retryTimeSpan;
  private int parallelRun;
  private String chooseTime;
  private String userId;
  private String jobName;
  private int isMonitor;
  private String userNames;

  public JobModel(
      int jobId,
      String queryName,
      String namespace,
      String userNames,
      int type,
      int retry,
      int retryTimeSpan,
      int parallelRun,
      String chooseTime,
      String jobName,
      int isMonitor) {
    this.jobId = jobId;
    this.queryName = queryName;
    this.namespace = namespace;
    this.userNames = userNames;
    this.type = type;
    this.retry = retry;
    this.retryTimeSpan = retryTimeSpan;
    this.parallelRun = parallelRun;
    this.chooseTime = chooseTime;
    this.jobName = jobName;
    this.isMonitor = isMonitor;
  }

  public JobModel(
      int jobId, String scheduleTime, int jobType, int jobLevel, String userId, String jobName) {
    this.jobId = jobId;
    this.scheduleTime = scheduleTime;
    this.jobType = jobType;
    this.jobLevel = jobLevel;
    this.userId = userId;
    this.jobName = jobName;
  }

  public JobModel(int jobId, String scheduleTime, int jobType, int jobLevel) {
    this.jobId = jobId;
    this.scheduleTime = scheduleTime;
    this.jobType = jobType;
    this.jobLevel = jobLevel;
  }

  public boolean equals(Object object) {
    if (object instanceof JobModel) {
      JobModel jobModel = (JobModel) object;
      if (this.queryName == jobModel.getQueryName()) {
        return true;
      }
    }
    return false;
  }

  public int hashCode() {
    return this.queryName.hashCode();
  }

  public String toString() {
    return this.jobId
        + "#"
        + this.scheduleTime
        + "#"
        + this.jobType
        + "#"
        + this.jobLevel
        + "#"
        + this.queryName
        + "#"
        + this.userNames
        + "#"
        + this.retry
        + "#"
        + this.retryTimeSpan
        + "#"
        + this.chooseTime
        + "#"
        + this.jobName
        + "#"
        + this.isMonitor;
  }

  public static void main(String[] args) {
    //        JobModel jobModel = new JobModel(1,"2","2015");
    //        JobModel jobModel1 = new JobModel(1,"3","2015");
    //        JobModel jobModel2 = new JobModel(2,"2","2015");
    //        Set<JobModel> set = new HashSet<JobModel>();
    //        set.add(jobModel);
    //        set.add(jobModel1);
    //        set.add(jobModel2);
    //        for (JobModel job : set){
    //            System.out.println(job.getJobId());
    //        }
  }

  public int compareTo(Object o) {
    JobModel target = (JobModel) o;
    int executeId = Integer.parseInt(queryName);
    int targetId = Integer.parseInt(target.getQueryName());
    int result = executeId > targetId ? 1 : (executeId == targetId ? 0 : -1);
    return result;
  }
}
