package com.naixue.nxdp.attachment.goshawk.dao;

import java.util.List;

import com.naixue.nxdp.attachment.goshawk.model.ClusterHdfsCold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterHdfsColdRepository
        extends JpaRepository<ClusterHdfsCold, Integer>, JpaSpecificationExecutor<ClusterHdfsCold> {

    List<ClusterHdfsCold> findByDirLikeAndStatus(String dir, Integer status);
}
