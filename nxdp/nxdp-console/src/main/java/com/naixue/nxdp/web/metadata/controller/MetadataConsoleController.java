package com.naixue.nxdp.web.metadata.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/metadata/console")
public class MetadataConsoleController {

    @RequestMapping("")
    public String console() {
        return "metadata/console";
    }
}
