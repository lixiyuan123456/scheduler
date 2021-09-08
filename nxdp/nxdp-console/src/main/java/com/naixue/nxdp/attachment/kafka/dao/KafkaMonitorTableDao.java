package com.naixue.nxdp.attachment.kafka.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.naixue.nxdp.attachment.kafka.model.KafkaMonitorTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class KafkaMonitorTableDao {

    @Autowired
    private JdbcTemplate kafkaMonitorJdbcTemplate;


    /**
     * @author wangkaixuan
     * @date 2018年7月2日
     * @Description:查询kafka图表数据
     */
    public List<KafkaMonitorTable> queryListByTime(String startTime, String endTime, String topicNames, String consumerNames) {
        String sql = "select * from kafka_monitor_table where date_time >= ? and date_time <= ? and topic_name in (" + topicNames + ") and consumer_name in (" + consumerNames + ") order by date_time";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        params.put("topicNames", topicNames);
        params.put("consumerNames", consumerNames);
        RowMapper<KafkaMonitorTable> rowMapper = new BeanPropertyRowMapper<>(KafkaMonitorTable.class);
        List<KafkaMonitorTable> kafaList = kafkaMonitorJdbcTemplate.query(sql, rowMapper, startTime, endTime);

        return kafaList;
    }

    /**
     * @author wangkaixuan
     * @date 2018年7月2日
     * @Description:查询kafka下拉选项列表
     */
    public List<KafkaMonitorTable> querySelectList(String startTime, String endTime) {

        String sql = "select topic_name,consumer_name,max(lag_size) lag_size from kafka_monitor_table where date_time >= ? and date_time <= ? group by topic_name,consumer_name";

        RowMapper<KafkaMonitorTable> rowMapper = new BeanPropertyRowMapper<>(KafkaMonitorTable.class);

        List<KafkaMonitorTable> kafaList = kafkaMonitorJdbcTemplate.query(sql, rowMapper, startTime, endTime);

        return kafaList;
    }

}
