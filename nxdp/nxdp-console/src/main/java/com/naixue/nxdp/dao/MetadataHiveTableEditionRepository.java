package com.naixue.nxdp.dao;

import java.util.List;

import com.naixue.nxdp.model.metadata.MetadataHiveTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetadataHiveTableEditionRepository
        extends JpaRepository<MetadataHiveTable.TableEdition, Integer> {

    List<MetadataHiveTable.TableEdition> findByTableIdAndStatusInOrderByIdDesc(
            Integer tableId, List<Integer> statusList);

    List<MetadataHiveTable.TableEdition> findByTableIdAndStatus(Integer tableId, Integer status);
}
