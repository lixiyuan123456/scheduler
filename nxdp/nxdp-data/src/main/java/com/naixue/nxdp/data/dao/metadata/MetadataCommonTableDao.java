package com.naixue.nxdp.data.dao.metadata;

import com.naixue.nxdp.data.model.metadata.MetadataCommonTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetadataCommonTableDao
        extends JpaRepository<MetadataCommonTable, Integer>,
        JpaSpecificationExecutor<MetadataCommonTable> {

    List<MetadataCommonTable> findByIdIn(final List<Integer> ids);

    List<MetadataCommonTable> findByStatus(final Integer status);

    List<MetadataCommonTable> findByStatusAndDatabaseId(
            final Integer status, final Integer databaseId);

    List<MetadataCommonTable> findByDatabaseId(final Integer databaseId);

    List<MetadataCommonTable> findTop10ByStatusAndNameLike(final Integer status, final String name);

    List<MetadataCommonTable> findByServerIdAndDatabaseId(
            final Integer serverId, final Integer databaseId);

    List<MetadataCommonTable> findByTypeAndDatabaseId(final Integer type, final Integer databaseId);
}
