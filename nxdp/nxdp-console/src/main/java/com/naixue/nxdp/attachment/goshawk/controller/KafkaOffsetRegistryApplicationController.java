package com.naixue.nxdp.attachment.goshawk.controller;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.annotation.Admin;
import com.naixue.nxdp.attachment.goshawk.service.KafkaOffsetRegistryApplicationService;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.service.UserService;
import org.mortbay.util.ajax.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.naixue.nxdp.attachment.goshawk.model.KafkaOffsetRegistryApplication;
import com.naixue.nxdp.attachment.goshawk.model.KafkaOffsetRegistryApplication.KafkaCluster;
import com.naixue.nxdp.web.BaseController;

@Controller
@RequestMapping("/goshawk/workorder/kafka-offset/registry")
public class KafkaOffsetRegistryApplicationController extends BaseController {

    @Autowired
    private KafkaOffsetRegistryApplicationService kafkaOffsetRegistryApplicationService;

    @Autowired
    private UserService userService;

    @ResponseBody
    @RequestMapping("/kafka-clusters.do")
    public Object getKafkaClusters() {
        return success(JSON.parse(KafkaCluster.json()));
    }

    @ResponseBody
    @RequestMapping("/register.do")
    public Object register(HttpServletRequest request, KafkaOffsetRegistryApplication application) {
        User currentUser = getCurrentUser(request);
        KafkaOffsetRegistryApplication registryApplication =
                kafkaOffsetRegistryApplicationService.applykafkaOffsetRegistryApplication(
                        application, currentUser);
        userService.notifyAdmins("[" + currentUser.getName() + "]提交了登记Kafka偏移量的工单，请尽快前往审批。");
        return success(registryApplication);
    }

    @ResponseBody
    @RequestMapping("/registered-applications.do")
    public Object getRegisteredApplications(HttpServletRequest request, DataTableRequest dataTable) {
        User currentUser = getCurrentUser(request);
        KafkaOffsetRegistryApplication queryCondition = new KafkaOffsetRegistryApplication();
        queryCondition.setApplicant(currentUser.getPyName());
        Page<KafkaOffsetRegistryApplication> page =
                kafkaOffsetRegistryApplicationService.getKafkaOffsetRegistryApplications(
                        queryCondition, dataTable.getStart() / dataTable.getLength(), dataTable.getLength());
        return new DataTableResponse<KafkaOffsetRegistryApplication>(
                dataTable.getStart(),
                dataTable.getLength(),
                dataTable.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    @Admin
    @ResponseBody
    @RequestMapping("/list.do")
    public Object getKafkaOffsetRegistryApplications(DataTableRequest dataTable) {
        Page<KafkaOffsetRegistryApplication> page =
                kafkaOffsetRegistryApplicationService.getKafkaOffsetRegistryApplications(
                        null, dataTable.getStart() / dataTable.getLength(), dataTable.getLength());
        return new DataTableResponse<KafkaOffsetRegistryApplication>(
                dataTable.getStart(),
                dataTable.getLength(),
                dataTable.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    @Admin
    @ResponseBody
    @RequestMapping("/assess.do")
    public Object assess(HttpServletRequest request, KafkaOffsetRegistryApplication application) {
        User currentUser = getCurrentUser(request);
        kafkaOffsetRegistryApplicationService.assess(application, currentUser);
        return success();
    }

    @Admin
    @ResponseBody
    @RequestMapping("/reedit.do")
    public Object reedit(HttpServletRequest request, KafkaOffsetRegistryApplication application) {
        User currentUser = getCurrentUser(request);
        kafkaOffsetRegistryApplicationService.reedit(application, currentUser);
        return success();
    }

    @Admin
    @ResponseBody
    @RequestMapping("/delete.do")
    public Object reedit(HttpServletRequest request, Integer id) {
        User currentUser = getCurrentUser(request);
        kafkaOffsetRegistryApplicationService.delete(id, currentUser);
        return success();
    }
}
