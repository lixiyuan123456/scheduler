package com.naixue.nxdp.data.model.zstream;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString(exclude = {"job"})
@Entity
@Table(name = "t_zstream_job_log")
public class ZstreamJobLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "job_id")
    private Integer jobId;
    @Column(name = "job_version")
    private String jobVersion = "";
    @Column(name = "application_id")
    private String applicationId = "";
    @Column(name = "application_job_id")
    private String applicationJobId = "";
    @Column(name = "log")
    private String log = "";
    @Column(name = "create_time", insertable = false, updatable = false)
    private Date createTime;
    @Column(name = "modify_time", insertable = false, updatable = false)
    private Date modifyTime;
    @Column(name = "status")
    private Integer status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", insertable = false, updatable = false)
    private ZstreamJob job;
    @Transient
    private String dashboardUrl;

    public ZstreamJobLog() {
    }

    public ZstreamJobLog(
            final Integer jobId,
            final String jobVersion,
            final String applicationId,
            final String applicationJobId,
            final String log,
            final Integer status) {
        this.jobId = jobId;
        this.jobVersion = jobVersion;
        this.applicationId = applicationId;
        this.applicationJobId = applicationJobId;
        this.log = log;
        this.status = status;
    }
}
