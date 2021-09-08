package com.naixue.nxdp.data.model.metadata;

import com.naixue.nxdp.data.BaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "t_metadata_common_database")
public class MetadataCommonDatabase extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name = "";

    @Column(name = "server_id")
    private Integer serverId = 0;

    @Column(name = "type")
    private Integer type = 0;

    @Transient
    private MetadataType metadataType;
    @Column(name = "status")
    private Integer status = 1; // 默认有效
    @Transient
    private String search;

    public MetadataType getMetadataType() {
        return MetadataType.parseEnum(getType());
    }

  /*@Column(name = "create_time", insertable = false, updatable = false)
  private Date createTime;

  @Column(name = "modify_time", insertable = false, updatable = false)
  private Date modifyTime;*/

    public void setMetadataType(MetadataType metadataType) {
        this.metadataType = metadataType;
        this.type = metadataType.getCode();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }

    @Override
    public boolean equals(Object target) {
        if (getName() != null && target instanceof MetadataCommonDatabase) {
            MetadataCommonDatabase object = (MetadataCommonDatabase) target;
            return getName().equals(object.getName());
        }
        return false;
    }
}
