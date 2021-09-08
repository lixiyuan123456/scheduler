package com.naixue.nxdp.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "t_dict_cluster")
public class HdpCluster implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    @Column(name = "cluster_name")
    private String clusterName;

    @Column(name = "cluster_type")
    private Integer clusterType;
}
