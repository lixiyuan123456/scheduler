package com.naixue.nxdp.web.openapi;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.naixue.nxdp.model.JobExecuteLog;
import com.naixue.nxdp.service.JobExecuteLogService;
import com.naixue.nxdp.util.DateUtils;
import com.naixue.nxdp.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/openApi")
public class OpenApiController extends BaseController {

    @Autowired
    private JobExecuteLogService jobExecuteLogService;

    public static void main(String[] args) {
        Date date = DateUtils.getBeginningOfDay(new Date());
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
        System.out.println(date.getTime() / 1000);
    }

    @RequestMapping("/job_{jobId}/{endTimestamp}_runningState")
    public Object getJobRunningState(
            @PathVariable("jobId") Integer jobId, @PathVariable("endTimestamp") String endTimestamp) {
        JobExecuteLog jobExecuteLog =
                jobExecuteLogService.getJobExecuteLogByJobIdAndTime(jobId, endTimestamp);
        return success(jobExecuteLog.getJobState());
    }
}
