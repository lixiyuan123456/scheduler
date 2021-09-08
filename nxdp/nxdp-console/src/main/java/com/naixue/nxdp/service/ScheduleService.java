package com.naixue.nxdp.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.naixue.nxdp.dao.JobConfigRepository;
import com.naixue.nxdp.dao.JobDependenciesRepository;
import com.naixue.nxdp.dao.JobScheduleRepository;
import com.naixue.zzdp.data.model.metadata.MetadataCommonTable;
import com.naixue.nxdp.model.JobConfig;
import com.naixue.nxdp.model.JobDependencies;
import com.naixue.nxdp.model.JobExecuteLog;
import com.naixue.nxdp.model.JobIO;
import com.naixue.nxdp.model.JobSchedule;
import com.naixue.nxdp.model.plugin.g6.Edge;
import com.naixue.nxdp.model.plugin.g6.Node;
import com.naixue.zzdp.platform.service.metadata.MetadataCommonTableService;

/**
 * @author: wangyu @Created by 2018/2/6
 */
@Service
public class ScheduleService {

    private static final String G6_NODES = "nodes";
    private static final String G6_EDGES = "edges";
    // @Autowired private JobExecuteLogRepository jobExecuteLogRepository;
    @Autowired
    private JobScheduleRepository jobScheduleRepository;
    @Autowired
    private JobConfigRepository jobConfigRepository;
    @Autowired
    private JobDependenciesRepository jobDependenciesRepository;
    @Autowired
    private JobExecuteLogService jobExecuteLogService;
    @Autowired
    private JobIOService jobIOService;
    @Autowired
    private MetadataCommonTableService metadataCommonTableService;

    /**
     * 获取当前job依赖的job信息
     *
     * @param jobId
     * @return
     */
    public Page<JobSchedule> getDependentJobs(Integer jobId) {
        if (jobId != null) {
            // 获取当前任务的依赖信息
            JobConfig jobConfig = jobConfigRepository.findOne(jobId);
            if (jobConfig != null && jobConfig.getDetails() != null) {
                JSONObject detailJson = JSONObject.parseObject(jobConfig.getDetails());
                if (detailJson != null && detailJson.containsKey("dependencies")) {
                    String dependencies = detailJson.getString("dependencies");
                    if (dependencies.trim().length() > 0) {
                        String[] dependentIds = dependencies.split(",");
                        Integer[] dependentIdArr = new Integer[dependentIds.length];
                        for (int i = 0; i < dependentIds.length; i++) {
                            dependentIdArr[i] = Integer.parseInt(dependentIds[i]);
                        }
                        Pageable pageable =
                                new PageRequest(0, Integer.MAX_VALUE, Sort.Direction.DESC, "createTime");
                        Page<JobSchedule> jobList =
                                jobScheduleRepository.findAllByJobIdIn(dependentIdArr, pageable);
                        return jobList;
                    }
                }
            }
        }
        return null;
    }

    public Object renderG6Data4DependencyRelationship(final Integer jobId) {
        Assert.notNull(jobId, "jobId is not allowed to be null.");
        JobConfig job = jobConfigRepository.findOne(jobId);
        Assert.notNull(job, "job[" + jobId + "] is not exist.");
        List<Node> g6Nodes = new ArrayList<>();
        List<Edge> g6Edges = new ArrayList<>();
        // 当前任务的父任务
        List<JobDependencies> parentJobDependencies = jobDependenciesRepository.findByJobId(jobId);
        if (!CollectionUtils.isEmpty(parentJobDependencies)) {
            int cursor = parentJobDependencies.size();
            for (int i = 0; i < parentJobDependencies.size(); i++) {
                JobDependencies parent = parentJobDependencies.get(i);
                JobConfig parentJobConfig = jobConfigRepository.findOne(parent.getDependentJobId());
                Node node =
                        Node.builder()
                                .id(parent.getDependentJobId() + "")
                                .label("【上游任务】" + parentJobConfig.getJobName())
                                .x(-300)
                                .y((i + 1) / 2 * 200 * (i % 2 == 0 ? -1 : 1))
                                .style(Node.Style.builder().fill("#FFFF00").stroke("#FFFF00").build())
                                .nodeType(1)
                                .build();
                g6Nodes.add(node);
                Edge edge =
                        Edge.builder()
                                .id("parentEdge_" + parent.getDependentJobId())
                                .source(parent.getDependentJobId() + "")
                                .target(jobId + "")
                                .build();
                g6Edges.add(edge);
                List<JobIO> jobIOs = jobIOService.getJobIOsByJobId(parent.getDependentJobId());
                if (!CollectionUtils.isEmpty(jobIOs)) {
                    for (int j = 0; j < jobIOs.size(); j++) {
                        cursor += j + 1;
                        JobIO jobIO = jobIOs.get(j);
                        Node ioNode =
                                Node.builder()
                                        .id(jobIO.getEnumType().name() + "_" + jobIO.getId())
                                        // .x((i + 2) * -300)
                                        // .y((j + 1) / 2 * 200 * (j % 2 == 0 ? -1 : 1))
                                        .x(-300)
                                        .y((cursor + 1) / 2 * 200 * (cursor % 2 == 0 ? -1 : 1))
                                        .style(Node.Style.builder().fill("#00FF00").stroke("#00FF00").build())
                                        .build();
                        g6Nodes.add(ioNode);
                        if (jobIO.getEnumMode() == JobIO.Mode.DATATABLE) {
                            MetadataCommonTable table =
                                    metadataCommonTableService
                                            .listMetadataCommonTablesByTableIdsWithFullName(
                                                    Lists.newArrayList(Integer.parseInt(jobIO.getValue())))
                                            .get(0);
                            ioNode.setLabel("【上游任务输出】" + table.getFullName());
                        } else if (jobIO.getEnumMode() == JobIO.Mode.HDFS) {
                            ioNode.setLabel("【上游任务输出】" + jobIO.getValue());
                        }
                        Edge ioEdge =
                                Edge.builder()
                                        .id(jobIO.getEnumType().name() + "_parentEdge_" + jobIO.getId())
                                        // .source(parent.getDependentJobId() + "")
                                        // .target(jobIO.getEnumType().name() + "_" + jobIO.getId())
                                        .source(jobIO.getEnumType().name() + "_" + jobIO.getId())
                                        .target(jobId + "")
                                        .build();
                        g6Edges.add(ioEdge);
                    }
                }
            }
        }
        // 当前任务的子任务
        List<JobDependencies> childJobDependencies =
                jobDependenciesRepository.findByDependentJobId(jobId);
        int cursor = 0;
        if (!CollectionUtils.isEmpty(childJobDependencies)) {
            cursor = childJobDependencies.size();
            for (int i = 0; i < childJobDependencies.size(); i++) {
                JobDependencies child = childJobDependencies.get(i);
                JobConfig childJobConfig = jobConfigRepository.findOne(child.getJobId());
                if (childJobConfig == null) {
                    continue;
                }
                Node node =
                        Node.builder()
                                .id(child.getJobId() + "")
                                .label("【下游任务】" + childJobConfig.getJobName() + "")
                                .x(300)
                                .y((i + 1) / 2 * 200 * (i % 2 == 0 ? -1 : 1))
                                .style(Node.Style.builder().fill("#0000FF").stroke("#0000FF").build())
                                .nodeType(1)
                                .build();
                g6Nodes.add(node);
                Edge edge =
                        Edge.builder()
                                .id("childEdge_" + child.getJobId())
                                .source(jobId + "")
                                .target(child.getJobId() + "")
                                .build();
                g6Edges.add(edge);
            }
        }
        // 当前任务的IO
        List<JobIO> jobIOs = jobIOService.getJobIOsByJobId(jobId);
        if (!CollectionUtils.isEmpty(jobIOs)) {
            for (int j = 0; j < jobIOs.size(); j++) {
                cursor += j;
                JobIO jobIO = jobIOs.get(j);
                Node ioNode =
                        Node.builder()
                                .id(jobIO.getEnumType().name() + "_" + jobIO.getId())
                                .x(300)
                                .y((cursor + 1) / 2 * 200 * (cursor % 2 == 0 ? -1 : 1))
                                .style(Node.Style.builder().fill("#00FF00").stroke("#00FF00").build())
                                .build();
                g6Nodes.add(ioNode);
                if (jobIO.getEnumMode() == JobIO.Mode.DATATABLE) {
                    MetadataCommonTable table =
                            metadataCommonTableService
                                    .listMetadataCommonTablesByTableIdsWithFullName(
                                            Lists.newArrayList(Integer.parseInt(jobIO.getValue())))
                                    .get(0);
                    ioNode.setLabel("【当前任务输出】" + table.getFullName());
                } else if (jobIO.getEnumMode() == JobIO.Mode.HDFS) {
                    ioNode.setLabel("【当前任务输出】" + jobIO.getValue());
                }
                Edge ioEdge =
                        Edge.builder()
                                .id(jobIO.getEnumType().name() + "_childEdge_" + jobIO.getId())
                                .source(jobId + "")
                                .target(jobIO.getEnumType().name() + "_" + jobIO.getId())
                                .build();
                g6Edges.add(ioEdge);
            }
        }
        Map<String, Object> result = new HashMap<>();
        g6Nodes.add(
                Node.builder()
                        .id(jobId + "")
                        .label("【当前任务】" + job.getJobName())
                        .style(Node.Style.builder().fill("#FF0000").stroke("#FF0000").build())
                        .build());
        result.put(G6_NODES, g6Nodes);
        result.put(G6_EDGES, g6Edges);
        return result;
    }

    /**
     * 获取job的依赖关系，包括前置任务和后置任务
     *
     * @param jobId
     * @return
     */
    public String getSimpleDependentRelation(Integer jobId) {
        // 获取当前任务依赖的任务信息
        List<JobDependencies> dependList = jobDependenciesRepository.findByJobId(jobId);
        // 获取依赖当前任务的任务信息
        List<JobDependencies> dependedList = jobDependenciesRepository.findByDependentJobId(jobId);

        // 存储所有待查询待任务ID
        Set<Integer> jobIdList = new HashSet<>();
        for (JobDependencies job : dependList) {
            jobIdList.add(job.getDependentJobId());
        }
        jobIdList.add(jobId);
        for (JobDependencies job : dependedList) {
            jobIdList.add(job.getJobId());
        }

        // 查询所有任务信息
        Integer[] jobIds = new Integer[jobIdList.size()];
        jobIdList.toArray(jobIds);
        List<JobSchedule> jobScheduleList = jobScheduleRepository.findAllByJobIdIn(jobIds);
        // 返回查询结果
        StringBuilder dependSb = new StringBuilder();
        for (JobDependencies job : dependList) {
            for (JobSchedule jobSchedule : jobScheduleList) {
                if (job.getDependentJobId().equals(jobSchedule.getJobId())) {
                    dependSb.append(
                            "<li class='list-group-item' onclick='viewJobRelation("
                                    + jobSchedule.getJobId()
                                    + ","
                                    + ("\"" + jobSchedule.getJobName() + "\"")
                                    + ")'>"
                                    + jobSchedule.getJobName()
                                    + "</li>");
                }
            }
        }

        StringBuilder currSb = new StringBuilder();
        for (JobSchedule jobSchedule : jobScheduleList) {
            if (jobSchedule.getJobId().equals(jobId)) {
                currSb.append(
                        "<li class='list-group-item'><a href='/scheduler/task/detail?schedulerId="
                                + jobSchedule.getJobId()
                                + "'>"
                                + jobSchedule.getJobName()
                                + "</a></li>");
            }
        }

        StringBuilder dependedSb = new StringBuilder();
        for (JobDependencies job : dependedList) {
            for (JobSchedule jobSchedule : jobScheduleList) {
                if (job.getJobId().equals(jobSchedule.getJobId())) {
                    dependedSb.append(
                            "<li class='list-group-item' onclick='viewJobRelation("
                                    + jobSchedule.getJobId()
                                    + ","
                                    + ("\"" + jobSchedule.getJobName() + "\"")
                                    + ")'>"
                                    + jobSchedule.getJobName()
                                    + "</li>");
                }
            }
        }

        StringBuilder resultSb = new StringBuilder();
        if (dependSb.length() > 0) {
            resultSb.append("<ul class='list-group schedule-relation' style='width:auto;'>");
            resultSb.append(dependSb);
            resultSb.append("</ul>");
            resultSb.append(
                    "<i style='line-height: 35px' class='glyphicon glyphicon-arrow-right font-green schedule-relation' title='依赖'></i>");
        }
        if (currSb.length() > 0) {
            resultSb.append("<ul class='list-group schedule-relation' style='width:auto;'>");
            resultSb.append(currSb);
            resultSb.append("</ul>");
        }
        if (dependedSb.length() > 0) {
            resultSb.append(
                    "<i style='line-height: 35px' class='glyphicon glyphicon-arrow-right font-green schedule-relation' title='依赖'></i>");
            resultSb.append(
                    "<ul class='list-group schedule-relation' style='width:auto; position: absolute;'>");
            resultSb.append(dependedSb);
            resultSb.append("</ul>");
        }
        return resultSb.toString();
    }

    /**
     * 获取job一周运行时长
     *
     * @param jobId
     * @return
     */
    public List<Long[]> getRunTimeList(Integer jobId) {
        List<Long[]> runTimeList = new ArrayList<>();
        List<JobExecuteLog> jobExecuteLogList =
                jobExecuteLogService.getLast7DaysAutoExecuteHistoryByJobId(jobId);
        if (CollectionUtils.isEmpty(jobExecuteLogList)) {
            return runTimeList;
        }
        for (JobExecuteLog jobExecuteLog : jobExecuteLogList) {
            Date executeStartTime = jobExecuteLog.getExecuteTime();
            Date executeEndTime = jobExecuteLog.getExecuteEndTime();
            List<Long> data = new LinkedList<>();
            data.add(executeStartTime.getTime());
            data.add(
                    executeEndTime == null
                            ? 0L
                            : (executeEndTime.getTime() - executeStartTime.getTime()) / 1000);
            runTimeList.add(data.toArray(new Long[data.size()]));
        }
        return runTimeList;
    }
}
