package com.naixue.nxdp.dao.mapper;

import com.naixue.nxdp.model.metadata.MetadataHiveTable;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MetadataHiveTableMapper {

    @Insert(
            "insert into t_metadata_hive_table_edition(`id`,`table_id`,`server_id`,`db_id`,`name`,`full_name`,`partition`,`description`,`label_id`,`child_label_id`,`group`,`level`,`creator_id`,`modifier_id`,`json`,`status`,`version`,`type`,`location`,`update_type`,`storage_format`,`char_separator`,`modify_location`) values("
                    + "${id},"
                    + "${tableId},"
                    + "${serverId},"
                    + "${dbId},"
                    + "'${name}',"
                    + "'${fullName}',"
                    + "${partition},"
                    + "'${description}',"
                    + "${labelId},"
                    + "${childLabelId},"
                    + "${group},"
                    + "${level},"
                    + "'${creatorId}',"
                    + "'${modifierId}',"
                    + "'${json}',"
                    + "${status},"
                    + "'${version}',"
                    + "${type},"
                    + "'${location}',"
                    + "${updateType},"
                    + "${storageFormat},"
                    + "'${charSeparator}',"
                    + "${modifyLocation})")
    void add(MetadataHiveTable.TableEdition table);
}
