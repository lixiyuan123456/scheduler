package com.naixue.nxdp.dao;

import com.naixue.nxdp.model.JobUpdateRecord;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author: wangyu
 * @Created by 2018/1/30
 **/
public interface JobUpdateRecordRepository extends JpaRepository<JobUpdateRecord, Integer> {

    List<JobUpdateRecord> findAllByJobId(Integer jobId, Sort sort);
}
