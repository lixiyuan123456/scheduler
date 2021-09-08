package com.naixue.nxdp.web;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.annotation.Admin;
import com.naixue.nxdp.annotation.Register;
import com.naixue.nxdp.service.HadoopBindingService;
import com.naixue.nxdp.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.naixue.nxdp.attachment.hue.service.HueService;

@Controller
@RequestMapping("/register")
public class RegisterApiController extends BaseController {

    @Autowired
    HueService hueService;
    @Autowired
    HadoopBindingService hadoopBindingService;
    @Autowired
    private RegisterService registerService;

    @Register
    @ResponseBody
    @RequestMapping("/register.do")
    public Object register(com.naixue.nxdp.model.Register form) {
        registerService.register(form);
        return success();
    }

    @Admin
    @ResponseBody
    @RequestMapping("/list-registers")
    public Object listRegisters(DataTableRequest dataTable) {
        Page<com.naixue.nxdp.model.Register> page =
                registerService.listRegisters(
                        dataTable.getStart() / dataTable.getLength(), dataTable.getLength());
        return new DataTableResponse<com.naixue.nxdp.model.Register>(
                dataTable.getStart(),
                dataTable.getLength(),
                dataTable.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    @Admin
    @ResponseBody
    @RequestMapping("/operate")
    public Object operate(
            HttpServletRequest request, Integer id, Integer operate, String hadoopUserGroupName) {
        registerService.operate(getCurrentUser(request), id, operate, hadoopUserGroupName);
        return success();
    }

    @Admin
    @ResponseBody
    @RequestMapping("/all-hadoop-user-groups.do")
    public Object getAllHadoopUserGroups() {
        return success("hadoopUserGroups", hadoopBindingService.getAllHadoopUserGroups());
    }

    @Admin
    @ResponseBody
    @RequestMapping("/new-hadoop-user-group.do")
    public Object newHadoopUserGroup(Integer registerDeptId, String hadoopUserName) {
        // zzdp
        hadoopBindingService.createHadoopBindings(registerDeptId, hadoopUserName);
        // iQuery
        hueService.createAuthGroupWithDefaultPermissions(hadoopUserName);
        return success();
    }
}
