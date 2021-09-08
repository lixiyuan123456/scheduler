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

import lombok.Data;

@Data
@Entity
@Table(name = "t_metadata_db_table")
public class MetadataDbTable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "server_id")
    private Integer serverId;

    @Column(name = "db_name")
    private String dbName;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "create_time", insertable = false, updatable = false)
    private Date createTime;

    @Column(name = "modify_time", insertable = false, updatable = false)
    private Date modifyTime;

    @Transient
    private Status status = Status.NEVER_LOADED;

    public static enum Status {
        NEVER_LOADED,
        LOADED;
    }
}
