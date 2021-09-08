package com.naixue.nxdp.web;

import com.naixue.nxdp.model.JobWorkPendingQueue;
import com.naixue.nxdp.service.JobWorkPendingQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/jobWorkPendingQueue")
public class JobWorkPendingQueueController extends BaseController {

    @Autowired
    private JobWorkPendingQueueService jobWorkPendingQueueService;

    @RequestMapping("/page.do")
    @ResponseBody
    public Object page(DataTableRequest<JobWorkPendingQueue> dataTable) {
        JobWorkPendingQueue condition = new JobWorkPendingQueue();
        condition.setBatchNumber(dataTable.getSearch().get(DataTableRequest.Search.value.toString()));
        Page<JobWorkPendingQueue> page =
                jobWorkPendingQueueService.pageJobWorkPendingQueues(
                        condition, dataTable.getStart() / dataTable.getLength(), dataTable.getLength());
        return new DataTableResponse<JobWorkPendingQueue>(
                dataTable.getStart(),
                dataTable.getLength(),
                dataTable.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    @RequestMapping("/killByBatchNumber.do")
    @ResponseBody
    public Object killByBatchNumber(String batchNumber) {
        jobWorkPendingQueueService.killByBatchNumber(batchNumber);
        return success();
    }
}
