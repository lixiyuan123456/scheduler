package com.naixue.nxdp.attachment.goshawk.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author 刘蒙
 */
@Data
@Entity
@Table(name = "t_cluster_report_spark_overresource")
public class ReportSpark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "job_id")
    private String jobId;

    @Column(name = "application_name")
    private String applicationName;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "total_cpu")
    private Integer totalCpu;

    @Column(name = "total_memory")
    private Integer totalMemory;

    @Column(name = "executor_memory")
    private Integer executorMemory;

    @Column(name = "tuning")
    private String tuning;

    @Column(name = "application_type")
    private String applicationType;

    @Column(name = "gmt_create")
    private Date gmtCreate;
}
