package com.naixue.nxdp.attachment.goshawk.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author 刘蒙
 */
@Data
@Entity
@Table(name = "t_cluster_app_whitelist")
public class Whitelist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "app_name")
    private String appName;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "gmt_create")
    private Date gmtCreate;
}
