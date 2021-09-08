package com.naixue.nxdp.dao;

import java.sql.Timestamp;
import java.util.List;

import com.naixue.nxdp.model.JobConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: wangyu @Created by 2017/11/8
 */
public interface JobConfigRepository extends JpaRepository<JobConfig, Integer> {

    /**
     * 根据job名称精确查询job信息
     *
     * @param jobName
     * @return
     */
    List<JobConfig> findByJobName(String jobName);

    List<JobConfig> findByJobNameContaining(String jobName);

    List<JobConfig> findByJobNameContainingAllIgnoringCase(String jobName);

    List<JobConfig> findByJobNameAndCreated(String jobName, Timestamp created);

    @Transactional
    @Modifying
    @Query(
            value =
                    "insert into t_job_config(job_name, type, status, user_id, description, tags, details, created) "
                            + "values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)",
            nativeQuery = true)
    Integer saveJob(
            String jobName,
            int type,
            int status,
            String userId,
            String description,
            String tags,
            String details,
            Timestamp created);

    @Transactional
    @Modifying
    @Query(
            value =
                    "update t_job_config "
                            + "set job_name=?1, status=?2, user_id=?3, description=?4, tags=?5, details=?6 where id=?7",
            nativeQuery = true)
    Integer update(
            String jobName,
            int status,
            String userId,
            String description,
            String tags,
            String details,
            int jobId);

    JobConfig findFirst1ByJobNameEqualsAndStatusIn(String jobName, Integer[] statuses);

    @Transactional
    @Modifying
    @Query(
            value =
                    "update t_job_config "
                            + "set code_edit_lock=?1, editer_id=?2, editer_name=?3 where id=?4",
            nativeQuery = true)
    Integer updateEditLock(Integer codeEditLock, String editerId, String editerName, Integer jobId);

    @Transactional
    @Modifying
    @Query("update JobConfig " + "set status=?1, details=?2 where id=?3")
    Integer updateStatusAndDetails(Integer status, String details, Integer jobId);
}
