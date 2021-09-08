package com.naixue.nxdp.web.zstream.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/zstream")
public class ZStreamController {

    @RequestMapping("")
    public String index() {
        return "/zstream/index";
    }

    @RequestMapping("/udx")
    public String toUdx() {
        return "/zstream/udx";
    }

    @RequestMapping("/udx/uploader")
    public String toUdxUploader() {
        return "/zstream/udx-uploader";
    }

    @RequestMapping("/job/editor")
    public String toJobEditor(HttpServletRequest request, Integer jobId, String action) {
        request.setAttribute("jobId", jobId);
        request.setAttribute("action", action);
        return "/zstream/job-editor";
    }

    @RequestMapping("/job/details")
    public String toJobDetails(HttpServletRequest request, @RequestParam("jobId") Integer jobId) {
        request.setAttribute("jobId", jobId);
        return "/zstream/job-details";
    }

    @RequestMapping("/job")
    public String toJobSqlTemplates() {
        return "/zstream/job";
    }
}
