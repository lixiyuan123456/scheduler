package com.naixue.nxdp.dao;

import java.util.List;

import com.naixue.nxdp.model.metadata.MetadataDbTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetadataDbTableRepository extends JpaRepository<MetadataDbTable, Integer> {

    List<MetadataDbTable> findByServerIdAndDbName(Integer serverId, String dbName);
}
