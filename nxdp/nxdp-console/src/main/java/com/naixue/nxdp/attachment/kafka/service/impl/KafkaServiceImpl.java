package com.naixue.nxdp.attachment.kafka.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.naixue.nxdp.attachment.kafka.service.IKafkaService;
import com.naixue.nxdp.service.MonitorChartApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.naixue.nxdp.attachment.kafka.dao.KafkaMonitorTableDao;
import com.naixue.nxdp.attachment.kafka.model.KafkaMonitorTable;
import com.naixue.nxdp.attachment.kafka.model.ResultModel;
import com.naixue.nxdp.attachment.util.DateUtil;

/**
 * @author wangkaixuan kafka监控service
 */
@Service
public class KafkaServiceImpl implements IKafkaService {

    @Autowired
    private KafkaMonitorTableDao kafkaMonitorTableDao;

    /**
     * @author wangkaixuan
     * @date 2018年7月2日 @Description:查询kafka监控下拉选项
     */
    @Override
    public ResultModel querySelectList(String startTime, String endTime) {
        ResultModel res = new ResultModel();
        res.setErrorCode(-1);

        double diff = DateUtil.diff2DaysHour(endTime + ":00", startTime + ":00");
        if (diff > 48) {
            res.setMessage("查询范围过长，最长查询时间范围为48小时");
            return res;
        }
        if (diff < 0) {
            res.setMessage("查询时间格式异常");
            return res;
        }
        List<KafkaMonitorTable> selectList =
                kafkaMonitorTableDao.querySelectList(startTime + ":00", endTime + ":59");
        Collections.sort(
                selectList,
                new Comparator<KafkaMonitorTable>() {
                    @Override
                    public int compare(KafkaMonitorTable o1, KafkaMonitorTable o2) {
                        if (o1.getLagSize() < o2.getLagSize()) return 1;
                        else if (o1.getLagSize() > o2.getLagSize()) return -1;
                        else return 0;
                    }
                });
        res.setErrorCode(0);
        res.setData(selectList);
        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2018年7月2日 @Description:查询kafka监控图标数据
     */
    @Override
    public Map<String, List<MonitorChartApiService.DailyStatisticData>> queryEchartsData(
            String startTime, String endTime, String selectTopicStr) {
        // SimpleDateFormat sf = new SimpleDateFormat("MMdd HH:mm");
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        StringBuffer topicNames = new StringBuffer("");
        StringBuffer consumerNames = new StringBuffer("");
        Map<String, List<MonitorChartApiService.DailyStatisticData>> dataMap = new HashMap<String, List<MonitorChartApiService.DailyStatisticData>>();
        Map<String, List<KafkaMonitorTable>> topicMap = new HashMap<>();

        if (selectTopicStr != null && selectTopicStr.contains(",")) {
            String[] array = selectTopicStr.split(",");

            for (int i = 0; i < array.length; i++) {
                String str = array[i];
                String[] array2 = str.split(";");
                topicNames.append("'" + array2[0].trim() + "',");
                consumerNames.append("'" + array2[1].trim() + "',");
                topicMap.put(array2[0].trim() + "-" + array2[1].trim(), new ArrayList<KafkaMonitorTable>());
            }
        } else {
            return dataMap;
        }

        // 查询topic数
        int topicSize = topicMap.size() - 1;
        int topicIndex = 0; // topic下标

    /*if(selectTopic.size()>0) {
    	for (Iterator iterator = selectTopic.iterator(); iterator.hasNext();) {
    		KafkaMonitorTable kafkaMonitorTable = (KafkaMonitorTable) iterator.next();
    		topicNames.add(kafkaMonitorTable.getTopicName());
    		consumerNames.add(kafkaMonitorTable.getConsumerName());
    	}
    }else {
    	return dataMap;
    }*/

        // 获取所有时间
        List<String> dateList = DateUtil.get2DayMinute(startTime + ":00", endTime + ":00");
        if (dateList == null || dateList.size() <= 0) {
            return dataMap;
        }

        List<KafkaMonitorTable> selectList =
                kafkaMonitorTableDao.queryListByTime(
                        startTime + ":00",
                        endTime + ":59",
                        topicNames.toString().substring(0, topicNames.toString().lastIndexOf(",")),
                        consumerNames.toString().substring(0, consumerNames.toString().lastIndexOf(",")));
        // 将数据按照主题分组
        if (selectList != null && selectList.size() > 0) {
            for (Iterator iterator = selectList.iterator(); iterator.hasNext(); ) {
                KafkaMonitorTable kafkaMonitorTable = (KafkaMonitorTable) iterator.next();
                String onlyKey =
                        kafkaMonitorTable.getTopicName() + "-" + kafkaMonitorTable.getConsumerName();
                List<KafkaMonitorTable> dataList = topicMap.get(onlyKey);
                dataList.add(kafkaMonitorTable);
            }
        }

        // 循环处理所有topic数据
        for (Iterator iterator = topicMap.keySet().iterator(); iterator.hasNext(); ) {
            String onlyKey = (String) iterator.next();
            List<KafkaMonitorTable> seResList = topicMap.get(onlyKey);
            if (seResList.size() > 0) {

                // 值不为空，遍历日期列表赋值
                int dataIndex = 0;
                for (Iterator iterator2 = dateList.iterator(); iterator2.hasNext(); ) {
                    String xValue = iterator2.next().toString(); // 日期值
                    if (seResList.size() > dataIndex) {
                        // 获取当前数据
                        KafkaMonitorTable kafkaMonitorTable = (KafkaMonitorTable) seResList.get(dataIndex);
                        String kTime = sf2.format(kafkaMonitorTable.getDateTime());
                        if (kTime.equals(xValue)) {
                            // 是同一天，赋值，然后继续循环数据
                            dataIndex++;
                            List<MonitorChartApiService.DailyStatisticData> dataList = null;
                            if (dataMap.get(onlyKey) == null) {
                                dataList = new ArrayList<MonitorChartApiService.DailyStatisticData>();
                            } else {
                                dataList = dataMap.get(onlyKey);
                            }

                            MonitorChartApiService.DailyStatisticData data1 = new MonitorChartApiService.DailyStatisticData();
                            data1.setName(xValue);
                            data1.setValue(kafkaMonitorTable.getLogSize());
                            data1.setGroup("logSize");
                            data1.setChart(MonitorChartApiService.DailyStatisticChart.DAILY_JOB_STATISTIC);

                            MonitorChartApiService.DailyStatisticData data2 = new MonitorChartApiService.DailyStatisticData();
                            data2.setName(xValue);
                            data2.setValue(kafkaMonitorTable.getOffsetSize());
                            data2.setGroup("offsetSize");
                            data2.setChart(MonitorChartApiService.DailyStatisticChart.DAILY_JOB_STATISTIC);

                            MonitorChartApiService.DailyStatisticData data3 = new MonitorChartApiService.DailyStatisticData();
                            data3.setName(xValue);
                            data3.setValue(kafkaMonitorTable.getLagSize());
                            data3.setGroup("lagSize");
                            data3.setChart(MonitorChartApiService.DailyStatisticChart.DAILY_JOB_STATISTIC);

                            dataList.add(data1);
                            dataList.add(data2);
                            dataList.add(data3);

                            if (dataMap.get(onlyKey) == null) {
                                dataMap.put(onlyKey, dataList);
                            }

                        } else {
                            // 当前日期数据中没有，直接赋值
                            List<MonitorChartApiService.DailyStatisticData> dataList = null;
                            if (dataMap.get(onlyKey) == null) {
                                dataList = new ArrayList<MonitorChartApiService.DailyStatisticData>();
                            } else {
                                dataList = dataMap.get(onlyKey);
                            }

                            MonitorChartApiService.DailyStatisticData data1 = new MonitorChartApiService.DailyStatisticData();
                            data1.setName(xValue);
                            // data1.setValue(0);
                            data1.setGroup("logSize");
                            data1.setChart(MonitorChartApiService.DailyStatisticChart.DAILY_JOB_STATISTIC);

                            MonitorChartApiService.DailyStatisticData data2 = new MonitorChartApiService.DailyStatisticData();
                            data2.setName(xValue);
                            // data2.setValue(0);
                            data2.setGroup("offsetSize");
                            data2.setChart(MonitorChartApiService.DailyStatisticChart.DAILY_JOB_STATISTIC);

                            MonitorChartApiService.DailyStatisticData data3 = new MonitorChartApiService.DailyStatisticData();
                            data3.setName(xValue);
                            // data3.setValue(0);
                            data3.setGroup("lagSize");
                            data3.setChart(MonitorChartApiService.DailyStatisticChart.DAILY_JOB_STATISTIC);

                            dataList.add(data1);
                            dataList.add(data2);
                            dataList.add(data3);

                            if (dataMap.get(onlyKey) == null) {
                                dataMap.put(onlyKey, dataList);
                            }
                        }

                    } else {
                        // 日期还未结束，数据已结束，直接赋值
                        List<MonitorChartApiService.DailyStatisticData> dataList = null;
                        if (dataMap.get(onlyKey) == null) {
                            dataList = new ArrayList<MonitorChartApiService.DailyStatisticData>();
                        } else {
                            dataList = dataMap.get(onlyKey);
                        }

                        MonitorChartApiService.DailyStatisticData data1 = new MonitorChartApiService.DailyStatisticData();
                        data1.setName(xValue);
                        // data1.setValue(0);
                        data1.setGroup("logSize");
                        data1.setChart(MonitorChartApiService.DailyStatisticChart.DAILY_JOB_STATISTIC);

                        MonitorChartApiService.DailyStatisticData data2 = new MonitorChartApiService.DailyStatisticData();
                        data2.setName(xValue);
                        // data2.setValue(0);
                        data2.setGroup("offsetSize");
                        data2.setChart(MonitorChartApiService.DailyStatisticChart.DAILY_JOB_STATISTIC);

                        MonitorChartApiService.DailyStatisticData data3 = new MonitorChartApiService.DailyStatisticData();
                        data3.setName(xValue);
                        // data3.setValue(0);
                        data3.setGroup("lagSize");
                        data3.setChart(MonitorChartApiService.DailyStatisticChart.DAILY_JOB_STATISTIC);

                        dataList.add(data1);
                        dataList.add(data2);
                        dataList.add(data3);

                        if (dataMap.get(onlyKey) == null) {
                            dataMap.put(onlyKey, dataList);
                        }
                    }
                }

            } else {
                // 空数据，直接赋值
                for (Iterator iterator2 = dateList.iterator(); iterator2.hasNext(); ) {
                    // 循环日期列表进行赋值
                    String xValue = iterator2.next().toString(); // 日期值
                    List<MonitorChartApiService.DailyStatisticData> dataList = null;
                    if (dataMap.get(onlyKey) == null) {
                        dataList = new ArrayList<MonitorChartApiService.DailyStatisticData>();
                    } else {
                        dataList = dataMap.get(onlyKey);
                    }

                    MonitorChartApiService.DailyStatisticData data1 = new MonitorChartApiService.DailyStatisticData();
                    data1.setName(xValue);
                    // data1.setValue(0);
                    data1.setGroup("logSize");
                    data1.setChart(MonitorChartApiService.DailyStatisticChart.DAILY_JOB_STATISTIC);

                    MonitorChartApiService.DailyStatisticData data2 = new MonitorChartApiService.DailyStatisticData();
                    data2.setName(xValue);
                    // data2.setValue(0);
                    data2.setGroup("offsetSize");
                    data2.setChart(MonitorChartApiService.DailyStatisticChart.DAILY_JOB_STATISTIC);

                    MonitorChartApiService.DailyStatisticData data3 = new MonitorChartApiService.DailyStatisticData();
                    data3.setName(xValue);
                    // data3.setValue(0);
                    data3.setGroup("lagSize");
                    data3.setChart(MonitorChartApiService.DailyStatisticChart.DAILY_JOB_STATISTIC);

                    dataList.add(data1);
                    dataList.add(data2);
                    dataList.add(data3);

                    if (dataMap.get(onlyKey) == null) {
                        dataMap.put(onlyKey, dataList);
                    }
                }
            }
        }

        return dataMap;
    }
}
