package com.naixue.nxdp.model.metadata;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "t_metadata_hive_db_permission")
public class MetadataHiveDbPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "hadoop_user_group_name")
    private String hadoopUserGroupName;

    @Column(name = "hive_db_id")
    private Integer hiveDbId;
}
