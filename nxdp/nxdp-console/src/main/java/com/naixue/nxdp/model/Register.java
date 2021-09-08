package com.naixue.nxdp.model;

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
@Table(name = "t_register")
public class Register implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_pyname")
    private String userPyname;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "dept_id")
    private Integer deptId;

    @Column(name = "dept_name")
    private String deptName;

    @Column(name = "description")
    private String description;

    @Column(name = "create_time")
    private Date createTime = new Date();

    @Column(name = "modify_time")
    private Date modifyTime = new Date();

    @Column(name = "status")
    private Integer status = Status.WAIT.getCode();

    @Column(name = "operator_id")
    private String operatorId;

    @Column(name = "operator_name")
    private String operatorName;

    public static enum Status {
        WAIT(0, "等待"),

        RATIFY(1, "批准"),

        REJECT(2, "拒绝"),

        END(9, "结束");

        private Integer code;
        private String name;

        Status(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static Status getEnum(Integer code) {
            for (Status e : Status.values()) {
                if (e.getCode().equals(code)) {
                    return e;
                }
            }
            throw new NullPointerException("没有对应的枚举值");
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
