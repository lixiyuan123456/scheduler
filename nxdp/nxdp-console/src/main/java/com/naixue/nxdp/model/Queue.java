package com.naixue.nxdp.model;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @author: wangyu
 * @Created by 2018/1/30
 **/
@Entity
@Table(name = "t_dict_queue")
public class Queue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "dept_id")
    private Integer deptId;

    @Column(name = "queue_name", length = 50)
    private String name;

    @Column(name = "updated")
    private Timestamp updated;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getUpdated() {
        return updated;
    }

    public void setUpdated(Timestamp updated) {
        this.updated = updated;
    }
}
