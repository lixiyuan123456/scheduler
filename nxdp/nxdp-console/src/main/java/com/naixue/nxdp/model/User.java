package com.naixue.nxdp.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author: wangyu @Created by 2017/11/19
 */
@Data
@Entity
@Table(name = "t_user")
public class User {

    @Id
    @Column(name = "userid")
    private String id;

    @Column(name = "name")
    private String pyName;
    @Column(name = "realname")
    private String name;
    @Transient
    private String userName;
    @Transient
    private String trueName;
    @Column(name = "email")
    private String email;
    @Column(name = "department_id")
    private int deptId;
    @Column(name = "department_name")
    private String deptName;
    @Column(name = "status")
    private Integer status = 1;
    @Transient
    private Status enumStatus = Status.getStatus(this.status);

    /*private String mobile;*/

  /*@Column(name = "is_leader")
  private int isLeader;*/
    @Column(name = "permission_level")
    private Integer permissionLevel = PermissionLevel.COMMON.getCode();
    @Column(name = "create_time", insertable = false, updatable = false)
    private Date createTime;

  /*@Column(name = "department_path")
    private String departmentpath;
  */
    @Column(name = "modify_time", insertable = false, updatable = false)
    private Date modifyTime;
    @Transient
    @Enumerated(EnumType.STRING)
    private Flag flag = Flag.TRUE;
    @Transient
    private HadoopBinding hadoopBinding;
    @Transient
    private boolean agent;

    public void setPyName(String pyName) {
        this.pyName = pyName;
        this.userName = pyName;
    }

    public void setName(String name) {
        this.name = name;
        this.trueName = name;
    }

    public String getUserName() {
        return this.pyName;
    }

    /*@Transient private UserDepartment department;*/

    public String getTrueName() {
        return this.name;
    }

    public static enum Flag {
        TRUE,
        FALSE;
    }

    @AllArgsConstructor
    public static enum Status {
        ON(1, "正常"),

        OFF(0, "离职");

        @Getter
        @Setter
        private int code;
        @Getter
        @Setter
        private String name;

        public static Status getStatus(final int code) {
            for (Status status : Status.values()) {
                if (status.getCode() == code) {
                    return status;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return this.name() + "[" + getCode() + "," + getName() + "]";
        }
    }

    @AllArgsConstructor
    public static enum PermissionLevel {
        COMMON(0, "普通"),
        ADMIN(1, "管理员");

        @Getter
        @Setter
        private int code;

        @Getter
        @Setter
        private String name;

        public static PermissionLevel getPermissionLevel(final int code) {
            for (PermissionLevel permissionLevel : PermissionLevel.values()) {
                if (permissionLevel.getCode() == code) {
                    return permissionLevel;
                }
            }
            return null;
        }
    }
}
