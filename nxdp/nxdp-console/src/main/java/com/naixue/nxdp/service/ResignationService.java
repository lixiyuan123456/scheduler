package com.naixue.nxdp.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Splitter;
import com.naixue.nxdp.dao.JobConfigRepository;
import com.naixue.nxdp.dao.JobScheduleRepository;
import com.naixue.nxdp.dao.ResignationRepository;
import com.naixue.nxdp.dao.UserRepository;
import com.naixue.zzdp.data.dao.zstream.ZstreamJobDao;
import com.naixue.zzdp.data.model.zstream.ZstreamJob;
import com.naixue.nxdp.model.JobConfig;
import com.naixue.nxdp.model.JobSchedule;
import com.naixue.nxdp.model.Resignation;
import com.naixue.nxdp.model.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ResignationService {

    @Autowired
    private ResignationRepository resignationRepository;

    @Autowired
    private JobConfigRepository jobConfigRepository;

    @Autowired
    private JobScheduleRepository jobScheduleRepository;

    @Autowired
    private ZstreamJobDao zstreamJobDao;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Executor taskExecutor;

    public Page<Resignation> page(Resignation condition, Integer pageIndex, Integer pageSize) {
        Specification<Resignation> specification =
                new Specification<Resignation>() {
                    @Override
                    public Predicate toPredicate(
                            Root<Resignation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                        List<Predicate> data = new ArrayList<>();
                        if (!StringUtils.isEmpty(condition.getHolder())) {
                            data.add(cb.equal(root.get("holder"), condition.getHolder()));
                        }
                        return cb.and(data.toArray(new Predicate[data.size()]));
                    }
                };
        Pageable pageable =
                new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.ASC, "id")));
        return resignationRepository.findAll(specification, pageable);
    }

    @Transactional
    public void resign(final String userId) {
        Assert.hasText(userId, "userId is not allowed to be null.");
        User currentUser = userService.getUserByUserId(userId, false);
        CountDownLatch latch = new CountDownLatch(2);
        taskExecutor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        collectCommonJobsFromJobSchedule(currentUser);
                        latch.countDown();
                        log.info("collect {}'s common jobs is done.", currentUser.getPyName());
                    }
                });
        taskExecutor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        collectZstreamJobsFromZstreamJob(currentUser);
                        latch.countDown();
                        log.info("collect {}'s zstream jobs is done.", currentUser.getPyName());
                    }
                });
        try {
            latch.await();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        currentUser.setStatus(User.Status.OFF.getCode());
        userRepository.save(currentUser);
        log.info("collect {}'s all jobs is done.", currentUser.getPyName());
    }

    @Transactional
    public void transferJobs(final User currentUser, final List<Resignation> resignations) {
        if (CollectionUtils.isEmpty(resignations)) {
            return;
        }
        log.info(
                "start to save {}'s transfer jobs to database:{}.",
                currentUser.getPyName(),
                JSON.toJSONString(resignations));
        List<Resignation> transferJobs = new ArrayList<>();
        for (Resignation resignation : resignations) {
            Resignation source = resignationRepository.findOne(resignation.getId());
            if (!currentUser.getId().equals(source.getHolder())) {
                throw new RuntimeException("unauthorized operation");
            }
            source.setTargetDeveloper(resignation.getTargetDeveloper());
            source.setTargetReceiver(resignation.getTargetReceiver());
            source.setStatus(Resignation.Status.getStatus(Resignation.Status.DONE));
            transferJobs.add(source);
        }
        final List<Resignation> finalResignations = resignationRepository.save(transferJobs);
        taskExecutor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        log.info(
                                "start to transfer {}'s jobs:{}.",
                                currentUser.getPyName(),
                                JSON.toJSONString(finalResignations));
                        doTransferJobs(finalResignations);
                        log.info(
                                "transfer {}'s jobs is done:{}.",
                                currentUser.getPyName(),
                                JSON.toJSONString(finalResignations));
                    }
                });
    }

    @Transactional
    private void collectCommonJobsFromJobSchedule(final User currentUser) {
        log.info("collect {}'s common jobs now.", currentUser.getPyName());
        List<JobSchedule> jobs =
                jobScheduleRepository.findDistinctByUserIdOrReceiverContaining(
                        currentUser.getId(), currentUser.getId());
        if (CollectionUtils.isEmpty(jobs)) {
            return;
        }
        List<Resignation> resignations = new ArrayList<>();
        for (JobSchedule job : jobs) {
            Resignation resignation =
                    Resignation.builder()
                            .jobId(job.getJobId())
                            .jobName(job.getJobName())
                            .enumJobType(Resignation.Type.COMMON_JOB)
                            .sourceDeveloper(job.getUserId())
                            .sourceReceiver(job.getReceiver())
                            .holder(currentUser.getId())
                            .build();
            resignations.add(resignation);
        }
        resignationRepository.save(resignations);
    }

    @Transactional
    private void collectZstreamJobsFromZstreamJob(final User currentUser) {
        log.info("collect {}'s zstream jobs now.", currentUser.getPyName());
        List<ZstreamJob> jobs =
                zstreamJobDao.findDistinctByCreatorOrReceiversContaining(
                        currentUser.getPyName(), currentUser.getPyName());
        if (CollectionUtils.isEmpty(jobs)) {
            return;
        }
        List<Resignation> resignations = new ArrayList<>();
        for (ZstreamJob job : jobs) {
            StringBuilder newReceivers = new StringBuilder();
            if (!StringUtils.isEmpty(job.getReceivers())) {
                Iterator<String> iterator = Splitter.on(",").split(job.getReceivers()).iterator();
                while (iterator.hasNext()) {
                    User user = userService.getUserByUserPyName(iterator.next());
                    newReceivers.append(user == null ? "" : user.getId());
                }
            }
            Resignation resignation =
                    Resignation.builder()
                            .jobId(job.getId())
                            .jobName(job.getName())
                            .enumJobType(Resignation.Type.ZSTREAM_JOB)
                            .sourceDeveloper(
                                    job.getCreator().equals(currentUser.getPyName())
                                            ? currentUser.getId()
                                            : userService.getUserByUserPyName(currentUser.getPyName()).getId())
                            .sourceReceiver(newReceivers.toString())
                            .holder(currentUser.getId())
                            .build();
            resignations.add(resignation);
        }
        resignationRepository.save(resignations);
    }

    @Transactional
    public void doTransferJobs(final List<Resignation> resignations) {
        if (CollectionUtils.isEmpty(resignations)) {
            return;
        }
        Map<String, User> usersMap = userService.findAllInMap();
        List<JobConfig> commonJobConfigs = new ArrayList<>();
        List<JobSchedule> commonJobs = new ArrayList<>();
        List<ZstreamJob> zstreamJobs = new ArrayList<>();
        for (Resignation resignation : resignations) {
            log.info(
                    "there are {} jobs is need to transfer,now its time to start to transfer job:{}.",
                    resignations.size(),
                    JSON.toJSONString(resignation));
            if (resignation.getJobType() == Resignation.Type.COMMON_JOB.getCode()) {
                JobConfig sourceJobConfig = jobConfigRepository.findOne(resignation.getJobId());
                sourceJobConfig.setUserId(resignation.getTargetDeveloper());
                JSONObject jobConfigJson = JSON.parseObject(sourceJobConfig.getDetails());
                jobConfigJson.getJSONObject("scheduler").put("receiver", resignation.getTargetReceiver());
                sourceJobConfig.setDetails(JSON.toJSONString(jobConfigJson));
                commonJobConfigs.add(sourceJobConfig);
                JobSchedule source = jobScheduleRepository.findOne(resignation.getJobId());
                source.setDeptId(usersMap.get(resignation.getTargetDeveloper()).getDeptId());
                source.setUserId(resignation.getTargetDeveloper());
                source.setUserName(usersMap.get(resignation.getTargetDeveloper()).getName());
                source.setReceiver(resignation.getTargetReceiver());
                commonJobs.add(source);
            } else if (resignation.getJobType() == Resignation.Type.ZSTREAM_JOB.getCode()) {
                ZstreamJob source = zstreamJobDao.findOne(resignation.getJobId());
                User developer = usersMap.get(resignation.getTargetDeveloper());
                source.setCreator(developer.getPyName());
                StringBuilder receivers = new StringBuilder();
                Iterator<String> iterator =
                        Splitter.on(",").split(resignation.getTargetReceiver()).iterator();
                while (iterator.hasNext()) {
                    User receiver = usersMap.get(iterator.next());
                    receivers.append(receiver.getPyName()).append(",");
                }
                String sReceivers = receivers.toString();
                source.setReceivers(
                        StringUtils.isEmpty(sReceivers)
                                ? ""
                                : sReceivers.substring(0, sReceivers.length() - 1));
                zstreamJobs.add(source);
            }
        }
        jobConfigRepository.save(commonJobConfigs);
        jobScheduleRepository.save(commonJobs);
        zstreamJobDao.save(zstreamJobs);
        log.info(
                "transfer job is done,update t_job_config {} rows,update t_job_schedule {} rows,update t_zstream_job {} rows.",
                commonJobConfigs.size(),
                commonJobs.size(),
                zstreamJobs.size());
    }
}
