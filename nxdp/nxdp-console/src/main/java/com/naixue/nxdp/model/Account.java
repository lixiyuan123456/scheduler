package com.naixue.nxdp.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "t_account")
public class Account implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "pyname")
    private String pyname;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

  /*@Column(name = "mobile")
  private String mobile;*/

  /*@Column(name = "is_leader")
  private Integer isLeader;*/

    @Column(name = "dept_id")
    private Integer deptId;

    @Column(name = "dept_name")
    private String deptName;

  /*@Column(name = "dept_path")
  private String deptPath;*/

    @Column(name = "create_time", insertable = false, updatable = false)
    private Date createTime;

    @Column(name = "modify_time", insertable = false, updatable = false)
    private Date modifyTime;
}
