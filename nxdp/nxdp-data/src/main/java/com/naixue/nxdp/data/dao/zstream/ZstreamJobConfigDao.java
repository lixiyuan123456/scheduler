package com.naixue.nxdp.data.dao.zstream;

import com.naixue.nxdp.data.model.zstream.ZstreamJob;
import com.naixue.nxdp.data.model.zstream.ZstreamJobConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ZstreamJobConfigDao
        extends JpaRepository<ZstreamJob, Integer>, JpaSpecificationExecutor<ZstreamJobConfig> {
}
