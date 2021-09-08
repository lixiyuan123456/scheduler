package com.naixue.nxdp.attachment.goshawk.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name = "t_kafka_offset_registry_application")
public class KafkaOffsetRegistryApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "cluster")
    private Integer cluster;

    @Column(name = "consumer")
    private String consumer;

    @Column(name = "topic")
    private String topic;

    @Column(name = "threshold")
    private Integer threshold;

    @Column(name = "description")
    private String description;

    @Column(name = "applicant")
    private String applicant;

    @Column(name = "applicant_name")
    private String applicantName;

    @Column(name = "assessor")
    private String assessor = "";

    @Column(name = "status")
    private Integer status = 0; // 默认待审批

    @Column(name = "assess_comment")
    private String assessComment = "";

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "create_time")
    private Date createTime = new Date();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "modify_time")
    private Date modifyTime = new Date();

    public static enum Status {
        // 等待审批
        WAITING_ASSESS(0, "等待审批"),
        // 审批通过
        PASS(1, "审核通过"),
        // 审批驳回
        NOT_PASS(2, "审核不通过");

        @Getter
        @Setter
        private Integer code;
        @Getter
        @Setter
        private String name;

        Status(final Integer code, final String name) {
            this.code = code;
            this.name = name;
        }

        public static Status getStatus(final Integer code) {
            Status status = null;
            switch (code) {
                case 0:
                    status = WAITING_ASSESS;
                    break;
                case 1:
                    status = PASS;
                    break;
                case 2:
                    status = NOT_PASS;
                    break;
                default:
                    break;
            }
            return status;
        }
    }

    public static enum KafkaCluster {
        KAFKA_ZZ_1_0(0, "zz-Kafka-1.0"),
        KAFKA_58_0_8(1, "58-Kafak-0.8"),
        KAFKA_58_1_0(2, "58-Kafka-1.0");

        @Getter
        @Setter
        private Integer code;
        @Getter
        @Setter
        private String name;

        KafkaCluster(final Integer code, final String name) {
            this.code = code;
            this.name = name;
        }

        @SuppressWarnings("unchecked")
        public static String json() {
            SerializeConfig config = new SerializeConfig();
            config.configEnumAsJavaBean(KafkaCluster.class);
            return JSON.toJSONString(KafkaCluster.values(), config);
        }

        public static KafkaCluster getKafkaCluster(final Integer code) {
            return get(code);
        }

        private static KafkaCluster get(final Integer code) {
            KafkaCluster kc = null;
            switch (code) {
                case 0:
                    kc = KAFKA_ZZ_1_0;
                    break;
                case 1:
                    kc = KAFKA_58_0_8;
                    break;
                case 2:
                    kc = KAFKA_58_1_0;
                    break;
                default:
                    break;
            }
            return kc;
        }
    }
}
