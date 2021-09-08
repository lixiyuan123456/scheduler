package com.naixue.nxdp.model.metadata;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.naixue.nxdp.model.HadoopBinding;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "hadoopBindings")
@Entity
@Table(name = "t_metadata_hive_db")
public class MetadataHiveDb implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    @Transient
    @ManyToMany(mappedBy = "metadataHiveDbs")
    private List<HadoopBinding> hadoopBindings;
}
