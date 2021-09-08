package com.naixue.nxdp.dao;

import com.naixue.nxdp.model.JobRerunDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author: wangyu
 * @Created by 2018/1/31
 **/
public interface JobRerunDetailRepository extends JpaRepository<JobRerunDetail, Integer> {

    @Query("select detail from JobRerunDetail detail join detail.jobSchedule schedule where detail.jobId=schedule.jobId and detail.taskId=?1")
    List<JobRerunDetail> findJobScheduleByTaskId(Integer taskId);

    @Query("select detail from JobRerunDetail  detail join detail.jobExecuteLog log where detail.jobId=log.jobId and detail.taskId=log.taskId and detail.taskId=?1")
    List<JobRerunDetail> findJobExecuteLogByTaskId(Integer taskId);

    @Transactional
    @Modifying
    @Query(value = "update t_job_rerun_detail set operation_type=?1 where id=?2", nativeQuery = true)
    Integer changeOperationTypeById(Integer operationType, Integer id);

    @Transactional
    @Modifying
    @Query("update JobRerunDetail set operationType=?1 where id in(?2)")
    Integer changeOperationTypeByIdIn(Integer operationType, List<Integer> idList);

    List<JobRerunDetail> findAllByTaskId(Integer taskId);
}
