package com.naixue.nxdp.dao;

import java.util.Date;
import java.util.List;

import com.naixue.nxdp.model.JobSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface JobScheduleRepository
        extends JpaRepository<JobSchedule, Integer>, JpaSpecificationExecutor<JobSchedule> {

    @Transactional
    @Modifying
    @Query(value = "update t_job_schedule set status=?1 where job_id=?2", nativeQuery = true)
    Integer updateStatusByJobId(Integer status, Integer jobId);

    @Transactional
    @Modifying
    @Query("update JobSchedule set job_state=?1 where job_id=?2")
    Integer updateJobStateByJobId(Integer jobState, Integer jobId);

    Page<JobSchedule> findAllByStatusInAndJobNameContains(
            Integer[] statuses, String jobName, Pageable pageable);

    @Query(
            "from JobSchedule t where t.status in :statuses and (t.jobName like :queryString or outValue like :queryString)")
    Page<JobSchedule> findSelectableJobSchedules(
            @Param("statuses") Integer[] statuses,
            @Param("queryString") String queryString,
            Pageable pageable);

    Page<JobSchedule> findAllByJobIdIn(Integer[] jobIds, Pageable pageable);

    List<JobSchedule> findAllByJobIdIn(Integer[] jobIds);

    List<JobSchedule> findAllByJobIdInAndStatus(Integer[] jobIds, Integer status);

    @Transactional
    @Modifying
    @Query(
            value =
                    "update t_job_schedule set "
                            + "`dept_id`=?1, "
                            + "`user_id`=?2, "
                            + "`user_name`=?3, "
                            + "`job_name`=?4, "
                            + "`job_desc`=?5, "
                            + "`status`=?6, "
                            + "`job_type`=?7, "
                            + "`dispatch_command`=?8, "
                            + "`run_time`=?9, "
                            + "`job_level`=?10, "
                            + "`is_monitor`=?11, "
                            + "`parallel_run`=?12, "
                            + "`error_run_continue`=?13, "
                            + "`retry`=?14, "
                            + "`receiver`=?15, "
                            + "`hadoop_queue_id`=?16, "
                            + "`delay_warn`=?17, "
                            + "`update_id`=?18, "
                            + "`update_name`=?19, "
                            + "`spark_version`=?20, "
                            + "`next_fire_time`=?21, "
                            + "`schedule_level`=?22 "
                            + "where `job_id`=?23 ",
            nativeQuery = true)
    Integer update(
            Integer deptId,
            String userId,
            String userName,
            String jobName,
            String jobDesc,
            Integer status,
            Integer jobType,
            String dispatchCommand,
            String runTime,
            Integer jobLevel,
            Integer isMonitor,
            Integer parallelRun,
            Integer errorRunContinue,
            Integer retry,
            String receiver,
            Integer hadoopQueueId,
            Integer delayWarn,
            String updateId,
            String updateName,
            String sparkVersion,
            Date nextFireTime,
            Integer schedule,
            Integer jobId);

    @Transactional
    @Modifying
    @Query(
            value = "update t_job_schedule set `schedule_level`=?1 where `job_id`=?2 ",
            nativeQuery = true)
    Integer update(Integer scheduleLevel, Integer jobId);

    @Transactional
    @Modifying
    @Query("update JobSchedule set jobState=?1 where jobId in (?2)")
    Integer updateJobStateByJobIdIn(Integer jobState, List<Integer> jobIdList);

    @Transactional
    @Modifying
    @Query("update JobSchedule set jobState=5 where jobId in (?1)")
    Integer killAllJob(List<Integer> jobIdList);

    List<JobSchedule> findAllByStatusAndUserId(Integer status, String userId);

    List<JobSchedule> findAllByStatus(Integer status);

    List<JobSchedule> findAllByStatusAndDeptId(Integer status, Integer deptId);

    List<JobSchedule> findByCreateTimeBetween(Date start, Date end);

    Long countByCreateTimeLessThanEqual(Date end);

    JobSchedule findByJobId(Integer jobId);

    List<JobSchedule> findDistinctByUserIdOrReceiverContaining(
            final String userId, final String receiver);
}
