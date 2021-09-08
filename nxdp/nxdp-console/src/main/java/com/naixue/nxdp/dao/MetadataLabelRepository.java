package com.naixue.nxdp.dao;

import java.util.List;

import com.naixue.nxdp.model.metadata.MetadataLabel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetadataLabelRepository extends JpaRepository<MetadataLabel, Integer> {

    List<MetadataLabel> findByParentIdAndLevelAndStatus(
            Integer parentId, Integer level, Integer status);
}
