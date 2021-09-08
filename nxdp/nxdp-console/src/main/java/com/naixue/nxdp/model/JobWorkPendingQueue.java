package com.naixue.nxdp.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.naixue.zzdp.data.BaseModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
// @AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_job_work_queue")
public class JobWorkPendingQueue extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Builder.Default
    @Column(name = "job_id")
    private Integer jobId = 0;

    @Column(name = "choose_run_time")
    private Date chooseRunTime;

    @Builder.Default
    @Column(name = "operator")
    private String operator = "";

    @Builder.Default
    @Column(name = "status")
    private Integer status = 0;

    @Builder.Default
    @Transient
    private Status enumStatus = Status.FALSE;

    @Version
    @Builder.Default
    @Column(name = "version")
    private Integer version = 0;

    @Builder.Default
    @Column(name = "batch_number")
    private String batchNumber = "";

    private JobWorkPendingQueue(
            Integer id,
            Integer jobId,
            Date chooseRunTime,
            String operator,
            Integer status,
            Status enumStatus,
            Integer version,
            String batchNumber) {
        this.id = id;
        this.jobId = jobId;
        this.chooseRunTime = chooseRunTime;
        this.operator = operator;
        this.status = status;
        this.enumStatus = enumStatus;
        this.version = version;
        this.batchNumber = batchNumber;
        if (status == 0 && enumStatus != Status.FALSE) {
            this.status = enumStatus.getCode();
        }
        if (enumStatus == Status.FALSE && status != 0) {
            this.enumStatus = Status.getStatus(status);
        }
    }

    @AllArgsConstructor
    public static enum Status {
        FALSE(0),
        TRUE(1);

        @Getter
        @Setter
        private int code;

        public static Status getStatus(final int code) {
            if (code == 0) {
                return Status.FALSE;
            } else {
                return Status.TRUE;
            }
        }
    }
}
