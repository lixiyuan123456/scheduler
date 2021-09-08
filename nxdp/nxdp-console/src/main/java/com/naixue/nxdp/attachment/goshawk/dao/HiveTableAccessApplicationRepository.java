package com.naixue.nxdp.attachment.goshawk.dao;

import java.util.List;

import com.naixue.nxdp.attachment.goshawk.model.HiveTableAccessApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HiveTableAccessApplicationRepository
        extends JpaRepository<HiveTableAccessApplication, Integer>,
        JpaSpecificationExecutor<HiveTableAccessApplication> {

    List<HiveTableAccessApplication> findByDbIdAndTableIdAndProxyAccountAndStatus(
            Integer dbId, Integer tableId, String proxyAccount, HiveTableAccessApplication.Status status);
}
