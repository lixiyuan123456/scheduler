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
@Table(name = "t_server_config")
public class ServerConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "host")
    private String host;

    @Column(name = "port")
    private String port;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "creator_id")
    private String creatorId;

    @Column(name = "server_type")
    private Integer serverType;

    @Column(name = "logic_type")
    private Integer logicType;

    @Column(name = "is_binlog_supported")
    private Integer isBinlogSupported;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "modify_time")
    private Date modifyTime;

    @Column(name = "last_modifier_id")
    private String lastModifierId;

    public static enum ServerType {

        MYSQL(1, "Mysql"),

        HIVE(2, "HIVE");

        private Integer id;
        private String name;
        private ServerType(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public static ServerType parseEnum(Integer id) {
            for (ServerType e : ServerType.values()) {
                if (e.getId().equals(id)) {
                    return e;
                }
            }
            return null;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static enum LogicType {

        DATA_SOURCE(1, "数据源"),

        DATA_WAREHOUSE(2, "数据仓库");

        private Integer id;
        private String name;

        private LogicType(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public static LogicType parseEnum(Integer id) {
            for (LogicType e : LogicType.values()) {
                if (e.getId().equals(id)) {
                    return e;
                }
            }
            return null;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static enum ServerTypeGroup {
        MYSQL_DATA_SOURCE(1, "Mysql数据源"),

        MYSQL_DATA_WAREHOUSE(2, "Mysql数据仓库"),

        /*HIVE_DATA_SOURCE(3,"HIVE数据源"),*/

        HIVE_DATA_WAREHOUSE(4, "HIVE数据仓库");

        private Integer id;
        private String name;

        private ServerTypeGroup(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public static ServerTypeGroup parseEnum(Integer id) {
            for (ServerTypeGroup e : ServerTypeGroup.values()) {
                if (e.getId().equals(id)) {
                    return e;
                }
            }
            return null;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

}
