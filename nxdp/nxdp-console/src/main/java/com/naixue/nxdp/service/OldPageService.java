package com.naixue.nxdp.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.naixue.nxdp.model.JobExecuteLog;
import com.naixue.nxdp.model.User;

/**
 * @author: wangyu @Created by 2018/2/6
 */
public interface OldPageService {

    /**
     * 改变job的状态，同时改变t_job_config表和t_job_schedule表
     *
     * @param jobId
     * @param status 目标状态
     * @return
     */
    @Transactional
    void changeJobStatus(Integer jobId, Integer status) throws RuntimeException;

    /**
     * 手动执行任务，更改t_job_schedule状态及插入t_job_execute_log记录
     *
     * @param jobId
     * @param chooseRunTime
     * @param user
     * @return
     */
    @Transactional
    String runJobManual(Integer jobId, Date chooseRunTime, User user) throws RuntimeException;

    JobExecuteLog triggerJob(final User currentUser, final Integer jobId, final Date chooseRunTime)
            throws RuntimeException;

    /**
     * 按指定一段时间运行任务[时间范围前闭后开]
     *
     * @param currentUser
     * @param jobId
     * @param startDate
     * @param endDate
     * @throws RuntimeException
     */
    public void runJobWithTimeSpan(
            final User currentUser, final Integer jobId, final Date startDate, final Date endDate)
            throws RuntimeException;

    /**
     * 手动kill任务，更改t_job_schedule状态及t_job_execute_log状态更新
     *
     * @param jobId
     * @param executeId 执行记录ID
     * @return
     */
    @Transactional
    void killRunningJob(Integer jobId, Integer executeId) throws RuntimeException, Exception;

    /**
     * 获取当前job的所有后置任务
     *
     * @param jobId
     * @return
     */
    List<Map<String, Object>> getJobAllChildren(Integer jobId);
}
