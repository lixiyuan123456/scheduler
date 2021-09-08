package com.naixue.nxdp.web;

import com.naixue.nxdp.annotation.Admin;
import com.naixue.nxdp.service.HadoopBindingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.naixue.nxdp.attachment.hue.service.HueService;

@Controller
@RequestMapping("/system")
public class SystemController extends BaseController {

    @Autowired
    HueService hueService;

    @Autowired
    HadoopBindingService hadoopBindingService;

    @Admin
    @RequestMapping("/users")
    public String users() {
        return "system/users-list";
    }

    @Admin
    @RequestMapping("/grant")
    public String grant(Model model) {
        model.addAttribute("hadoopUserGroups", hadoopBindingService.getAllHadoopUserGroups());
        return "system/registers-list";
    }

    @Admin
    @RequestMapping("/manual")
    public String manual() {
        return "system/manual";
    }

    @Admin
    @ResponseBody
    @RequestMapping("/manual/sync-all-users-to-hue.do")
    public Object syncAllUsers2Hue() {
        hueService.syncZzdpUsers2Hue();
        return success();
    }
}
