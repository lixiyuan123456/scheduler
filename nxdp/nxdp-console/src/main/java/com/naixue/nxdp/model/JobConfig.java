package com.naixue.nxdp.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.util.StringUtils;

import lombok.Data;

/**
 * @Author: wangyu @Created by 2017/11/8
 */
@Data
@Entity
@Table(name = "t_job_config")
public class JobConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "job_name", length = 255)
    private String jobName;

    @Column(length = 4)
    private Integer type;

    @Column(length = 4)
    private Integer status;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(length = Integer.MAX_VALUE)
    private String description;

    @Column(length = 255)
    private String tags;

    @Column(length = Integer.MAX_VALUE)
    private String details;

    @Column(name = "code_edit_lock")
    private Integer codeEditLock;

    @Column(name = "editer_id", length = 50)
    private String editerId;

    @Column(name = "editer_name", length = 20)
    private String editerName;

    @Column(name = "created", insertable = false, updatable = false)
    private Timestamp created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Timestamp updated;

    public void setJobName(String jobName) {
        if (!StringUtils.isEmpty(jobName)) {
            jobName = jobName.replace(" ", "");
            jobName = StringEscapeUtils.escapeSql(jobName);
        }
        this.jobName = jobName;
    }

    public static enum JobType {
        DATA_EXTRACT_SCRIPT(1, "数据抽取脚本"),

        MYSQL_SCRIPT(2, "MYSQL脚本"),

        HIVE(3, "HIVE"),

        MAPREDUCE(4, "MAPREDUCE"),

        SHELL(5, "SHELL"),

        SPARK(7, "SPARK"),

        SPARK_STREAMING(12, "SPARK_STREAMING"),

        SPARK_SQL(16, "SPARK_SQL"),

        SHELL_IDE(21, "SHELL_IDE"),

        SCHEDULED_JOB(99, "SCHEDULED_JOB");

        private int id;
        private String name;
        private JobType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static JobType getEnum(int id) {
            for (JobType e : JobType.values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static enum JobStatus {
        NORMAL(2, "正常"),

        DELETE(99, "删除");

        private Integer id;
        private String name;

        private JobStatus(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
