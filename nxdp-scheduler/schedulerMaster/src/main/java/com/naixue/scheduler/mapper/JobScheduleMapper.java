package com.naixue.scheduler.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.naixue.scheduler.model.JobSchedule;

public interface JobScheduleMapper {

  /** 在线任务 */
  @Select("select * from t_job_schedule where status = 1")
  List<JobSchedule> findOnlineJobs() throws Exception;

  /** 已被加载到Quartz中的任务 */
  @Select("select * from t_job_schedule where load_in_quartz_status = 1")
  List<JobSchedule> findLoadedInQuartzJobs() throws Exception;

  @Update(
      "update t_job_schedule t set t.load_in_quartz_status = #{loadInQuartzStatus},t.version = t.version + 1  where t.job_id = #{jobId} and t.version = #{version}")
  int changeLoadInQuartzStatus(
      @Param("jobId") Integer jobId,
      @Param("version") Integer version,
      @Param("loadInQuartzStatus") Integer loadInQuartzStatus)
      throws Exception;
}
