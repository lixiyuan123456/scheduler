package com.naixue.nxdp.attachment.kafka.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.naixue.nxdp.service.MonitorChartApiService;
import com.naixue.nxdp.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.naixue.nxdp.attachment.kafka.model.KafkaMonitorTable;
import com.naixue.nxdp.attachment.kafka.model.ResultModel;
import com.naixue.nxdp.attachment.kafka.service.IKafkaService;

@Controller
@RequestMapping("/kafka")
public class KafkaController extends BaseController {

    @Autowired
    private IKafkaService kafkaServiceImpl;

    /**
     * wangkaixuan
     * 跳转kafka页面
     *
     * @return
     */
    @RequestMapping("/kafkaMonitor")
    public ModelAndView kafkaMonitor() {
        ModelAndView mv = new ModelAndView("attachment/kafka/kafkaMonitor");
        //默认时间范围
        Long now = System.currentTimeMillis();
        Long now_30 = now - 60 * 60 * 1000;

        Timestamp start = new Timestamp(now_30);
        Timestamp end = new Timestamp(now);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        mv.addObject("startTime", sf.format(start));
        mv.addObject("endTime", sf.format(end));
        //默认topic
        List<KafkaMonitorTable> querySelectList = null;
        ResultModel model = kafkaServiceImpl.querySelectList(sf.format(start), sf.format(end));
        if (model.getErrorCode() == 0) {
            querySelectList = (List<KafkaMonitorTable>) model.getData();
        }

        List<String[]> selectList = new ArrayList<>();
        if (querySelectList != null && querySelectList.size() > 0) {

            for (Iterator iterator = querySelectList.iterator(); iterator.hasNext(); ) {
                KafkaMonitorTable kmt = (KafkaMonitorTable) iterator.next();
                String[] arr = new String[2];
                arr[0] = kmt.getTopicName() + ";" + kmt.getConsumerName() + ";" + kmt.getId();
                arr[1] = kmt.getTopicName() + "-" + kmt.getConsumerName() + ":" + kmt.getLagSize();
                selectList.add(arr);
            }
        }

        mv.addObject("querySelectList", selectList);

        return mv;
    }

    /**
     * @author wangkaixuan
     * @date 2018年7月3日
     * @Description:查询kafka集群图表数据
     */
    @RequestMapping("/kafkaMonitor-statistic")
    @ResponseBody
    public Object kafkaMonitorStatistic(
            @RequestParam(name = "startTime") String startTime,
            @RequestParam(name = "endTime") String endTime,
            @RequestParam(name = "selectTopic") String selectTopic) {
        Map<String, List<MonitorChartApiService.DailyStatisticData>> kafkaStatistic = kafkaServiceImpl.queryEchartsData(startTime, endTime, selectTopic);
        return success("success", "value", kafkaStatistic);
    }


    /**
     * @author wangkaixuan
     * @date 2018年7月3日
     * @Description:查询kafka集群图表数据
     */
    @RequestMapping("/getKafkaSelectData")
    @ResponseBody
    public Object getKafkaSelectData(
            @RequestParam(name = "startTime") String startTime,
            @RequestParam(name = "endTime") String endTime) {

        //默认topic
        List<KafkaMonitorTable> querySelectList = null;
        ResultModel model = kafkaServiceImpl.querySelectList(startTime, endTime);
        if (model.getErrorCode() == 0) {
            querySelectList = (List<KafkaMonitorTable>) model.getData();
        } else {
            return success("error", "value", model.getMessage());
        }

        List<String[]> selectList = new ArrayList<>();
        if (querySelectList != null && querySelectList.size() > 0) {

            for (Iterator iterator = querySelectList.iterator(); iterator.hasNext(); ) {
                KafkaMonitorTable kmt = (KafkaMonitorTable) iterator.next();
                String[] arr = new String[2];
                arr[0] = kmt.getTopicName() + ";" + kmt.getConsumerName() + ";" + kmt.getId();
                arr[1] = kmt.getTopicName() + "-" + kmt.getConsumerName() + ":" + kmt.getLagSize();
                selectList.add(arr);
            }
        }
        return success("success", "value", selectList);
    }

}
