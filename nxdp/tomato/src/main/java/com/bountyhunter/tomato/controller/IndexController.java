package com.bountyhunter.tomato.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/")
public class IndexController {

    @RequestMapping("/")
    public Object index(HttpServletRequest request) {
        request.getSession().setAttribute("springsession", "springsession");
        return "/index";
    }
}
