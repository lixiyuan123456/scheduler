package com.naixue.nxdp.data.dao.zstream;

import com.naixue.nxdp.data.model.zstream.ZstreamJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZstreamJobDao
        extends JpaRepository<ZstreamJob, Integer>, JpaSpecificationExecutor<ZstreamJob> {

    List<ZstreamJob> findDistinctByCreatorOrReceiversContaining(
            final String creator, final String receiver);
}
