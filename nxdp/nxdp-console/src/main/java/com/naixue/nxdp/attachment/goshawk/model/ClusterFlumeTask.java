package com.naixue.nxdp.attachment.goshawk.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name = "t_cluster_flume_task")
public class ClusterFlumeTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "cluster")
    private Integer cluster;

    @Column(name = "topic")
    private String topic;

    @Column(name = "consumer")
    private String consumer;

    @Column(name = "create_time", insertable = false, updatable = false)
    private Date createTime;

    @Column(name = "modify_time", insertable = false, updatable = false)
    private Date modifyTime;

    public static enum Cluster {
        CLUSTER_ZZ(1, "转转"),
        CLUSTER_58(2, "58");

        @Getter
        @Setter
        private Integer code;
        @Getter
        @Setter
        private String name;

        Cluster(final Integer code, final String name) {
            this.code = code;
            this.name = name;
        }

        public static Cluster get(final Integer code) {
            Cluster cluster = null;
            switch (code) {
                case 1:
                    cluster = CLUSTER_ZZ;
                    break;
                case 2:
                    cluster = CLUSTER_58;
                    break;
                default:
                    break;
            }
            Assert.isNull(cluster, "Cluster[" + code + "] is not exist");
            return cluster;
        }

        @SuppressWarnings("unchecked")
        public static String json() {
            SerializeConfig config = new SerializeConfig();
            config.configEnumAsJavaBean(Cluster.class);
            return JSON.toJSONString(Cluster.values(), config);
        }
    }
}
