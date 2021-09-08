package com.naixue.nxdp.attachment.goshawk.controller;

import com.naixue.nxdp.annotation.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.naixue.nxdp.attachment.goshawk.model.ClusterFlumeTask;
import com.naixue.nxdp.attachment.goshawk.service.ClusterFlumeTaskService;
import com.naixue.nxdp.web.BaseController;

@Controller
@RequestMapping("/goshawk/flume-task")
public class ClusterFlumeTaskController extends BaseController {

    @Autowired
    private ClusterFlumeTaskService clusterFlumeTaskService;

    @Admin
    @RequestMapping("/")
    public Object index() {
        return "/attachment/goshawk/flume-task";
    }

    @Admin
    @ResponseBody
    @RequestMapping("/clusters.do")
    public Object clusters() {
        return success(JSON.parse(ClusterFlumeTask.Cluster.json()));
    }

    @Admin
    @ResponseBody
    @RequestMapping("/find.do")
    public Object save(Integer id) {
        ClusterFlumeTask object = clusterFlumeTaskService.findOne(id);
        return success(object);
    }

    @Admin
    @ResponseBody
    @RequestMapping("/save.do")
    public Object save(ClusterFlumeTask clusterFlumeTask) {
        ClusterFlumeTask object = clusterFlumeTaskService.saveOrUpdate(clusterFlumeTask);
        return success(object);
    }

    @Admin
    @ResponseBody
    @RequestMapping("/list.do")
    public Object list(DataTableRequest dataTable) {
        Page<ClusterFlumeTask> page =
                clusterFlumeTaskService.list(
                        dataTable.getStart() / dataTable.getLength(), dataTable.getLength());
        return new DataTableResponse<ClusterFlumeTask>(
                dataTable.getStart(),
                dataTable.getLength(),
                dataTable.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    @Admin
    @ResponseBody
    @RequestMapping("/delete.do")
    public Object delete(Integer id) {
        clusterFlumeTaskService.delete(id);
        return success();
    }
}
