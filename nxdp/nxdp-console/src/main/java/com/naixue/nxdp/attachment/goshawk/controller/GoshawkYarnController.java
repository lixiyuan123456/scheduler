package com.naixue.nxdp.attachment.goshawk.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Splitter;
import com.naixue.nxdp.attachment.goshawk.dao.GoshawkThresHoldRepository;
import com.naixue.nxdp.attachment.goshawk.dao.GoshawkYarnJobMapReduceRepository;
import com.naixue.nxdp.attachment.goshawk.dao.GoshawkYarnJobSparkRepository;
import com.naixue.nxdp.attachment.goshawk.dao.YarnWhiltelistRepository;
import com.naixue.nxdp.attachment.goshawk.model.QueryCondition;
import com.naixue.nxdp.attachment.goshawk.model.Threshold;
import com.naixue.nxdp.attachment.goshawk.model.Whitelist;
import com.naixue.nxdp.attachment.goshawk.model.YarnJobMapReduce;
import com.naixue.nxdp.attachment.goshawk.model.YarnJobSpark;
import com.naixue.nxdp.attachment.goshawk.model.YarnQueueResource;
import com.naixue.nxdp.attachment.goshawk.model.YarnReportCondition;
import com.naixue.nxdp.attachment.goshawk.model.YarnQueueResource.Queue;
import com.naixue.nxdp.attachment.goshawk.service.IYarnJobQueryService;
import com.naixue.nxdp.attachment.goshawk.service.IYarnReportService;
import com.naixue.nxdp.attachment.goshawk.service.IYarnWhitelistService;
import com.naixue.nxdp.attachment.util.XmlUtils;
import com.naixue.zzdp.common.util.ShellUtils;
import com.naixue.nxdp.web.BaseController;

import lombok.extern.slf4j.Slf4j;

/**
 * 苍鹰-yarn管理
 *
 * @author 刘蒙
 */
@Controller
@Slf4j
@RequestMapping("/goshawk/yarn")
public class GoshawkYarnController extends BaseController {

    @Autowired
    private GoshawkThresHoldRepository thresHoldRepository;

    @Autowired
    private GoshawkYarnJobSparkRepository sparkRepository;

    @Autowired
    private GoshawkYarnJobMapReduceRepository mrRepository;

    @Autowired
    private IYarnJobQueryService yarnJobQueryService;

    @Autowired
    private UserService userService;

    @Autowired
    private IYarnWhitelistService whitelistService;

    @Autowired
    private YarnWhiltelistRepository whiltelistRepository;

    @Autowired
    private IYarnReportService reportService;

    /**
     * 跳转阈值列表界面
     */
    @RequestMapping("/threshold")
    public ModelAndView threshold() {
        return new ModelAndView("attachment/goshawk/yarn/threshold");
    }

    /**
     * 跳转阈值列表界面
     */
    @RequestMapping("/yarn-job")
    public ModelAndView yarnJob() {
        return new ModelAndView("attachment/goshawk/yarn/yarn-job");
    }

    /**
     * 跳转阈值列表界面
     */
    @RequestMapping("/threshold/list.do")
    @ResponseBody
    public Object listThreshold() {
        Map<String, Object> map = new HashMap<>();

        try {
            List<Threshold> all = thresHoldRepository.findAll();
            map.put("status", "ok");
            map.put("data", all);
        } catch (Exception e) {
            log.error("查询threshold列表出错", e);
            map.put("error", e.getMessage());
            map.put("status", "failed");
        }

        return map;
    }

    /**
     * 跳转阈值列表界面
     */
    @RequestMapping("/threshold/delete.do")
    @ResponseBody
    public Object deleteThreshold(String thresholdId) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNumeric(thresholdId)) {
            int id = Integer.parseInt(thresholdId);
            try {
                thresHoldRepository.delete(id);
                map.put("status", "ok");
            } catch (Exception e) {
                log.error("删除threshold出错", e);
                map.put("error", e.getMessage());
                map.put("status", "failed");
            }
        } else {
            map.put("error", "thresholdId无效: " + thresholdId);
            map.put("status", "failed");
        }

        return map;
    }

    /**
     * 跳转阈值列表界面
     */
    @RequestMapping("/threshold/saveOrUpdate.do")
    @ResponseBody
    public Object saveOrUpdateThreshold(@RequestBody Threshold t) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", "failed");
        try {
            if (t.getId() == null) {
                log.debug("新增记录: {}", t);
                Threshold saved = thresHoldRepository.save(t);
                if (saved != null) {
                    map.put("status", "ok");
                }
            } else {
                log.debug("更新记录: {}", t);
                Integer updated =
                        thresHoldRepository.update(t.getKey(), t.getName(), t.getValue(), t.getId());
                if (updated == 1) {
                    map.put("status", "ok");
                } else {
                    map.put("error", "没有找到对应的记录: " + t);
                }
            }
        } catch (Exception e) {
            log.error("添加或更新threshold出错: {}", t, e);
            map.put("error", e.getMessage());
        }

        return map;
    }

    /**
     * 跳转阈值列表界面
     */
    @RequestMapping("/yarn-job/get-queue.do")
    @ResponseBody
    public Object getQueue(String appType) {
        Map<String, Object> map = new HashMap<>();
        try {
            List<String> queue = new ArrayList<>();
            if ("Mapreduce".equalsIgnoreCase(appType)) {
                queue.addAll(mrRepository.queue());
            } else {
                queue.addAll(sparkRepository.queue());
            }
            queue.remove("null");
            queue.remove(null);

            map.put("status", "ok");
            map.put("data", queue);
        } catch (Exception e) {
            log.error("查询{}作业队列出错", appType, e);
            map.put("error", e.getMessage());
            map.put("status", "failed");
        }

        return map;
    }

    @RequestMapping("/yarn-job/list.do")
    @ResponseBody
    public Object listMetadataHiveTables(DataTableRequest dataTable) {
        QueryCondition q = JSON.parseObject(dataTable.getCondition(), QueryCondition.class);
        if ("mapreduce".equalsIgnoreCase(q.getAppType())) {
            Page<YarnJobMapReduce> page =
                    yarnJobQueryService.listMapReduce(
                            q, dataTable.getStart() / dataTable.getLength(), dataTable.getLength());
            return new DataTableResponse<>(
                    dataTable.getStart(),
                    dataTable.getLength(),
                    dataTable.getDraw(),
                    page.getTotalElements(),
                    page.getTotalElements(),
                    page.getContent());
        } else {
            Page<YarnJobSpark> page =
                    yarnJobQueryService.listSpark(
                            q, dataTable.getStart() / dataTable.getLength(), dataTable.getLength());
            return new DataTableResponse<>(
                    dataTable.getStart(),
                    dataTable.getLength(),
                    dataTable.getDraw(),
                    page.getTotalElements(),
                    page.getTotalElements(),
                    page.getContent());
        }
    }

    @RequestMapping("/queue-resource/cfg")
    public String yarnQueueResourceCfgBuilder() {
        return "attachment/goshawk/yarn/queue-resource-cfg";
    }

    @ResponseBody
    @RequestMapping("/queue-resource/cfg/root-tree.do")
    public Object buildRootTree4YarnQueueResourceCfg() {
        Queue rootQueue = YarnQueueResource.buildRootQueue();
        return success("tree", Arrays.asList(rootQueue));
    }

    @ResponseBody
    @RequestMapping("/queue-resource/cfg/view-xml.do")
    public Object viewXml4YarnQueueResourceCfg(@RequestBody Queue rootQueue) {
        YarnQueueResource.enhanceQueue(rootQueue, rootQueue);
        return success("xml", XmlUtils.toXml(new YarnQueueResource.Allocation(rootQueue)));
    }

    @ResponseBody
    @RequestMapping("/queue-resource/cfg/distribute.do")
    public Object distributeXml2Servers(String xml, String servers, String path) {
        Assert.hasText(xml, "xml字符串为空");
        Assert.hasText(servers, "分发目标服务器ip为空");
        Assert.hasText(path, "分发路径为空");
        createAndMoveXmlFile(xml, servers, path);
        return success();
    }

    @ResponseBody
    @RequestMapping("/queue-resource/cfg/cluster-metrics.do")
    public Object viewXml4YarnQueueResourceCfg(YarnQueueResource.ClusterMetrics clusterMetrics) {

        return success("data", clusterMetrics);
    }

    @RequestMapping("/whitelist")
    public Object list(HttpServletRequest request, Model model) {
        model.addAttribute("users", userService.listUsers());
        model.addAttribute("currentUser", getCurrentUser(request));
        return "attachment/goshawk/yarn/whitelist";
    }

    @RequestMapping("/whitelist/list.do")
    @ResponseBody
    public Object listWhitelist(DataTableRequest dataTable) {
        Page<Whitelist> page =
                whitelistService.list(
                        JSON.parseObject(dataTable.getCondition(), Whitelist.class),
                        dataTable.getStart() / dataTable.getLength(),
                        dataTable.getLength());
        return new DataTableResponse<>(
                dataTable.getStart(),
                dataTable.getLength(),
                dataTable.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    @RequestMapping("/whitelist/add.do")
    @ResponseBody
    public Object addWhitelist(@RequestBody Whitelist whitelist) {
        whiltelistRepository.save(whitelist);
        return success("ok", "msg", whitelist.getAppName());
    }

    @RequestMapping("/whitelist/delete.do")
    @ResponseBody
    public Object delete(Integer id) {
        if (id == null) {
            return success("error", "msg", "id为空");
        }

        Whitelist one = whiltelistRepository.getOne(id);
        String appName = one.getAppName();

        whiltelistRepository.delete(id);
        return success("ok", "msg", appName);
    }

    /**
     * 跳转报表列表界面
     */
    @RequestMapping("/report")
    public ModelAndView report() {
        return new ModelAndView("attachment/goshawk/yarn/report");
    }

    @RequestMapping("/report/list.do")
    @ResponseBody
    public Object listReport(DataTableRequest req) {
        YarnReportCondition condition = JSON.parseObject(req.getCondition(), YarnReportCondition.class);
        String reportType = condition.getReportType();

        Page<?> page;
        if ("1".equals(reportType)) {
            page =
                    reportService.listOverOneHour(
                            condition, req.getStart() / req.getLength(), req.getLength());
        } else if ("2".equals(reportType)) {
            page =
                    reportService.listMapReduce(condition, req.getStart() / req.getLength(), req.getLength());
        } else {
            page = reportService.listSpark(condition, req.getStart() / req.getLength(), req.getLength());
        }
        return new DataTableResponse<>(
                req.getStart(),
                req.getLength(),
                req.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    private void createAndMoveXmlFile(String xml, String servers, String path) {
        File tempFile = null;
        try {
            if (!StringUtils.endsWith(path, "/")) {
                path += File.separator;
            }
            tempFile = File.createTempFile("cfg", ".xml");
            FileCopyUtils.copy(xml.getBytes(StandardCharsets.UTF_8), tempFile);
            String tempFilePath = tempFile.getAbsolutePath();
            log.info("生成的临时XML文件绝对路径=" + tempFilePath);
            Iterator<String> iterator = Splitter.on(";").split(servers).iterator();
            while (iterator.hasNext()) {
                String ip = iterator.next();
                // ShellUtils.exec("scp", tempFilePath, "work@" + ip + ":" + path);
                ShellUtils.exec(" scp " + tempFilePath + " work@" + ip + ":" + path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            tempFile.deleteOnExit();
        }
    }
}
