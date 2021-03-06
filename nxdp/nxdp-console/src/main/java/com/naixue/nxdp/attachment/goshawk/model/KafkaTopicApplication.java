package com.naixue.nxdp.attachment.goshawk.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
@Entity
@Table(name = "t_kafka_topic_application")
public class KafkaTopicApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "topic_name")
    private String topicName;

    @Column(name = "daily_data_capacity")
    private Long dailyDataCapacity;

    @Transient
    private Integer partitions;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "creator")
    private String creator;

    @Column(name = "creator_name")
    private String creatorName;

    @Column(name = "creator_dept_name")
    private String creatorDeptName;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status")
    private Status status = Status.WAITING_ASSESS;

    @Column(name = "assessor")
    private String assessor = "";

    @Column(name = "assessor_name")
    private String assessorName = "";

    @Column(name = "assess_comment")
    private String assessComment = "";

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "create_time")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "modify_time")
    private Date modifyTime;

    // ??????????????????50G   5?????????
    // ??????????????????50G??????100G   10?????????
    // ??????????????????100G??????500G   15?????????
    // ??????????????????500G   25?????????
    // dailyDataCapacity   ??????(M)
    public Integer getPartitions() {
        Integer partitions = 5;
        if (getDailyDataCapacity() != null) {
            Integer dataCapacity = ((Double) Math.ceil(getDailyDataCapacity() / 1024)).intValue();
            if (dataCapacity >= 50 && dataCapacity < 100) {
                partitions = 10;
            } else if (dataCapacity >= 100 && dataCapacity < 500) {
                partitions = 15;
            } else if (dataCapacity >= 500) {
                partitions = 25;
            }
        }
        return partitions;
    }

    public static enum Status {
        WAITING_ASSESS, // ????????????
        PASS, // ????????????
        NOT_PASS; // ???????????????

        public static Status valueOf(Integer ordinal) {
            if (ordinal < 0 || ordinal >= values().length) {
                throw new RuntimeException("?????????ordinal=" + ordinal + "?????????");
            }
            return values()[ordinal];
        }
    }
}
