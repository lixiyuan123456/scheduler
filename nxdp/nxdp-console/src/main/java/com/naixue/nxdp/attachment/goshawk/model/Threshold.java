package com.naixue.nxdp.attachment.goshawk.model;

import lombok.Data;

import javax.persistence.*;

/**
 * @author 刘蒙
 */
@Data
@Entity
@Table(name = "t_cluster_threshold")
public class Threshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "threshold_key")
    private String key;

    @Column(name = "threshold_name")
    private String name;

    @Column(name = "threshold_value")
    private Integer value;

}
