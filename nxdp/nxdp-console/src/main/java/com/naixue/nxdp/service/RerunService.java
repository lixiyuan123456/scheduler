package com.naixue.nxdp.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;

import com.naixue.nxdp.model.User;

/**
 * @author: wangyu @Created by 2018/2/6
 */
public interface RerunService {

    /**
     * 提交批量重跑job
     *
     * @param jobIds      需重跑的job列表
     * @param runTime     重跑时间
     * @param beginJobIds 开始任务
     * @param user        用户信息
     * @return
     */
    Map<String, Object> submitRerunJobs(
            String jobIds, Timestamp runTime, String beginJobIds, User user);

    /**
     * 获取重跑job明细
     *
     * @param taskId 重跑任务ID
     * @return
     */
    Map<String, Object> jobRerunDetail(Integer taskId);

    /**
     * 删除taskId重跑任务下，除ids以外的所有job
     *
     * @param ids
     * @param taskId
     * @return
     */
    Map<String, Object> keepJobAll(String ids, Integer taskId);

    /**
     * kill所有job
     *
     * @param executeIds
     * @param jobIds
     * @return
     */
    Map<String, Object> cancelAllJobs(String executeIds, String jobIds) throws IOException;

    /**
     * 提交该重跑task下的所有job
     *
     * @param taskId
     * @param user
     * @return
     */
    Map<String, Object> submitTask(Integer taskId, User user);
}
