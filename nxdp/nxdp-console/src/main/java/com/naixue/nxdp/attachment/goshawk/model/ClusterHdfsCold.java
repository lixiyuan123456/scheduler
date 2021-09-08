package com.naixue.nxdp.attachment.goshawk.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name = "t_cluster_hdfs_cold")
public class ClusterHdfsCold implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "dir")
    private String dir;

    @Transient
    private String dirLike;

    @Column(name = "atime")
    private Date accessTime;

    @Column(name = "is_del")
    private Integer status;

    @Column(name = "error")
    private String error;

    @Transient
    private List<Integer> includeIds;

    @Transient
    private List<Integer> includeStatus;

    @Transient
    private List<Integer> excludeStatus;

    public static enum Status {
        UN_DELETE(0, "未删除"),
        DELETING(1, "删除中"),
        DELETE_SUCCESS(2, "删除成功"),
        DELETE_FAILURE(3, "删除失败");

        @Getter
        @Setter
        private Integer code;
        @Getter
        @Setter
        private String name;
        Status(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static Status get(final Integer code) {
            Status status = null;
            switch (code) {
                case 0:
                    status = Status.UN_DELETE;
                    break;
                case 1:
                    status = Status.DELETING;
                    break;
                case 2:
                    status = Status.DELETE_SUCCESS;
                    break;
                case 3:
                    status = Status.DELETE_FAILURE;
                    break;
                default:
                    break;
            }
            return status;
        }
    }
}
