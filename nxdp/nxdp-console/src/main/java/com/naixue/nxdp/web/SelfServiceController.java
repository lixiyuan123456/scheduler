package com.naixue.nxdp.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.annotation.Register;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/self-service")
public class SelfServiceController extends BaseController {

    @Autowired
    private RegisterService registerService;

    @Register
    @RequestMapping("/register")
    public String register(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        List<com.naixue.nxdp.model.Register> registers =
                registerService.listRegistersByUserId(currentUser.getId());
        request.setAttribute("registers", registers);
        return "selfservice/register";
    }
}
