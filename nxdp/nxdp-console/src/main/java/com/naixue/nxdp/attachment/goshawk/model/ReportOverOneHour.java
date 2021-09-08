package com.naixue.nxdp.attachment.goshawk.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author 刘蒙
 */
@Data
@Entity
@Table(name = "t_cluster_report_overonhour")
public class ReportOverOneHour {
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

    @Column(name = "start_time")
    private Date startTime;

    @Column(name = "end_time")
    private Date endTime;

    @Column(name = "run_time")
    private String runTime;

    @Column(name = "application_type")
    private String applicationType;

    @Column(name = "gmt_create")
    private Date gmtCreate;
}
