package com.naixue.nxdp.attachment.kafka.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "kafka_monitor_table")
public class KafkaMonitorTable {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "date_time")
    private Timestamp dateTime;

    @Column(name = "topic_name")
    private String topicName;

    @Column(name = "consumer_name")
    private String consumerName;

    @Column(name = "log_size")
    private Integer logSize;

    @Column(name = "offset_size")
    private Integer offsetSize;

    @Column(name = "pyname")
    private Integer lagSize;
}
