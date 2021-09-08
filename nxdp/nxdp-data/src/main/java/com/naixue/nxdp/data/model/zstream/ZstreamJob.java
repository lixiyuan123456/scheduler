package com.naixue.nxdp.data.model.zstream;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@ToString(exclude = {"jobConfig", "udxs", "nameLike", "dashboardUrl"})
@Getter
@Setter
@Entity
@Table(name = "t_zstream_job")
public class ZstreamJob implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "name")
    private String name = "";
    @Column(name = "status")
    private Integer status = Status.NEW.getCode();
    @Column(name = "last_job_version")
    private String lastJobVersion = "";

  /*@Column(name = "type")
  private Integer type;*/
    @Column(name = "last_application_id")
    private String lastApplicationId = "";
    @Column(name = "last_application_job_id")
    private String lastApplicationJobId = "";
    @Column(name = "creator")
    private String creator = "";
    @Column(name = "proxy_code")
    private String proxyCode = "";
    @Column(name = "description")
    private String description = "";
    @Column(name = "create_time", insertable = false, updatable = false)
    private Date createTime;
    @Column(name = "modify_time", insertable = false, updatable = false)
    private Date modifyTime;
    @Column(name = "receivers")
    private String receivers = "";
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "job")
    // @PrimaryKeyJoinColumn
    private ZstreamJobConfig jobConfig;
    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.REFRESH})
    @JoinTable(
            name = "t_zstream_job_udxs",
            joinColumns = {@JoinColumn(name = "job_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "udx_id", referencedColumnName = "id")})
    private List<ZstreamUdx> udxs;
    @OneToMany(
            mappedBy = "job",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL})
    private List<ZstreamJobLog> logs;
    @Transient
    private String nameLike;
    @Transient
    private String dashboardUrl;
    @Transient
    private String yarnLogUrl;

    public ZstreamJob() {
        if (this.jobConfig == null) {
            this.jobConfig = new ZstreamJobConfig();
            this.jobConfig.setJob(this);
        }
    }

    public static enum Status {
        NEW(0, "新建"),
        RUNNING(1, "正在运行"),
        BOOT_FAILURE(2, "启动失败"),
        RUNTIME_FAILURE(3, "运行时失败"),
        LAUNCHING(9, "启动中"),
        KILLED(-1, "KILLED"),
        DELETED(-9, "已删除");

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
                case -1:
                    status = Status.KILLED;
                    break;
                case 0:
                    status = Status.NEW;
                    break;
                case 1:
                    status = Status.RUNNING;
                    break;
                case 2:
                    status = Status.BOOT_FAILURE;
                    break;
                case 3:
                    status = Status.RUNTIME_FAILURE;
                    break;
                default:
                    break;
            }
            return status;
        }
    }
}
