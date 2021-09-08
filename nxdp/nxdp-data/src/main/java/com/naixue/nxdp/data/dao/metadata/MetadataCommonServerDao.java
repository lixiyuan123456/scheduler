package com.naixue.nxdp.data.dao.metadata;

import com.naixue.nxdp.data.model.metadata.MetadataCommonServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetadataCommonServerDao
        extends JpaRepository<MetadataCommonServer, Integer>,
        JpaSpecificationExecutor<MetadataCommonServer> {

    List<MetadataCommonServer> findByStatus(final Integer status);

    List<MetadataCommonServer> findByType(final Integer type);

    MetadataCommonServer findTop1ByStatusAndName(final Integer status, final String name);
}
