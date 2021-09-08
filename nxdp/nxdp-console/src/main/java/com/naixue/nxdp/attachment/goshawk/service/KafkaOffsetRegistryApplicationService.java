package com.naixue.nxdp.attachment.goshawk.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.naixue.nxdp.model.User;
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
import org.springframework.util.StringUtils;

import com.naixue.nxdp.attachment.goshawk.dao.KafkaOffsetRegistryApplicationRepository;
import com.naixue.nxdp.attachment.goshawk.model.KafkaOffsetRegistryApplication;
import com.naixue.nxdp.attachment.goshawk.model.KafkaOffsetRegistryApplication.Status;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaOffsetRegistryApplicationService {

    @Autowired
    private KafkaOffsetRegistryApplicationRepository kafkaOffsetRegistryApplicationRepository;

    @Transactional
    public KafkaOffsetRegistryApplication applykafkaOffsetRegistryApplication(
            KafkaOffsetRegistryApplication application, User currentUser) {
        Assert.notNull(application, "入参application不允许为空");
        application.setApplicant(currentUser.getPyName());
        application.setApplicantName(currentUser.getName());
        KafkaOffsetRegistryApplication registryApplication =
                kafkaOffsetRegistryApplicationRepository.save(application);
        return registryApplication;
    }

    public Page<KafkaOffsetRegistryApplication> getKafkaOffsetRegistryApplications(
            KafkaOffsetRegistryApplication queryCondition, Integer pageIndex, Integer pageSize) {
        return kafkaOffsetRegistryApplicationRepository.findAll(
                new Specification<KafkaOffsetRegistryApplication>() {

                    @Override
                    public Predicate toPredicate(
                            Root<KafkaOffsetRegistryApplication> root,
                            CriteriaQuery<?> query,
                            CriteriaBuilder cb) {
                        List<Predicate> conditions = new ArrayList<>();
                        if (queryCondition != null) {
                            if (!StringUtils.isEmpty(queryCondition.getApplicant())) {
                                conditions.add(cb.equal(root.get("applicant"), queryCondition.getApplicant()));
                            }
                        }
                        return cb.and(conditions.toArray(new Predicate[conditions.size()]));
                    }
                },
                new PageRequest(pageIndex, pageSize, new Sort(new Order(Direction.DESC, "id"))));
    }

    @Transactional
    public void assess(KafkaOffsetRegistryApplication application, User currentUser) {
        Assert.notNull(application, "入参application不允许为空");
        if (application.getId() == null) {
            throw new RuntimeException("Id不存在");
        }
        KafkaOffsetRegistryApplication originalApplication =
                kafkaOffsetRegistryApplicationRepository.getOne(application.getId());
        Status oldStatus =
                KafkaOffsetRegistryApplication.Status.getStatus(originalApplication.getStatus());
        Status newStatus = KafkaOffsetRegistryApplication.Status.getStatus(application.getStatus());
        // 原状态为待审批，则只能审批通过、审批拒绝
        if (oldStatus != Status.WAITING_ASSESS
                || (newStatus != Status.PASS && newStatus != Status.NOT_PASS)) {
            throw new RuntimeException("非法操作");
        }
        originalApplication.setThreshold(application.getThreshold());
        originalApplication.setAssessComment(application.getAssessComment());
        originalApplication.setStatus(application.getStatus());
        kafkaOffsetRegistryApplicationRepository.save(originalApplication);
    }

    @Transactional
    public void reedit(KafkaOffsetRegistryApplication application, User currentUser) {
        Assert.notNull(application, "入参application不允许为空");
        if (application.getId() == null) {
            throw new RuntimeException("Id不存在");
        }
        KafkaOffsetRegistryApplication originalApplication =
                kafkaOffsetRegistryApplicationRepository.getOne(application.getId());
        Status oldStatus =
                KafkaOffsetRegistryApplication.Status.getStatus(originalApplication.getStatus());
        if (oldStatus != Status.PASS) {
            throw new RuntimeException("非法操作");
        }
        originalApplication.setCluster(application.getCluster());
        originalApplication.setConsumer(application.getConsumer());
        originalApplication.setTopic(application.getTopic());
        originalApplication.setThreshold(application.getThreshold());
        originalApplication.setAssessComment(application.getAssessComment());
        kafkaOffsetRegistryApplicationRepository.save(originalApplication);
    }

    public void delete(Integer id, User currentUser) {
        log.debug("{}操作删除Id={}的工单", currentUser.getPyName(), id);
        kafkaOffsetRegistryApplicationRepository.delete(id);
    }
}
