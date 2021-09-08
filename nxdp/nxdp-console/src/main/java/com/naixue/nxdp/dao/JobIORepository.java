package com.naixue.nxdp.dao;

import java.util.List;

import com.naixue.nxdp.model.JobIO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JobIORepository
        extends JpaRepository<JobIO, Integer>, JpaSpecificationExecutor<JobIO> {

    void deleteByJobId(final Integer jobId);

    List<JobIO> findByJobId(final Integer jobId);
}
