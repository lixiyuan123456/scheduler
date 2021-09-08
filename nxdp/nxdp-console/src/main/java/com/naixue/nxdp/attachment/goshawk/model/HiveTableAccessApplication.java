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

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
@Entity
@Table(name = "t_hive_table_access_application")
public class HiveTableAccessApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "db_id")
    private Integer dbId;

    @Column(name = "db_name")
    private String dbName;

    @Column(name = "table_id")
    private Integer tableId;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "table_location")
    private String tableLocation;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "permission")
    private Permission permission;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "creator")
    private String creator;

    @Column(name = "creator_name")
    private String creatorName;

    @Column(name = "creator_dept_name")
    private String creatorDeptName;

    @Column(name = "proxy_account")
    private String proxyAccount;

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

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status")
    private Status status = Status.WAITING_ASSESS;

    public static enum Permission {
        R, // 可读
        RW; // 可读可写

        public static Permission valueOf(Integer ordinal) {
            if (ordinal < 0 || ordinal >= values().length) {
                throw new RuntimeException("不存在ordinal=" + ordinal + "枚举值");
            }
            return values()[ordinal];
        }
    }

    public static enum Status {
        WAITING_ASSESS, // 等待审批
        PASS, // 审批通过
        NOT_PASS; // 审批不通过

        public static Status valueOf(Integer ordinal) {
            if (ordinal < 0 || ordinal >= values().length) {
                throw new RuntimeException("不存在ordinal=" + ordinal + "枚举值");
            }
            return values()[ordinal];
        }
    }
}
