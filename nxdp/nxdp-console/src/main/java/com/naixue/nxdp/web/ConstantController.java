package com.naixue.nxdp.web;

import com.naixue.nxdp.config.CFG;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consts")
public class ConstantController extends BaseController {

    @RequestMapping("/hue-url.json")
    public Object getHUE_URL() {
        return success("HUE_URL", CFG.HUE_URL);
    }

    @RequestMapping("/websocket-hadoop-job-execute-log-reader-url")
    public Object getWEBSOCKET_HADOOP_JOB_EXECUTE_LOG_READER_URL() {
        return success("const", CFG.WEBSOCKET_HADOOP_JOB_EXECUTE_LOG_READER_URL);
    }
}
