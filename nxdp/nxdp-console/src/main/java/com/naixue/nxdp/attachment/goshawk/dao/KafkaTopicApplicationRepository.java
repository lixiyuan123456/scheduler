package com.naixue.nxdp.attachment.goshawk.dao;

import java.util.List;

import com.naixue.nxdp.attachment.goshawk.model.KafkaTopicApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface KafkaTopicApplicationRepository
        extends JpaRepository<KafkaTopicApplication, Integer>,
        JpaSpecificationExecutor<KafkaTopicApplication> {

    List<KafkaTopicApplication> findByTopicNameAndStatus(
            String topicName, KafkaTopicApplication.Status status);
}
