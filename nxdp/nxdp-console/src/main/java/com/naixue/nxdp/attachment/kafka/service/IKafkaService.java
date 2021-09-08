package com.naixue.nxdp.attachment.kafka.service;

import java.util.List;
import java.util.Map;

import com.naixue.nxdp.service.MonitorChartApiService;
import com.naixue.nxdp.attachment.kafka.model.ResultModel;

/**
 * @author wangkaixuan
 * kafka监控service
 */
public interface IKafkaService {

    public ResultModel querySelectList(String startTime, String endTime);

    public Map<String, List<MonitorChartApiService.DailyStatisticData>> queryEchartsData(String startTime, String endTime, String selectTopic);

}
