package com.naixue.nxdp.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Data;

@Data
@Entity
@Table(name = "t_daily_statistic")
public class DailyStatistic implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "date")
    private String date;

    @Column(name = "total_job_count")
    private Integer totalJobCount;

    @Column(name = "auto_job_exec_times")
    private Integer autoJobExecTimes;

    @Column(name = "manual_job_exec_times")
    private Integer manualJobExecTimes;

    @Column(name = "total_user_count")
    private Integer totalUserCount;

    @Column(name = "active_user_count")
    private Integer activeUserCount;

    @Column(name = "new_user_count")
    private Integer newUserCount;

    @Column(name = "total_job_exec_times")
    private Integer totalJobExecTimes;

    @Transient
    @Column(name = "create_time")
    private Date createTime;

    @Transient
    @Column(name = "modify_time")
    private Date modifyTime;
}
