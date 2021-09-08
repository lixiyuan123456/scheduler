package com.naixue.nxdp.data.model.zstream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@ToString(exclude = {"job"})
@Getter
@Setter
@Entity
@Table(name = "t_zstream_job_config")
public class ZstreamJobConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "`sql`")
    private String sql = "";

    @Column(name = "settings")
    private String settings = "";

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private ZstreamJob job;
}
