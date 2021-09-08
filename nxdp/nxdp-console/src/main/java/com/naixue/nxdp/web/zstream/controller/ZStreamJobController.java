package com.naixue.nxdp.web.zstream.controller;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.config.CFG;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.thirdparty.WXHelper;
import com.naixue.nxdp.util.YarnUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.naixue.zzdp.data.model.zstream.ZstreamJob;
import com.naixue.zzdp.data.model.zstream.ZstreamJobLog;
import com.naixue.zzdp.platform.service.zstream.ZstreamJobLogService;
import com.naixue.zzdp.platform.service.zstream.ZstreamJobService;
import com.naixue.nxdp.web.BaseController;

@RestController
@RequestMapping("/zstream/job")
public class ZStreamJobController extends BaseController {

    @Autowired
    private ZstreamJobService zstreamJobService;

    @Autowired
    private ZstreamJobLogService zstreamJobLogService;

    @RequestMapping("/list.do")
    public Object list(HttpServletRequest request, DataTableRequest dataTable) {
        User currentUser = getCurrentUser(request);
        ZstreamJob condition = JSON.parseObject(dataTable.getCondition(), ZstreamJob.class);
        if (StringUtils.isEmpty(condition.getCreator())) {
            condition.setCreator(currentUser.getPyName());
        }
        Page<ZstreamJob> page =
                zstreamJobService.listJobs(
                        condition, dataTable.getStart() / dataTable.getLength(), dataTable.getLength());
        if (!CollectionUtils.isEmpty(page.getContent())) {
            for (ZstreamJob job : page.getContent()) {
                if (!StringUtils.isEmpty(job.getLastApplicationId())) {
                    String dashboardUrl = MessageFormat.format(CFG.ZSTREAM_JOB, job.getLastApplicationId());
                    job.setDashboardUrl(dashboardUrl);
                    String yarnLogUrl =
                            MessageFormat.format(
                                    CFG.ZSTREAM_JOB_LOG, job.getLastApplicationId().replace("application_", ""));
                    job.setYarnLogUrl(yarnLogUrl);
                }
            }
        }
        DataTableResponse<ZstreamJob> datatable =
                new DataTableResponse<ZstreamJob>(
                        dataTable.getStart(),
                        dataTable.getLength(),
                        dataTable.getDraw(),
                        page.getTotalElements(),
                        page.getTotalElements(),
                        page.getContent());
        return datatable;
    }

    @RequestMapping("/save.do")
    public Object save(
            HttpServletRequest request,
            ZstreamJob job,
            @RequestParam(value = "udxIds[]", required = false) Integer[] udxIds) {
        User currentUser = getCurrentUser(request);
        // job.setCreator(currentUser.getPyName());
        job.setProxyCode(currentUser.getHadoopBinding().getProxyCode());
        ZstreamJob newJob = null;
        if (udxIds == null) {
            newJob = zstreamJobService.save(job);
        } else {
            newJob = zstreamJobService.save(job, Arrays.asList(udxIds));
        }
        return success(newJob);
    }

    @RequestMapping("/find.do")
    public Object findJobById(Integer jobId) {
        ZstreamJob job = zstreamJobService.findJobById(jobId, false);
        return success(job);
    }

    @RequestMapping("/delete.do")
    public Object deleteJobById(HttpServletRequest request, Integer id) {
        User currentUser = getCurrentUser(request);
        Assert.notNull(currentUser.getHadoopBinding(), "用户代号不存在，当前用户是" + currentUser.getPyName());
        ZstreamJob job = zstreamJobService.findJobById(id);
        Assert.notNull(job, "任务不存在，任务id是" + id);
        if (!job.getProxyCode().equals(currentUser.getHadoopBinding().getProxyCode())) {
            throw new RuntimeException("无权操作，不允许跨部门操作别人的任务");
        }
        zstreamJobService.deleteJobById(id);
        String wxMsg =
                MessageFormat.format(
                        "zstream job[id={0},name={1}] is deleted by {2} - {3}.",
                        job.getId(), job.getName(), currentUser.getName(), currentUser.getPyName());
        WXHelper.asyncSendWXMsg(wxMsg, job.getReceivers());
        return success();
    }

    @RequestMapping("/kill.do")
    public Object killJobById(HttpServletRequest request, Integer id) {
        User currentUser = getCurrentUser(request);
        Assert.notNull(currentUser.getHadoopBinding(), "用户代号不存在，当前用户是" + currentUser.getPyName());
        ZstreamJob job = zstreamJobService.findJobById(id);
        Assert.notNull(job, "任务不存在，任务id是" + id);
        if (!job.getProxyCode().equals(currentUser.getHadoopBinding().getProxyCode())) {
            throw new RuntimeException("无权操作，不允许跨部门操作别人的任务");
        }
        YarnUtils.State state =
                YarnUtils.queryApplicationStateByApplicationId(CFG.YARN, job.getLastApplicationId());
        if (state == Enum.valueOf(YarnUtils.State.class, "FAILED")) { // 运行失败
            throw new RuntimeException("zstream job is failed already.");
        } else if (state == Enum.valueOf(YarnUtils.State.class, "KILLED")) { // KILLED
            throw new RuntimeException("zstream job is killed already.");
        }
        zstreamJobService.killJob(job);
        String wxMsg =
                MessageFormat.format(
                        "zstream job[id={0},name={1}] is killed by {2} - {3}.",
                        job.getId(), job.getName(), currentUser.getName(), currentUser.getPyName());
        WXHelper.asyncSendWXMsg(wxMsg, job.getReceivers());
        return success();
    }

    @RequestMapping("/launch.do")
    public Object launchJob(HttpServletRequest request, Integer id, boolean resume) {
        User currentUser = getCurrentUser(request);
        Assert.notNull(currentUser.getHadoopBinding(), "用户代号不存在，当前用户是" + currentUser.getPyName());
        ZstreamJob job = zstreamJobService.findJobById(id);
        Assert.notNull(job, "任务不存在，任务id是" + id);
        if (!job.getProxyCode().equals(currentUser.getHadoopBinding().getProxyCode())) {
            throw new RuntimeException("无权操作，不允许跨部门操作别人的任务");
        }
        zstreamJobService.launchJob(job, resume);
        String wxMsg =
                MessageFormat.format(
                        "zstream job[id={0},name={1}] is started by {2} - {3}.",
                        job.getId(), job.getName(), currentUser.getName(), currentUser.getPyName());
        WXHelper.asyncSendWXMsg(wxMsg, job.getReceivers());
        return success();
    }

    @RequestMapping("/log.do")
    public Object viewJobRunLog(@RequestParam Integer jobId, @RequestParam String jobVersion) {
        ZstreamJobLog jobLog = zstreamJobLogService.readLog(jobId, jobVersion);
        return success(jobLog);
    }

    @Deprecated
    // @RequestMapping("/check_sql.do")
    public Object checkJobSqlGrammarByJobId(@RequestParam Integer jobId) {
        Object result = zstreamJobService.checkJobSqlGrammarByJobId(jobId);
        return success(result);
    }

    @RequestMapping("/check_sql.do")
    public Object checkJobSqlGrammar(@RequestParam String sql, String udxs) {
        Object result = zstreamJobService.checkJobSqlGrammar(sql, udxs);
        return success(result);
    }

    @RequestMapping("/list_job_logs.do")
    public Object listJobLogs(@RequestParam Integer jobId) {
        List<ZstreamJobLog> list = zstreamJobLogService.listLogs(jobId);
        if (!CollectionUtils.isEmpty(list)) {
            for (ZstreamJobLog log : list) {
                if (!StringUtils.isEmpty(log.getApplicationId())) {
                    String url = MessageFormat.format(CFG.ZSTREAM_JOB, log.getApplicationId());
                    log.setDashboardUrl(url);
                }
            }
        }
        return success(list);
    }
}
