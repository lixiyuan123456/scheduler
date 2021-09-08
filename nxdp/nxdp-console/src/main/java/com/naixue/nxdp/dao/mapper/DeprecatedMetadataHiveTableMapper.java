package com.naixue.nxdp.dao.mapper;

import java.util.List;

import com.naixue.nxdp.model.metadata.DeprecatedMetadataHiveTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DeprecatedMetadataHiveTableMapper {

    @Select("select * from t_metadata_hive_table")
    List<DeprecatedMetadataHiveTable> findAll();
}
