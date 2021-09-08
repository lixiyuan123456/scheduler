package com.naixue.nxdp.data.model.metadata;

import com.naixue.nxdp.data.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_metadata_common_server")
public class MetadataCommonServer extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name = "";

    @Column(name = "host")
    private String host = "";

    @Column(name = "port")
    private String port = "";

    @Column(name = "username")
    private String username = "";

    @Column(name = "password")
    private String password = "";

    @Column(name = "creator")
    private String creator = "";

    @Transient
    private String creatorName;

    @Column(name = "modifier")
    private String modifier = "";

    @Transient
    private String modifierName;

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
}
