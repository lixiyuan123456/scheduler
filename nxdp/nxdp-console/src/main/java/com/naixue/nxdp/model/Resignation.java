package com.naixue.nxdp.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

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
@Table(name = "t_resignation")
// @IdClass(UnionKey.class)
public class Resignation extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // @Id
    @Builder.Default
    @Column(name = "job_id")
    private Integer jobId = 0;

    @Builder.Default
    @Column(name = "job_name")
    private String jobName = "";

    @Builder.Default
    @Column(name = "job_url")
    private String jobUrl = "";

    @Builder.Default
    @Column(name = "job_type")
    private Integer jobType = 0;

    @Builder.Default
    @Transient
    private Type enumJobType = Type.NULL;
    @Builder.Default
    @Column(name = "source_developer")
    private String sourceDeveloper = "";
    @Builder.Default
    @Column(name = "source_receiver")
    private String sourceReceiver = "";
    @Builder.Default
    @Column(name = "target_developer")
    private String targetDeveloper = "";
    @Builder.Default
    @Column(name = "target_receiver")
    private String targetReceiver = "";
    @Builder.Default
    @Column(name = "holder")
    private String holder = "";
    @Builder.Default
    @Column(name = "status")
    private Integer status = 0;

    public Resignation(
            Integer id,
            Integer jobId,
            String jobName,
            String jobUrl,
            Integer jobType,
            Type enumJobType,
            String sourceDeveloper,
            String sourceReceiver,
            String targetDeveloper,
            String targetReceiver,
            String holder,
            Integer status) {
        this.id = id;
        this.jobId = jobId;
        this.jobName = jobName;
        this.jobUrl = jobUrl;
        this.jobType = jobType;
        this.enumJobType = enumJobType;
        this.sourceDeveloper = sourceDeveloper;
        this.sourceReceiver = sourceReceiver;
        this.targetDeveloper = targetDeveloper;
        this.targetReceiver = targetReceiver;
        this.holder = holder;
        this.status = status;
        if (jobType != 0 && enumJobType == Type.NULL) {
            this.enumJobType = Type.getType(jobType);
        }
        if (enumJobType != Type.NULL && jobType == 0) {
            this.jobType = enumJobType.getCode();
        }
    }

    public Type getEnumJobType() {
        if ((this.enumJobType == null || this.enumJobType == Type.NULL)
                && this.jobType != null
                && this.jobType != 0) {
            return Type.getType(this.jobType);
        }
        return this.enumJobType;
    }

    public void setEnumJobType(Type enumJobType) {
        this.enumJobType = enumJobType;
        this.jobType = enumJobType.getCode();
    }

    @AllArgsConstructor
    public static enum Type {
        NULL(0, "NULL"),

        COMMON_JOB(1, "COMMON_JOB"),

        ZSTREAM_JOB(2, "ZSTREAM_JOB");

        @Getter
        @Setter
        private int code;

        @Getter
        @Setter
        private String name;

        public static Type getType(final int code) {
            for (Type type : Type.values()) {
                if (type.getCode() == code) {
                    return type;
                }
            }
            return null;
        }
    }

    public static enum Status {
        UNDONE,
        DONE;

        public static int getStatus(Status status) {
            int code = 0;
            switch (status) {
                case UNDONE:
                    code = 0;
                    break;
                case DONE:
                    code = 1;
                    break;
                default:
                    break;
            }
            return code;
        }
    }

    public static class UnionKey implements Serializable {

        private static final long serialVersionUID = 1L;

        private Integer id;

        private Integer jobId;
    }
}
