package com.naixue.nxdp.dao;

import com.naixue.nxdp.model.metadata.MetadataHiveDbPermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetadataHiveDbPermissionRepository
        extends JpaRepository<MetadataHiveDbPermission, Integer> {
}
