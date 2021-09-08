package com.naixue.nxdp.attachment.goshawk.dao;

import com.naixue.nxdp.attachment.goshawk.model.ClusterFlumeTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterFlumeTaskRepository extends JpaRepository<ClusterFlumeTask, Integer> {
}
