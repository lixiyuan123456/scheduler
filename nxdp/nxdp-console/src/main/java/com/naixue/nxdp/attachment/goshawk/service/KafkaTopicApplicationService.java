package com.naixue.nxdp.attachment.goshawk.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.naixue.nxdp.model.User;
import com.naixue.nxdp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.naixue.nxdp.attachment.goshawk.GoshawkCfg;
import com.naixue.nxdp.attachment.goshawk.dao.KafkaTopicApplicationRepository;
import com.naixue.nxdp.attachment.goshawk.model.KafkaTopicApplication;
import com.naixue.nxdp.attachment.util.KafkaUtils;

@Service
public class KafkaTopicApplicationService {

    @Autowired
    private KafkaTopicApplicationRepository kafkaTopicApplicationRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public KafkaTopicApplication applyKafkaTopic(
            KafkaTopicApplication application, User currentUser) {
        validateRepeatedTopicName(application.getTopicName());
        User u = userService.getUserByUserId(currentUser.getId());
        application.setCreator(currentUser.getPyName());
        application.setCreatorName(currentUser.getName());
        // application.setCreatorDeptName(u.getDepartment() == null ? "" : u.getDepartment().getName());
        application.setCreatorDeptName(u.getDeptName());
        application.setCreateTime(new Date());
        application.setModifyTime(new Date());
        return kafkaTopicApplicationRepository.save(application);
    }

    public Page<KafkaTopicApplication> listKafkaTopicApplications(
            KafkaTopicApplication queryCondition, Integer pageIndex, Integer pageSize) {
        return kafkaTopicApplicationRepository.findAll(
                new Specification<KafkaTopicApplication>() {
                    @Override
                    public Predicate toPredicate(
                            Root<KafkaTopicApplication> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                        List<Predicate> conditions = new ArrayList<>();
                        if (queryCondition != null) {
                            if (!StringUtils.isEmpty(queryCondition.getCreator())) {
                                conditions.add(cb.equal(root.get("creator"), queryCondition.getCreator()));
                            }
                        }
                        return cb.and(conditions.toArray(new Predicate[conditions.size()]));
                    }
                },
                new PageRequest(pageIndex, pageSize, new Sort(new Order(Direction.DESC, "id"))));
    }

    @Transactional
    public void assessKafkaTopicApplication(
            Integer id, String topicName, String assessComment, KafkaTopicApplication.Status status) {
        Assert.notNull(id, "入参id不允许为空");
        Assert.hasText(topicName, "入参topicName不允许为空");
        // Assert.hasText(assessComment, "入参assessComment不允许为空");
        KafkaTopicApplication application = kafkaTopicApplicationRepository.findOne(id);
        if (KafkaTopicApplication.Status.NOT_PASS == status) {
            application.setStatus(status);
            application.setAssessComment(assessComment == null ? "" : assessComment);
        } else if (KafkaTopicApplication.Status.PASS == status) {
            validateRepeatedTopicName(topicName);
            application.setTopicName(topicName);
            application.setStatus(status);
            application.setAssessComment(assessComment == null ? "" : assessComment);
            createTopic(topicName, application);
        }
        kafkaTopicApplicationRepository.save(application);
    }

    private void validateRepeatedTopicName(String topicName) {
        List<KafkaTopicApplication> applications =
                kafkaTopicApplicationRepository.findByTopicNameAndStatus(
                        topicName, KafkaTopicApplication.Status.PASS);
        if (!CollectionUtils.isEmpty(applications)) {
            throw new RuntimeException("Kafka Topic 名称重复！！！");
        }
    }

    // 每天规模小于50G   5个分区
    // 每天规模大于50G小于100G   10个分区
    // 每天规模大于100G小于500G   15个分区
    // 每天规模大于500G   25个分区
    // dailyDataCapacity   单位(M)
    private void createTopic(String topicName, KafkaTopicApplication application) {
        KafkaUtils.createTopic(
                GoshawkCfg.KAFKA_CLUSTER_HOSTS,
                topicName,
                application.getPartitions(),
                GoshawkCfg.KAFKA_TOPIC_REPLICATION_FACTOR);
    }
}
