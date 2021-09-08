package com.naixue.nxdp.model.metadata;

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
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
@Entity
@Table(name = "t_metadata_label")
public class MetadataLabel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull(message = "标签名不允许为空")
    @Column(name = "name")
    private String name;

    @Column(name = "parent_id")
    private Integer parentId;

    @Column(name = "level")
    private Integer level;

    @Column(name = "creator_id")
    private String creatorId;

    @Column(name = "modifier_id")
    private String modifierId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "create_time", insertable = false, updatable = false)
    private Date createTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "modify_time", insertable = false, updatable = false)
    private Date modifyTime;

    @Column(name = "status")
    private Integer status;

  /*@NotFound(action = NotFoundAction.IGNORE)
  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "parent_id")
  private MetadataLebel parent;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
  private List<MetadataLebel> children;*/

    @Transient
    private List<MetadataLabel> children;
}
