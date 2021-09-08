package com.naixue.nxdp.dao;

import java.util.Date;
import java.util.List;

import com.naixue.nxdp.model.JobExecuteLog;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: wangyu @Created by 2018/1/29
 */
public interface JobExecuteLogRepository
        extends JpaRepository<JobExecuteLog, Integer>, JpaSpecificationExecutor<JobExecuteLog> {

    List<JobExecuteLog> findAllByJobId(Integer jobId, Sort sort);

    List<JobExecuteLog> findTop50ByJobId(Integer jobId, Sort sort);

    List<JobExecuteLog> findAllByJobIdAndJobState(Integer jobId, Integer jobState);

    List<JobExecuteLog> findAllByJobIdAndExecuteTimeGreaterThanEqual(Integer jobId, Date executeTime);

    List<JobExecuteLog> findAllByTaskId(Integer taskId);

    List<JobExecuteLog> findByTaskIdAndExecuteTimeBetween(Integer taskId, Date start, Date end);

    List<JobExecuteLog> findByJobIdAndTaskIdAndExecuteTimeBetweenOrderByCreateTimeAsc(
            Integer jobId, Integer taskId, Date start, Date end);

    JobExecuteLog findFirst1ByJobIdAndExecuteTimeBetween(Integer jobId, Date start, Date end);

    @Transactional
    @Modifying
    @Query(value = "update t_job_execute_log set job_state=?1 where id=?2", nativeQuery = true)
    Integer updateStateById(Integer state, Integer executeId);

    JobExecuteLog findFirst1ByJobIdAndJobStateOrderByIdAsc(Integer jobId, Integer jobState);

    @Transactional
    @Modifying
    @Query(value = "update JobExecuteLog set jobState=?1 where id in(?2) and jobState<3")
    Integer updateStateByIdIn(Integer state, List<Integer> executeIdList);

    List<JobExecuteLog> findByTaskIdAndJobStateAndExecuteTimeGreaterThanEqual(
            Integer taskId, Integer jobState, Date executeTime);

    List<JobExecuteLog> findByJobStateAndExecuteTimeGreaterThanEqual(
            Integer jobState, Date executeTime);

    List<JobExecuteLog> findByUserIdAndExecuteTimeGreaterThanEqual(String userId, Date executeTime);

    List<JobExecuteLog> findByUserIdAndExecuteTimeGreaterThanEqualAndJobState(
            String userId, Date executeTime, Integer jobState);

    List<JobExecuteLog> findByCreateTimeBetween(Date start, Date end);

    Integer countDistinctJobIdByExecuteTimeBetween(Date start, Date end);

    Integer countByTypeAndExecuteTimeBetween(Integer type, Date start, Date end);
}
