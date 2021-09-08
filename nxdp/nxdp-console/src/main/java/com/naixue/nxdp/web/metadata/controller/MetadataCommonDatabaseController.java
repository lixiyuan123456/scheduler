package com.naixue.nxdp.web.metadata.controller;

import java.util.List;

import org.assertj.core.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.naixue.zzdp.data.model.metadata.MetadataCommonDatabase;
import com.naixue.zzdp.platform.service.metadata.MetadataCommonDatabaseService;
import com.naixue.nxdp.web.BaseController;

@RestController
@RequestMapping("/metadata/common/database")
public class MetadataCommonDatabaseController extends BaseController {

    @Autowired
    private MetadataCommonDatabaseService metadataCommonDatabaseService;

    @RequestMapping("/list.do")
    public Object listMetadataCommonServers(DataTableRequest<MetadataCommonDatabase> dataTable) {
        if (dataTable.getStart() == 0 && dataTable.getLength() == 0) {
            List<MetadataCommonDatabase> list =
                    metadataCommonDatabaseService.listMetadataCommonDatabases();
            return success(list);
        }
        MetadataCommonDatabase condition = new MetadataCommonDatabase();
        if (!Strings.isNullOrEmpty(dataTable.getCondition())) {
            condition = JSON.parseObject(dataTable.getCondition(), MetadataCommonDatabase.class);
        }
        condition.setSearch(
                dataTable.getSearch() != null
                        ? dataTable.getSearch().get(DataTableRequest.Search.value.toString())
                        : null);
        Page<MetadataCommonDatabase> page =
                metadataCommonDatabaseService.listMetadataCommonDatabases(
                        condition,
                        dataTable.getStart() / dataTable.getLength(),
                        dataTable.getLength(),
                        new Sort.Order(Sort.Direction.DESC, "id"));
        return new DataTableResponse<MetadataCommonDatabase>(
                dataTable.getStart(),
                dataTable.getLength(),
                dataTable.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    @RequestMapping("/sync.do")
    public Object syncUpdateDatabasesByServerId(Integer serverId) {
        metadataCommonDatabaseService.syncUpdateDatabasesByServerId(serverId);
        return success();
    }
}
