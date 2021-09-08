package com.naixue.nxdp.dao;

import com.naixue.nxdp.model.metadata.MetadataHiveTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MetadataHiveTableRepository
        extends JpaRepository<MetadataHiveTable, Integer>,
        JpaSpecificationExecutor<MetadataHiveTable> {
}
