package com.naixue.nxdp.service.impl;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.naixue.nxdp.dao.JobExecuteLogRepository;
import com.naixue.nxdp.dao.JobRerunConfigRepository;
import com.naixue.nxdp.dao.JobRerunDetailRepository;
import com.naixue.nxdp.dao.JobScheduleRepository;
import com.naixue.nxdp.model.JobExecuteLog;
import com.naixue.nxdp.model.JobRerunConfig;
import com.naixue.nxdp.model.JobRerunDetail;
import com.naixue.nxdp.model.JobSchedule;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.service.JobExecuteLogService;
import com.naixue.nxdp.service.RerunService;

/**
 * @author: wangyu @Created by 2018/2/6
 */
@Service
public class RerunServiceImpl implements RerunService {

    @Autowired
    private JobScheduleRepository jobScheduleRepository;
    @Autowired
    private JobRerunConfigRepository jobRerunConfigRepository;
    @Autowired
    private JobRerunDetailRepository jobRerunDetailRepository;
    @Autowired
    private JobExecuteLogRepository jobExecuteLogRepository;

    // @Autowired private JobExecuteLogService jobExecuteLogService;

    /**
     * 提交批量重跑job
     *
     * @param jobIds      需重跑的job列表
     * @param runTime     重跑时间
     * @param beginJobIds 开始任务
     * @param user        用户信息
     * @return
     */
    @Override
    public Map<String, Object> submitRerunJobs(
            String jobIds, Timestamp runTime, String beginJobIds, User user) {
        Map<String, Object> resultMap = new HashMap<>();
        String status = "failure";
        String msg = "";
        if (jobIds != null && jobIds.trim().length() > 0) {
            //  判断是否所有的任务都处于下线状态
            String[] arr = jobIds.split(",");
            Integer[] jobIdArr = new Integer[arr.length];
            for (int i = 0; i < arr.length; i++) {
                jobIdArr[i] = Integer.valueOf(arr[i]);
            }
            List<JobSchedule> jobScheduleList =
                    jobScheduleRepository.findAllByJobIdInAndStatus(jobIdArr, 1);
            boolean allOffline = false;
            if (jobScheduleList.size() == 0) {
                allOffline = true;
            }
            if (allOffline) {
                msg = "提交的Job全部不合法 （调度状态不是上线， 不允许批量操作）";
            } else {
                // 将所有的任务Id保存到重跑历史记录表中
                String taskName = user.getPyName() + (int) (Math.random() * 10000);
                JobRerunConfig jobRerunConfig =
                        new JobRerunConfig(
                                beginJobIds,
                                user.getId(),
                                2,
                                taskName,
                                0,
                                runTime,
                                Timestamp.valueOf(LocalDateTime.now()),
                                Timestamp.valueOf(LocalDateTime.now()));
                jobRerunConfig = jobRerunConfigRepository.save(jobRerunConfig);
                Integer rerunTaskId = jobRerunConfig.getId();
                if (rerunTaskId > 0) {
                    // 将任务列表存储到t_job_rerun_detail表中
                    List<JobRerunDetail> jobRerunDetails = new ArrayList<>();
                    for (JobSchedule jobSchedule : jobScheduleList) {
                        JobRerunDetail jobRerunDetail =
                                new JobRerunDetail(
                                        rerunTaskId,
                                        jobSchedule.getJobId(),
                                        0,
                                        Timestamp.valueOf(LocalDateTime.now()),
                                        Timestamp.valueOf(LocalDateTime.now()));
                        jobRerunDetails.add(jobRerunDetail);
                    }
                    jobRerunDetails = jobRerunDetailRepository.save(jobRerunDetails);
                    status = "success";
                    resultMap.put("task", jobRerunConfig);
                }
            }
        }
        resultMap.put("status", status);
        resultMap.put("msg", msg);
        return resultMap;
    }

    /**
     * 获取重跑job明细
     *
     * @param taskId 重跑任务ID
     * @return
     */
    @Override
    public Map<String, Object> jobRerunDetail(Integer taskId) {
        Map<String, Object> resultMap = new HashMap<>();
        if (taskId != null) {
            JobRerunConfig jobRerunConfig = jobRerunConfigRepository.findOne(taskId);
            if (jobRerunConfig != null) {
                int status = jobRerunConfig.getStatus();
                // 如果任务未提交，则返回到未提交页面
                if (status == 0) {
                    List<JobRerunDetail> jobList = jobRerunDetailRepository.findJobScheduleByTaskId(taskId);
                    List<Object> details = new ArrayList<>();
                    for (JobRerunDetail jobRerunDetail : jobList) {
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("id", jobRerunDetail.getId());
                        detail.put("taskId", jobRerunDetail.getTaskId());
                        detail.put("jobId", jobRerunDetail.getJobId());
                        detail.put("name", jobRerunDetail.getJobSchedule().getJobName());
                        detail.put("status", jobRerunDetail.getJobSchedule().getStatus());
                        detail.put("state", status);
                        detail.put("runTime", jobRerunConfig.getRunTime());
                        detail.put("createTime", jobRerunDetail.getCreateTime());
                        detail.put("updateTime", jobRerunDetail.getUpdateTime());
                        detail.put("operationType", jobRerunDetail.getOperationType());
                        details.add(detail);
                    }
                    resultMap.put("taskId", taskId);
                    resultMap.put("beginJobs", jobRerunConfig.getBeginJobs());
                    resultMap.put("jobList", details);
                    resultMap.put("status", 0);
                } else { // 如果任务已提交，则返回到已提交页面
                    List<JobRerunDetail> jobList = jobRerunDetailRepository.findJobExecuteLogByTaskId(taskId);
                    List<Object> details = new ArrayList<>();
                    for (JobRerunDetail jobRerunDetail : jobList) {
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("id", jobRerunDetail.getId());
                        detail.put("executeId", jobRerunDetail.getJobExecuteLog().getId());
                        detail.put("jobId", jobRerunDetail.getJobId());
                        detail.put("name", jobRerunDetail.getJobExecuteLog().getJobName());
                        detail.put("state", jobRerunDetail.getJobExecuteLog().getJobState());
                        detail.put("runTime", jobRerunConfig.getRunTime());
                        detail.put("createTime", jobRerunDetail.getJobExecuteLog().getCreateTime());
                        detail.put("excuteTime", jobRerunDetail.getJobExecuteLog().getExecuteTime());
                        detail.put("updateTime", jobRerunDetail.getJobExecuteLog().getUpdateTime());
                        details.add(detail);
                    }
                    resultMap.put("taskId", taskId);
                    resultMap.put("jobList", details);
                    resultMap.put("beginJobs", jobRerunConfig.getBeginJobs());
                    resultMap.put("status", 1);
                }
            }
        }
        return resultMap;
    }

    /**
     * 删除taskId重跑任务下，除ids以外的所有job
     *
     * @param ids
     * @param taskId
     * @return
     */
    @Override
    public Map<String, Object> keepJobAll(String ids, Integer taskId) {
        Map<String, Object> resultMap = new HashMap<>();
        String status = "success";
        String msg = "";
        // 用于存储所有需要删除的任务
        List<Integer> removeList = new ArrayList<>();
        // 1. 获取该task下的所有任务信息
        List<JobRerunDetail> detailList = jobRerunDetailRepository.findAllByTaskId(taskId);

        List<String> list = new ArrayList<>();
        // 2. 过滤掉需要保持当前状态的任务
        if (ids != null && ids.trim().length() > 0) {
            String[] arr = ids.split(",");
            list = Arrays.asList(arr);
        }
        for (JobRerunDetail jobRerunDetail : detailList) {
            String id = String.valueOf(jobRerunDetail.getId());
            if (!list.contains(id)) {
                removeList.add(jobRerunDetail.getId());
            }
        }
        if (removeList.size() == 0) {
            status = "failure";
            msg = "没有需要删除的任务";
        } else {
            // 3. 更改job的状态，将job逻辑删除
            Integer code = jobRerunDetailRepository.changeOperationTypeByIdIn(-1, removeList);
            if (code == 0) {
                status = "failure";
                msg = "删除失败";
            }
        }
        resultMap.put("status", status);
        resultMap.put("msg", msg);
        return resultMap;
    }

    /**
     * kill所有job
     *
     * @param executeIds
     * @param jobIds
     * @return
     */
    @Override
    public Map<String, Object> cancelAllJobs(String executeIds, String jobIds) throws IOException {
        Map<String, Object> resultMap = new HashMap<>();
        // boolean status = true;
        // String statusText = "";
        // 1. kill 所有任务
        List<String> executeIdStrList = Arrays.asList(executeIds.split(","));
        List<Integer> executeIdList = new ArrayList<>();
        for (String s : executeIdStrList) {
            executeIdList.add(Integer.valueOf(s));
        }
        // boolean killFlag = ShellUtils.killProcess(pidRoot, executeIdList);
        boolean killFlag = false;
        if (!killFlag) {
            resultMap.put("status", false);
            resultMap.put("statusText", "查询任务进程ID失败!请重新刷新试试，并联系管理员");
            return resultMap;
        }

        // 2. 更改t_job_schedule的任务状态
        List<String> jobIdStrList = Arrays.asList(jobIds.split(","));
        List<Integer> jobIdList = new ArrayList<>();
        for (String s : jobIdStrList) {
            jobIdList.add(Integer.valueOf(s));
        }
        if (jobIdList.size() > 0) {
            Integer code = jobScheduleRepository.killAllJob(jobIdList);
            if (code == 0) {
                resultMap.put("status", false);
                resultMap.put("statusText", "Job状态更新失败");
                return resultMap;
            }
        }
        // 3. 更改t_job_execute_log的任务状态
        if (executeIdList.size() > 0) {
            Integer code = jobExecuteLogRepository.updateStateByIdIn(5, executeIdList);
            if (code == 0) {
                resultMap.put("status", false);
                resultMap.put("statusText", "kill job失败");
                return resultMap;
            }
        }
        resultMap.put("status", true);
        resultMap.put("statusText", "");
        return resultMap;
    }

    /**
     * 提交该重跑task下的所有job
     *
     * @param taskId
     * @param user
     * @return
     */
    @Override
    public Map<String, Object> submitTask(Integer taskId, User user) {
        Map<String, Object> resultMap = new HashMap<>();
        // 1. 查询重跑任务信息，获取其中的执行时间
        JobRerunConfig jobRerunConfig = jobRerunConfigRepository.findOne(taskId);
        // 2. 查询该taskId的所有任务信息，并插入到执行记录表
        List<JobRerunDetail> detailList = jobRerunDetailRepository.findJobScheduleByTaskId(taskId);
        List<JobExecuteLog> jobExecuteLogList = new ArrayList<>();
        for (JobRerunDetail jobRerunDetail : detailList) {
            if (jobRerunDetail.getOperationType() == 0) {
                JobSchedule jobSchedule = jobRerunDetail.getJobSchedule();
                if (jobSchedule != null) {
                    jobExecuteLogList.add(
                            JobExecuteLogService.castScheduleToExecuteLog(
                                    jobSchedule, taskId, jobRerunConfig.getRunTime(), user));
                }
            }
        }
        if (jobExecuteLogList.size() == 0) {
            resultMap.put("status", "failure");
            resultMap.put("msg", "该Task下无Job，请重新操作");
            return resultMap;
        }
        // 插入执行记录
        jobExecuteLogList = jobExecuteLogRepository.save(jobExecuteLogList);

        // 3. 修改task提交状态
        Integer code = jobRerunConfigRepository.updateStatusById(1, taskId);
        if (code == 0) {
            resultMap.put("status", "failure");
            resultMap.put("msg", "任务状态修改失败，请重新操作");
            return resultMap;
        }

        // 4. 修改jobSchedule运行状态
        List<Integer> jobIdList = new ArrayList<>();
        for (JobRerunDetail detail : detailList) {
            jobIdList.add(detail.getJobId());
        }
        jobScheduleRepository.updateJobStateByJobIdIn(1, jobIdList);
        resultMap.put("status", "success");
        return resultMap;
    }
}
