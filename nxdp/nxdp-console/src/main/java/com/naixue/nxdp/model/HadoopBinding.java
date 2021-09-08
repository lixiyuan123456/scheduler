package com.naixue.nxdp.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.naixue.nxdp.model.metadata.MetadataHiveDb;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "metadataHiveDbs")
@Entity
@Table(name = "t_hadoop_user")
public class HadoopBinding implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门id
     */
    @Id
    @Column(name = "department_id")
    private Integer deptId;

    /**
     * 用户名
     */
    @Column(name = "hadoop_user_name")
    private String proxyCode;

    @Transient
    private String hadoopUserGroupName;
    /**
     * 代号
     */
    @Column(name = "hadoop_code")
    private String hadoopCode;
    @ManyToMany
    @JoinTable(
            name = "t_metadata_hive_db_permission", // 中间表，默认是表1_表2
            joinColumns = {
                    @JoinColumn(name = "hadoop_user_group_name", referencedColumnName = "hadoop_user_name")
            }, // 本类字段xx在中间表中对应的字段
            inverseJoinColumns = {
                    @JoinColumn(name = "hive_db_id", referencedColumnName = "id")
            }) // 对方类字段xx在中间表对应的字段
    private List<MetadataHiveDb> metadataHiveDbs;

    public String getHadoopUserGroupName() {
        return this.proxyCode;
    }

    public void setHadoopUserGroupName(String hadoopUserGroupName) {
        this.proxyCode = hadoopUserGroupName;
    }
}
