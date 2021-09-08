package com.naixue.nxdp.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.naixue.nxdp.service.DataExtractService;
import com.naixue.nxdp.service.DevTaskService;
import com.naixue.nxdp.service.WebHDFSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.naixue.nxdp.dao.JobUpdateRecordRepository;
import com.naixue.nxdp.model.HdpCluster;
import com.naixue.nxdp.model.JobConfig;
import com.naixue.nxdp.model.JobUpdateRecord;
import com.naixue.nxdp.model.ServerConfig;
import com.naixue.nxdp.model.User;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: wangyu @Created by 2017/11/13
 */
@Controller
@Slf4j
@RequestMapping("/dev/task")
public class DevTaskController extends BaseController {

    @Autowired
    private DevTaskService devTaskService;

    @Autowired
    private DataExtractService dataExtractService;

    @Autowired
    private JobUpdateRecordRepository jobUpdateRecordRepository;

    @Autowired
    private WebHDFSService webHDFSService;

    /**
     * 编辑页面 - 获取SQL更新记录
     *
     * @param jobId
     * @return
     */
    @RequestMapping("/api/fetch-history")
    @ResponseBody
    public Object fetchHistory(@RequestParam(name = "id") Integer jobId) {
        Map<String, Object> resultMap = new HashMap<>();
        if (jobId != null) {
            // 获取job的更新记录
            List<JobUpdateRecord> logs = devTaskService.getUpdateRecordList(jobId);
            resultMap.put("list", logs);
        }
        return resultMap;
    }

    /**
     * 编辑页面 - 获取文件上传记录
     *
     * @param jobId
     * @return
     */
    @RequestMapping("/api/fetch-file-history")
    @ResponseBody
    public Object fetchFileHistory(@RequestParam(name = "id") Integer jobId) {
        Map<String, Object> resultMap = new HashMap<>();
        if (jobId != null) {
            // 获取job的更新记录
            List<JobUpdateRecord> logs = devTaskService.getUpdateRecordList(jobId);
            resultMap.put("list", logs);
        }
        return resultMap;
    }

    /**
     * 编辑页面 - 获取任务信息
     *
     * @param jobId
     * @return
     */
    @RequestMapping("/api/fetch-task")
    @ResponseBody
    public Object fetchTask(@RequestParam(name = "id") Integer jobId) {
        String status = "ok";
        Map<String, Object> resultMap = new HashMap<>();
        if (jobId == null) {
            status = "error";
        } else {
            // 获取job的基本信息
            JobConfig jobConfig = devTaskService.getJobConfigByJobId(jobId);
            resultMap.put("task", jobConfig);
        }
        resultMap.put("status", status);
        return resultMap;
    }

    @ResponseBody
    @RequestMapping("/api/fetch-task-data-extract")
    public Object fetchTaskDataExtract(@RequestParam(name = "id") Integer jobId) {
        JobConfig jobConfig = devTaskService.getJobConfigByJobId(jobId);
        TaskDTO taskVO = new TaskDTO();
        if (jobConfig != null) {
            taskVO.setId(jobConfig.getId());
            taskVO.setModuleName(jobConfig.getJobName());
            taskVO.setType(jobConfig.getType());
            taskVO.setOwnerId(jobConfig.getUserId());
            taskVO.setDescription(jobConfig.getDescription());
            taskVO.setDetails(jobConfig.getDetails());
        }
        return success("task", taskVO);
    }

    @RequestMapping("/api/list-hdp-clusters")
    @ResponseBody
    public Object listHdpClusters(Integer serverType, Integer localType) {
        Map<String, Object> resultMap = new HashMap<>();
        // 获取服务器列表
        List<HdpCluster> servers = devTaskService.getAllHdpClusters();
        resultMap.put("serverConfig", servers);
        return resultMap;
    }

    @ResponseBody
    @RequestMapping("/api/fetch-server-configs")
    public Object fetchServerConfigs(Integer serverType, Integer localType) throws Exception {
        // ServerConfig.ServerTypeGroup e = ServerConfig.ServerTypeGroup.parseEnum(serverTypeGroupId);
        List<Map<String, Object>> data =
                dataExtractService.fetchServers(
                        ServerConfig.ServerType.parseEnum(serverType),
                        ServerConfig.LogicType.parseEnum(localType));
        return success("serverConfig", data);
    }

    @ResponseBody
    @RequestMapping("/api/fetch-source-db")
    public Object fetchDbs(Integer serverType, Integer serverId) {
        List<Map<String, Object>> data =
                dataExtractService.fetchDbs(ServerConfig.ServerType.parseEnum(serverType), serverId);
        return success("serverDb", data);
    }

    @ResponseBody
    @RequestMapping("/api/fetch-tables")
    public Object fetchDbTables(Integer serverType, Integer serverId, Integer dbId, String dbName) {
        List<Map<String, Object>> data =
                dataExtractService.fetchDbTables(
                        ServerConfig.ServerType.parseEnum(serverType), serverId, dbId, dbName);
        return success("tables", data);
    }

    @ResponseBody
    @RequestMapping("/api/fetch-source-fields")
    public Object fetchDbTableFields(
            Integer serverType,
            @RequestParam(name = "id") Integer serverId,
            @RequestParam(name = "database") String dbName,
            @RequestParam(name = "table") String tableName) {
        Assert.notNull(serverType, "请求参数type不允许为空");
        List<Map<String, Object>> data =
                dataExtractService.fetchDbTableFields(
                        ServerConfig.ServerType.parseEnum(serverType), serverId, dbName, tableName);
        return success("fields", data);
    }

    @ResponseBody
    @RequestMapping("/api/clear-memory-cache-for-data-extract")
    public Object clearMemoryCacheForDataExtract() {
        dataExtractService.clearMemoryCache();
        return success();
    }

    @ResponseBody
    @RequestMapping("/api/fetch-server-type")
    public Object fetchServerTypeGroup(String type) {
        Map<String, Object> data = new HashMap<>();
        for (ServerConfig.ServerTypeGroup e : ServerConfig.ServerTypeGroup.values()) {
            data.put(e.getId() + "", e.getName());
        }
        return success("serverType", data);
    }

    @ResponseBody
    @RequestMapping("/api/fetch-task-history")
    public Object fetchTaskHistory(String columns, String search, Integer id) {
        List<JobUpdateRecord> list =
                jobUpdateRecordRepository.findAllByJobId(id, new Sort(Direction.DESC, "updateTime"));
        if (!CollectionUtils.isEmpty(list)) {
            List<Map<String, Object>> data = new ArrayList<>();
            for (JobUpdateRecord e : list) {
                Map<String, Object> m = new HashMap<>();
                m.put("created", e.getUpdateTime());
                m.put("user", e.getOperatorName());
                data.add(m);
            }
            return success("list", data);
        }
        return success();
    }

    @RequestMapping("/api/pull-scripts")
    @ResponseBody
    public Object pullScripts(String path, String revision) {
        String script = webHDFSService.readScriptStringFromWebHDFS(path, revision);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("revision", revision);
        resultMap.put("status", "ok");
        resultMap.put("scripts", script);
        return resultMap;
    }

    @ResponseBody
    @RequestMapping("/api/save")
    public Object saveDataExtractConfig(HttpServletRequest request, TaskDTO task) {
        JobConfig jobConfig = new JobConfig();
        if (task != null) {
            jobConfig.setId(task.getTask().getId());
            jobConfig.setJobName(task.getTask().getModuleName());
            jobConfig.setType(task.getTask().getType());
            jobConfig.setStatus(JobConfig.JobStatus.NORMAL.getId());
            jobConfig.setUserId(task.getTask().getOwnerId());
            jobConfig.setDescription(task.getTask().getDescription());
            jobConfig.setDetails(task.getTask().getDetails());
        }
        Integer id = null;
        try {
            id = dataExtractService.save(getCurrentUser(request), jobConfig);
        } catch (Exception e) {
            return success("error", "msg", e.getMessage());
        }
        return success("id", id);
    }

    /**
     * 编辑页面 - 保存Mysql或Hive任务
     *
     * @param jobConfig
     * @return
     */
    @RequestMapping("/api/save-script")
    @ResponseBody
    public Object saveScript(HttpServletRequest request, JobConfig jobConfig) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 获取当前用户信息
            User currUser = getCurrentUser(request);
            resultMap = devTaskService.saveSQLJob(jobConfig, currUser);
        } catch (Exception e) {
            log.error(request.getServletPath(), e);
            resultMap.put("msg", e.getMessage());
            resultMap.put("status", "error");
        }
        return resultMap;
    }

    /**
     * 编辑sql脚本，添加编辑锁并获取最新sql
     *
     * @param request
     * @param jobId
     * @param codeEditLock
     * @return
     */
    @RequestMapping("/api/edit-sql")
    @ResponseBody
    public Object editSql(
            HttpServletRequest request, @RequestParam(name = "id") Integer jobId, Integer codeEditLock) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 获取当前用户信息
            User currUser = getCurrentUser(request);
            resultMap = devTaskService.editSql(jobId, codeEditLock, currUser);
        } catch (Exception e) {
            resultMap.put("status", "error");
            resultMap.put("msg", e.getMessage());
            log.error(request.getServletPath(), e);
        }
        return resultMap;
    }

    @ResponseBody
    @RequestMapping("/api/toggle-ide-available-status")
    public Object toggleIdeAvailableStatus(
            HttpServletRequest request, @RequestParam(name = "id") Integer jobId, Integer codeEditLock) {
        User currentUser = getCurrentUser(request);
        return success("data", devTaskService.editShell(jobId, codeEditLock, currentUser));
    }

    /**
     * 保存MR、Spark等需上传文件的任务
     *
     * @param request
     * @param jobConfig
     * @return
     */
    @RequestMapping("/api/save-file-job")
    @ResponseBody
    public Object saveFileJob(HttpServletRequest request, JobConfig jobConfig) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            User currUser = getCurrentUser(request);
            resultMap = devTaskService.saveFileJob(jobConfig, currUser);
        } catch (Exception e) {
            log.error(request.getServletPath(), e);
            resultMap.put("msg", e.getMessage());
            resultMap.put("status", "error");
        }
        return resultMap;
    }

    @ResponseBody
    @RequestMapping("/api/save-shell-ide-job")
    public Object saveShellIdeJob(HttpServletRequest request, JobConfig jobConfig) {
        User currentUser = getCurrentUser(request);
        Integer jobId = devTaskService.saveShellJob(currentUser, jobConfig);
        return success("id", jobId);
    }

    @ResponseBody
    @RequestMapping("/saveScheduledJob.do")
    public Object saveScheduledJob(HttpServletRequest request, JobConfig jobConfig) {
        User currentUser = getCurrentUser(request);
        Integer jobId = devTaskService.saveScheduledJob(currentUser, jobConfig);
        return success("id", jobId);
    }

    /**
     * 上传文件
     *
     * @param jobFile
     * @return
     */
    @RequestMapping("/uploadFile")
    @ResponseBody
    public Object uploadFile(@RequestParam("jobFile") MultipartFile jobFile) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            resultMap = devTaskService.uploadFiles(jobFile);
        } catch (Exception e) {
            log.error("/uploadFile", e);
            resultMap.put("errorMsg", e.getMessage());
            resultMap.put("result", "false");
        }
        return resultMap;
    }

    /**
     * 下载文件
     *
     * @param response
     * @param fileName
     * @param realFileName
     * @return
     */
    @RequestMapping("/downloadFile")
    @ResponseBody
    public String downloadFile(
            HttpServletResponse response,
            @RequestParam(name = "fileName") String fileName,
            @RequestParam(name = "realFileName") String realFileName) {
        webHDFSService.downloadFileFromWebHDFS(response, fileName, realFileName);
        return JSON.toJSONString(true);
    }

    @RequestMapping("/scheduler/about.jsp")
    public String showCronAbout() {
        return "cron-about";
    }

    @Data
    public static class TaskDTO {

        private TaskDTO task;

        private Integer id;

        private String moduleName;

        private Integer type;

        private String ownerId;

        private String description;

        private String details;
    }
}
