package com.naixue.nxdp.data.model.zstream;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@ToString(exclude = {"nameLike"})
@Getter
@Setter
@Entity
@Table(name = "t_zstream_udx")
public class ZstreamUdx implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name = "";

    @Transient
    private String alias;

    @Column(name = "type")
    private Integer type;

    @Column(name = "url")
    private String url = "";

    @Column(name = "creator")
    private String creator = "";

    @Column(name = "proxy_code")
    private String proxyCode = "";

    @Column(name = "function_class")
    private String functionClass = "";

    @Column(name = "function_alias")
    private String functionAlias = "";

    @Column(name = "description")
    private String description = "";

    @Column(name = "create_time", insertable = false, updatable = false)
    private Date createTime;

    @Column(name = "modify_time", insertable = false, updatable = false)
    private Date modifyTime;

    @Column(name = "status")
    private Integer status = Status.ALIVE.getCode();

    @Transient
    private String nameLike;

    public static enum Status {
        DEAD(0, "删除"),
        ALIVE(1, "正常");

        @Getter
        @Setter
        private Integer code;
        @Getter
        @Setter
        private String name;
        Status(final Integer code, final String name) {
            this.code = code;
            this.name = name;
        }

        public static Status getStatus(final Integer code) {
            Status status = null;
            switch (code) {
                case 0:
                    status = Status.DEAD;
                    break;
                case 1:
                    status = Status.ALIVE;
                    break;
                default:
                    break;
            }
            return status;
        }
    }
}
