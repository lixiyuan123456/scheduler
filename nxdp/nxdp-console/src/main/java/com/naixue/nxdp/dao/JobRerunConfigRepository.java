package com.naixue.nxdp.dao;

import com.naixue.nxdp.model.JobRerunConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: wangyu @Created by 2018/1/31
 */
public interface JobRerunConfigRepository
        extends JpaRepository<JobRerunConfig, Integer>, JpaSpecificationExecutor<JobRerunConfig> {

    @Transactional
    @Modifying
    @Query(value = "update t_job_rerun_config set status=?1 where id=?2", nativeQuery = true)
    Integer updateStatusById(Integer status, Integer taskId);
}
