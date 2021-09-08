package com.naixue.nxdp.dao.mapper;

import java.util.List;

import com.naixue.nxdp.model.JobConfig;
import com.naixue.nxdp.model.JobExecuteLog;
import com.naixue.nxdp.model.JobSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface JobExecuteLogMapper {

    @Select(
            "SELECT t2.* FROM t_job_schedule t1,t_job_execute_log t2 WHERE t1.job_id = t2.job_id AND t1.job_type = #{jobType.id} AND t2.job_state = #{jobState.id}")
    List<JobExecuteLog> findJobExecuteLogs(
            @Param("jobType") JobConfig.JobType jobType,
            @Param("jobState") JobSchedule.JobState jobState);
}
