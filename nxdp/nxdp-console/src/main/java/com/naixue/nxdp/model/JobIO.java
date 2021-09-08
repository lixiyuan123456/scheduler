package com.naixue.nxdp.model;

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
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_job_io")
public class JobIO extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Builder.Default
    @Column(name = "job_id")
    private Integer jobId = 0;

    @Builder.Default
    @Column(name = "type")
    private Integer type = 0;
    @Builder.Default
    @Transient
    private Type enumType = Type.NONE;
    @Builder.Default
    @Column(name = "mode")
    private Integer mode = 0;
    @Builder.Default
    @Transient
    private Mode enumMode = Mode.NONE;
    @Builder.Default
    @Column(name = "value")
    private String value = "";

    public JobIO(
            Integer id,
            Integer jobId,
            Integer type,
            Type enumType,
            Integer mode,
            Mode enumMode,
            String value) {
        this.id = id;
        this.jobId = jobId;
        if (type == 0 && enumType != Type.NONE) {
            this.type = enumType.code;
        } else {
            this.type = type;
        }
        if (enumType == Type.NONE && type != 0) {
            this.enumType = Type.getEnum(type);
        } else {
            this.enumType = enumType;
        }
        if (mode == 0 && enumMode != Mode.NONE) {
            this.mode = enumMode.code;
        } else {
            this.mode = mode;
        }
        if (enumMode == Mode.NONE && mode != 0) {
            this.enumMode = Mode.getEnum(mode);
        } else {
            this.enumMode = enumMode;
        }
        this.value = value;
    }

    public void setType(Integer type) {
        this.type = type;
        this.enumType = Type.getEnum(type);
    }

    public Type getEnumType() {
        if (this.type != 0) {
            return Type.getEnum(this.type);
        }
        return this.enumType;
    }

    public void setEnumType(Type enumType) {
        this.enumType = enumType;
        this.type = enumType.code;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
        this.enumMode = Mode.getEnum(mode);
    }

    public Mode getEnumMode() {
        if (mode != 0) {
            return Mode.getEnum(this.mode);
        }
        return this.enumMode;
    }

    public void setEnumMode(Mode enumMode) {
        this.enumMode = enumMode;
        this.mode = enumMode.code;
    }

    @AllArgsConstructor
    public static enum Type {
        NONE(0),
        INPUT(1),
        OUTPUT(2);

        @Getter
        @Setter
        private Integer code;

        public static Type getEnum(final Integer code) {
            for (Type e : Type.values()) {
                if (e.getCode().equals(code)) {
                    return e;
                }
            }
            return null;
        }
    }

    @AllArgsConstructor
    public static enum Mode {
        NONE(0),
        DATATABLE(1),
        HDFS(2);

        @Getter
        @Setter
        private Integer code;

        public static Mode getEnum(final Integer code) {
            for (Mode e : Mode.values()) {
                if (e.getCode().equals(code)) {
                    return e;
                }
            }
            return null;
        }
    }
}
