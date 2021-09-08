package com.naixue.nxdp.attachment.goshawk.dao;

import com.naixue.nxdp.attachment.goshawk.model.ClusterHdfsColdWhite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterHdfsColdWhiteRepository
        extends JpaRepository<ClusterHdfsColdWhite, Integer>,
        JpaSpecificationExecutor<ClusterHdfsColdWhite> {
}
