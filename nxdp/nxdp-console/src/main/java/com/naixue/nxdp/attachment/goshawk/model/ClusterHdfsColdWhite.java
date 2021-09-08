package com.naixue.nxdp.attachment.goshawk.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "t_cluster_hdfs_cold_white")
public class ClusterHdfsColdWhite implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "keywords")
    private String keywords;

    @Column(name = "creator")
    private String creator;

    @Column(name = "create_time", insertable = false, updatable = false)
    private Date createTime;

    @Column(name = "modify_time", insertable = false, updatable = false)
    private Date modifyTime;
}
