package com.naixue.nxdp.dao;

import java.util.List;

import com.naixue.nxdp.model.JobWorkPendingQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface JobWorkPendingQueueRepository
        extends JpaRepository<JobWorkPendingQueue, Integer>,
        JpaSpecificationExecutor<JobWorkPendingQueue> {

    List<JobWorkPendingQueue> findByStatus(final Integer status);

    @Transactional
    @Modifying
    @Query(
            "UPDATE JobWorkPendingQueue t SET t.status = :status,t.version = :version + 1 WHERE t.id = :id AND t.version = :version")
    int updateStatusById(
            @Param("id") final int id,
            @Param("status") final int status,
            @Param("version") final int version);

    @Transactional
    @Modifying
    @Query(
            "UPDATE JobWorkPendingQueue t SET t.status = 1,t.version = 1 WHERE t.batchNumber = :batchNumber AND t.version = 0")
    int updateStatus2End(@Param("batchNumber") final String batchNumber);
}
