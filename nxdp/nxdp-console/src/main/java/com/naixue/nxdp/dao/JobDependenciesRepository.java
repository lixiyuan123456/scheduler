package com.naixue.nxdp.dao;

import com.naixue.nxdp.model.JobDependencies;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author: wangyu
 * @Created by 2018/1/29
 **/
public interface JobDependenciesRepository extends JpaRepository<JobDependencies, Integer> {

    boolean existsByDependentJobIdEquals(int dependentJobId);

    List<JobDependencies> findByJobId(Integer jobId);

    List<JobDependencies> findByDependentJobId(Integer jobId);

    List<JobDependencies> findByDependentJobIdIn(Integer[] jobIds);
}
