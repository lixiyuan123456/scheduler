package com.naixue.nxdp.attachment.goshawk.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.annotation.Admin;
import com.naixue.nxdp.model.HadoopBinding;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.service.HadoopBindingService;
import com.naixue.nxdp.service.UserService;
import com.naixue.nxdp.service.hivemetadata.HiveMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.naixue.nxdp.attachment.goshawk.model.HiveTableAccessApplication;
import com.naixue.nxdp.attachment.goshawk.model.KafkaTopicApplication;
import com.naixue.nxdp.attachment.goshawk.service.HiveTableAccessApplicationService;
import com.naixue.nxdp.attachment.goshawk.service.KafkaTopicApplicationService;
import com.naixue.nxdp.web.BaseController;

@Controller
@RequestMapping("/goshawk/workorder")
public class GoshawkWorkOrderController extends BaseController {

    @Autowired
    private KafkaTopicApplicationService kafkaTopicApplicationService;

    @Autowired
    private HiveTableAccessApplicationService hiveTableAccessApplicationService;

    @Autowired
    private HadoopBindingService hadoopBindingService;

    @Autowired
    private HiveMetadataService hiveMetadataService;

    @Autowired
    private UserService userService;

    @RequestMapping("/application")
    public String application(Model model, HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        HadoopBinding hadoopBinding = hadoopBindingService.findByDeptId(currentUser.getDeptId());
        model.addAttribute("hadoopBinding", hadoopBinding);
        return "attachment/goshawk/workorder/application";
    }

    @Admin
    @RequestMapping("/assess")
    public String assess() {
        return "attachment/goshawk/workorder/assess";
    }

    @ResponseBody
    @RequestMapping("/apply-kafka-topic.do")
    public Object applyKafkaTopic(HttpServletRequest request, KafkaTopicApplication application) {
        User currentUser = getCurrentUser(request);
        KafkaTopicApplication object =
                kafkaTopicApplicationService.applyKafkaTopic(application, currentUser);
        userService.notifyAdmins("[" + currentUser.getName() + "]提交了申请Kafka主题的工单，请尽快前往审批。");
        return success(object);
    }

    @ResponseBody
    @RequestMapping("/apply-hive-table-access-application.do")
    public Object applyHiveTableAccessApplication(
            HttpServletRequest request, HiveTableAccessApplication application) {
        User currentUser = getCurrentUser(request);
        HiveTableAccessApplication object =
                hiveTableAccessApplicationService.applyHiveTableAccessApplication(application, currentUser);
        userService.notifyAdmins("[" + currentUser.getName() + "]提交了申请Hive表权限的工单，请尽快前往审批。");
        return success(object);
    }

    @ResponseBody
    @RequestMapping("/kafka-topic-application/own-applications.do")
    public Object listKafkaTopicApplications(HttpServletRequest request, DataTableRequest dataTable) {
        User currentUser = getCurrentUser(request);
        KafkaTopicApplication queryCondition = new KafkaTopicApplication();
        queryCondition.setCreator(currentUser.getPyName());
        Page<KafkaTopicApplication> page =
                kafkaTopicApplicationService.listKafkaTopicApplications(
                        queryCondition, dataTable.getStart() / dataTable.getLength(), dataTable.getLength());
        return new DataTableResponse<KafkaTopicApplication>(
                dataTable.getStart(),
                dataTable.getLength(),
                dataTable.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    @Admin
    @ResponseBody
    @RequestMapping("/kafka-topic-application/list.do")
    public Object listKafkaTopicApplications(DataTableRequest dataTable) {
        Page<KafkaTopicApplication> page =
                kafkaTopicApplicationService.listKafkaTopicApplications(
                        null, dataTable.getStart() / dataTable.getLength(), dataTable.getLength());
        return new DataTableResponse<KafkaTopicApplication>(
                dataTable.getStart(),
                dataTable.getLength(),
                dataTable.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    @ResponseBody
    @RequestMapping("/hive-table-access-application/own-applications.do")
    public Object listHiveTableAccessApplications(
            HttpServletRequest request, DataTableRequest dataTable) {
        User currentUser = getCurrentUser(request);
        HiveTableAccessApplication queryCondition = new HiveTableAccessApplication();
        queryCondition.setCreator(currentUser.getPyName());
        Page<HiveTableAccessApplication> page =
                hiveTableAccessApplicationService.listHiveTableAccessApplications(
                        queryCondition, dataTable.getStart() / dataTable.getLength(), dataTable.getLength());
        return new DataTableResponse<HiveTableAccessApplication>(
                dataTable.getStart(),
                dataTable.getLength(),
                dataTable.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    @Admin
    @ResponseBody
    @RequestMapping("/hive-table-access-application/list.do")
    public Object listHiveTableAccessApplications(DataTableRequest dataTable) {
        Page<HiveTableAccessApplication> page =
                hiveTableAccessApplicationService.listHiveTableAccessApplications(
                        null, dataTable.getStart() / dataTable.getLength(), dataTable.getLength());
        return new DataTableResponse<HiveTableAccessApplication>(
                dataTable.getStart(),
                dataTable.getLength(),
                dataTable.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    @Admin
    @ResponseBody
    @RequestMapping("/assess-kafka-topic-application.do")
    public Object assessKafkaTopicApplication(
            Integer id, String topicName, String assessComment, Integer status) {
        kafkaTopicApplicationService.assessKafkaTopicApplication(
                id, topicName, assessComment, KafkaTopicApplication.Status.valueOf(status));
        return success();
    }

    @Admin
    @ResponseBody
    @RequestMapping("/assess-hive-table-access-application.do")
    public Object assessHiveTableAccessApplication(Integer id, String assessComment, Integer status) {
        hiveTableAccessApplicationService.assessHiveTableAccessApplication(
                id, assessComment, HiveTableAccessApplication.Status.valueOf(status));
        return success();
    }

    @ResponseBody
    @RequestMapping("/list-hive-dbs.do")
    public Object listHiveDbs() {
        List<HiveMetadataService.Db> list = hiveMetadataService.listDbs();
        return success(list);
    }

    @ResponseBody
    @RequestMapping("/list-hive-tbls.do")
    public Object listHiveTbls(Integer dbId) {
        List<HiveMetadataService.Tbl> list = hiveMetadataService.listTbls(dbId);
        return success(list);
    }
}
