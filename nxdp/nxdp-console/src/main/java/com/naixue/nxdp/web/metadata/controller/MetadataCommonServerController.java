package com.naixue.nxdp.web.metadata.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.model.User;
import com.naixue.nxdp.service.UserService;
import org.assertj.core.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.naixue.zzdp.data.model.metadata.MetadataCommonServer;
import com.naixue.zzdp.platform.service.metadata.MetadataCommonServerService;
import com.naixue.nxdp.web.BaseController;

@RestController
@RequestMapping("/metadata/common/server")
public class MetadataCommonServerController extends BaseController {

    @Autowired
    private MetadataCommonServerService metadataCommonServerService;

    @Autowired
    private UserService userService;

    @RequestMapping("/list.do")
    public Object listMetadataCommonServers(DataTableRequest<MetadataCommonServer> dataTable) {
        MetadataCommonServer condition = new MetadataCommonServer();
        if (!Strings.isNullOrEmpty(dataTable.getCondition())) {
            condition = JSON.parseObject(dataTable.getCondition(), MetadataCommonServer.class);
        }
        condition.setSearch(
                dataTable.getSearch() != null
                        ? dataTable.getSearch().get(DataTableRequest.Search.value.toString())
                        : null);
        Page<MetadataCommonServer> page =
                metadataCommonServerService.listMetadataCommonServers(
                        condition,
                        dataTable.getStart() / dataTable.getLength(),
                        dataTable.getLength(),
                        new Sort.Order(Sort.Direction.DESC, "id"));
        if (!CollectionUtils.isEmpty(page.getContent())) {
            List<MetadataCommonServer> list = page.getContent();
            for (MetadataCommonServer server : list) {
                User creator = userService.getUserByUserId(server.getCreator());
                server.setCreatorName(creator.getName());
                User modifier = userService.getUserByUserId(server.getModifier());
                server.setModifierName(modifier.getName());
            }
        }
        return new DataTableResponse<MetadataCommonServer>(
                dataTable.getStart(),
                dataTable.getLength(),
                dataTable.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    @RequestMapping("/find.do")
    public Object getMetadataCommonServerById(Integer serverId) {
        MetadataCommonServer obj =
                metadataCommonServerService.getMetadataCommonServerByServerId(serverId);
        return success(obj);
    }

    @RequestMapping("/save.do")
    public Object saveMetadataCommonServer(HttpServletRequest request, MetadataCommonServer server) {
        User currentUser = getCurrentUser(request);
        if (server.getId() == null) {
            server.setCreator(currentUser.getId());
        }
        server.setModifier(currentUser.getId());
        MetadataCommonServer obj = metadataCommonServerService.saveMetadataCommonServer(server);
        return success(obj);
    }
}
