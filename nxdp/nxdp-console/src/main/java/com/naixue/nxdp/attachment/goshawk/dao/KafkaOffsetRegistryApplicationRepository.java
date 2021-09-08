package com.naixue.nxdp.attachment.goshawk.dao;

import com.naixue.nxdp.attachment.goshawk.model.KafkaOffsetRegistryApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface KafkaOffsetRegistryApplicationRepository
        extends JpaRepository<KafkaOffsetRegistryApplication, Integer>,
        JpaSpecificationExecutor<KafkaOffsetRegistryApplication> {
}
