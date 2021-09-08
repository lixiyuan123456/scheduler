package com.naixue.nxdp.data.dao.metadata;

import com.naixue.nxdp.data.model.metadata.MetadataCommonDatabase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetadataCommonDatabaseDao
        extends JpaRepository<MetadataCommonDatabase, Integer>,
        JpaSpecificationExecutor<MetadataCommonDatabase> {

    List<MetadataCommonDatabase> findByStatus(final Integer status);

    List<MetadataCommonDatabase> findByServerId(final Integer serverId);

    List<MetadataCommonDatabase> findByStatusAndServerId(
            final Integer status, final Integer serverId);

    List<MetadataCommonDatabase> findByTypeAndServerId(final Integer type, final Integer serverId);
}
