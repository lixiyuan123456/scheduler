package com.naixue.nxdp.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "t_job_dependencies")
public class JobDependencies {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "job_id")
    private Integer jobId;

    @Transient
    private JobConfig jobConfig;

    @Transient
    private JobSchedule jobSchedule;

    @Column(name = "dependent_job_id")
    private Integer dependentJobId;

    @Transient
    private JobConfig parentJobConfig;

    @Transient
    private JobSchedule parentJobSchedule;

    @Transient
    private List<JobIO> jobIOs = new ArrayList<>();

    public JobDependencies(Integer jobId, Integer dependentJobId) {
        this.jobId = jobId;
        this.dependentJobId = dependentJobId;
    }
}
