package com.naixue.nxdp.data.model.metadata;

import com.naixue.nxdp.data.BaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "t_metadata_common_table")
public class MetadataCommonTable extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name = "";

    @Transient
    private String fullName = "";

    @Column(name = "alias")
    private String alias = "";

    @Column(name = "type")
    private Integer type = 0;

    @Transient
    private MetadataType metadataType;
    @Column(name = "server_id")
    private Integer serverId = 0;
    @Transient
    private String serverName = "";
    @Column(name = "database_id")
    private Integer databaseId = 0;
    @Transient
    private String databaseName = "";
    @Column(name = "columns")
    private String columns = "";
    @Column(name = "status")
    private Integer status = 1; // 默认有效
    @Column(name = "mirrored")
    private Integer mirrored = 0; // 是否已在HIVE库创建镜像（用于mysql分库分表）
    @Transient
    private MetadataCommonServer server;
    @Transient
    private MetadataCommonDatabase database;
    @Transient
    private MirroredStatus mirroredStatus = MirroredStatus.NOT_MIRRORED;
    @Transient
    private String search;

    public MetadataType getMetadataType() {
        return MetadataType.parseEnum(getType());
    }

    public void setMetadataType(MetadataType metadataType) {
        this.metadataType = metadataType;
        this.type = metadataType.getCode();
    }

    public MirroredStatus getMirroredStatus() {
        if (this.mirroredStatus == null) {
            this.mirroredStatus =
                    this.mirrored == 0 ? MirroredStatus.NOT_MIRRORED : MirroredStatus.MIRRORED;
        }
        return this.mirroredStatus;
    }

    public void setMirroredStatus(MirroredStatus mirroredStatus) {
        this.mirroredStatus = mirroredStatus;
        this.mirrored = mirroredStatus == MirroredStatus.NOT_MIRRORED ? 0 : 1;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }

    @Override
    public boolean equals(Object target) {
        if (getName() != null && target instanceof MetadataCommonTable) {
            MetadataCommonTable object = (MetadataCommonTable) target;
            return getName().equals(object.getName());
        }
        return false;
    }

    public static enum MirroredStatus {
        NOT_MIRRORED,
        MIRRORED;
    }
}
