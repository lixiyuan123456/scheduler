package com.naixue.nxdp.data;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
public class BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    @Column(name = "create_time", insertable = false, updatable = false)
    private Date createTime;

    @Getter
    @Setter
    @Column(name = "modify_time", insertable = false, updatable = false)
    private Date modifyTime;
}
