package com.naixue.nxdp.model.metadata;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
@Entity
@Table(name = "`t_metadata_hive_table`")
public class DeprecatedMetadataHiveTable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private Integer id;

    @NotNull(message = "表ID不允许为空")
    @Column(name = "`table_id`")
    private Integer tableId;

    @NotNull(message = "服务器不允许为空")
    @Column(name = "`server_id`")
    private Integer serverId;

    @NotNull(message = "数据库不允许为空")
    @Column(name = "`db_id`")
    private Integer dbId;

    @NotNull(message = "表名不允许为空")
    @Column(name = "`name`")
    private String name;

    @NotNull(message = "表全名不允许为空")
    @Column(name = "`full_name`")
    private String fullName;

    @NotNull(message = "表分区不允许为空")
    @Column(name = "`partition`")
    private Integer partition;

    @NotNull(message = "表描述信息不允许为空")
    @Column(name = "`description`")
    private String description;

    @NotNull(message = "标签不允许为空")
    @Column(name = "`label_id`")
    private Integer lebelId;

    @NotNull(message = "子标签不允许为空")
    @Column(name = "`child_label_id`")
    private Integer childLebelId;

    @NotNull(message = "表分组不允许为空")
    @Column(name = "`group`")
    private Integer group;

    @NotNull(message = "表层级不允许为空")
    @Column(name = "`level`")
    private Integer level;

    @NotNull(message = "表创建人ID不允许为空")
    @Column(name = "`creator_id`")
    private String creatorId;

    @NotNull(message = "表修改人ID不允许为空")
    @Column(name = "`modifier_id`")
    private String modifierId;

    @Transient
    private String creator;

    @Transient
    private String modifier;

    @NotNull(message = "表字段内容不允许为空")
    @Column(name = "`json`")
    private String json;

    @Column(name = "`status`")
    private Integer status = 0;

    @Column(name = "`version`")
    private String version;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`create_time`", insertable = false, updatable = false)
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`modify_time`", insertable = false, updatable = false)
    private Date modifyTime;

    @Column(name = "`type`")
    private Integer type;

    @Column(name = "`modify_location`")
    private Integer modifyLocation;

    @Column(name = "`location`")
    private String location;

    @Column(name = "`update_type`")
    private Integer updateType;

    @Column(name = "`storage_format`")
    private Integer storageFormat;

    @Column(name = "`char_separator`")
    private String charSeparator;

    public static enum TablePartition {
        _0P(0, "_0p"),
        _1H(1, "_1h"),
        _1D(2, "_1d"),
        _1W(3, "_1w"),
        _1M(4, "_1m"),
        _1Q(5, "_1q"),
        _1Y(6, "_1y");

        private Integer code;
        private String name;

        TablePartition(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static TablePartition getEnum(Integer code) {
            for (TablePartition e : TablePartition.values()) {
                if (e.getCode().equals(code)) {
                    return e;
                }
            }
            throw new NullPointerException();
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static enum TableGroup {
        INNER(1, "内部表"),
        OUTER(2, "外部表");

        private Integer code;
        private String name;

        TableGroup(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static TableGroup getEnum(Integer code) {
            for (TableGroup e : TableGroup.values()) {
                if (e.getCode().equals(code)) {
                    return e;
                }
            }
            throw new NullPointerException();
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static enum TableLevel {
        RAWDB(1, "快照表(rawdb)"),
        DW(2, "事实明细层(dw)"),
        DM(3, "集市层(dm)"),
        DIM(4, "字典维表层(dim)"),
        APP(5, "应用报表层(app)"),
        TMP(6, "临时数据层(tmp)");

        private Integer code;
        private String name;

        TableLevel(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static TableLevel getEnum(Integer code) {
            for (TableLevel e : TableLevel.values()) {
                if (e.getCode().equals(code)) {
                    return e;
                }
            }
            throw new NullPointerException();
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static enum TableStatus {
        DELETE(-1, "删除"),
        // 未在HIVE库建表
        DRAFT(0, "草稿"),
        // 在HIVE建表成功
        FORMAL(1, "正式");

        private Integer code;
        private String name;

        TableStatus(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static TableStatus getEnum(Integer code) {
            for (TableStatus e : TableStatus.values()) {
                if (e.getCode().equals(code)) {
                    return e;
                }
            }
            throw new NullPointerException();
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static enum TableUpdateType {
        FULL(1, "_full"),
        INC(2, "_inc");

        private Integer code;
        private String name;

        TableUpdateType(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static TableUpdateType getEnum(Integer code) {
            for (TableUpdateType e : TableUpdateType.values()) {
                if (e.getCode().equals(code)) {
                    return e;
                }
            }
            throw new RuntimeException("没有code=" + code + "对应的枚举");
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static enum TableStorageFormat {
        TEXTFILE(1, "textfile"),
        PARQUET(2, "parquet");

        private Integer code;
        private String name;

        TableStorageFormat(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static TableStorageFormat getEnum(Integer code) {
            for (TableStorageFormat e : TableStorageFormat.values()) {
                if (e.getCode().equals(code)) {
                    return e;
                }
            }
            throw new RuntimeException("没有code=" + code + "对应的枚举");
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
