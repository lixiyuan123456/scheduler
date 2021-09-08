package com.naixue.nxdp.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.model.ServerConfig;
import com.naixue.nxdp.model.metadata.MetadataDbTable;
import com.naixue.nxdp.service.DpService;
import com.naixue.nxdp.service.MetadataLabelService;
import com.naixue.nxdp.service.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.naixue.nxdp.dao.ServerConfigRepository;

@Controller
@RequestMapping("/metadata")
public class MetadataController extends BaseController {

    @Autowired
    MetadataLabelService metadataLebelService;
    @Autowired
    private ServerConfigRepository serverConfigRepository;
    @Autowired
    private MetadataService metadataService;
    @Autowired
    private DpService dpService;

    @RequestMapping("/server-list")
    public String serverList() {
        return "metadata/server-list";
    }

    @RequestMapping("/metadata-import")
    public String metadataImport() {
        return "metadata/metadata-import";
    }

    @ResponseBody
    @RequestMapping("/server/api/list-tree")
    public Object serversTreeList() {
        return metadataService.mapping();
    }

    @ResponseBody
    @RequestMapping("/server/api/add")
    public Object configServer(HttpServletRequest request, MetadataService.ServerConfigDTO server) {
        try {
            dpService.saveServerConfig(getCurrentUser(request), server.getServer());
        } catch (Exception e) {
            return success("failure", "msg", e.getMessage());
        }
        return success();
    }

    @ResponseBody
    @RequestMapping("/server/api/list-servers")
    public Object listServers() {
        List<MetadataService.ServerConfigDTO> servers = metadataService.listServers();
        return success("success", "servers", servers);
    }

    @ResponseBody
    @RequestMapping("/server/api/list-server-configs")
    public Object listServerConfigs() {
        List<ServerConfig> servers = serverConfigRepository.findAll();
        return success("success", "servers", servers);
    }

    @ResponseBody
    @RequestMapping("/server/reload")
    public Object reloadDbs() {
        return success();
    }

    @ResponseBody
    @RequestMapping("/db/api/list-dbs")
    public Object listDbs(Integer serverId) {
        return success("success", "databases", new ArrayList<>());
    }

    @ResponseBody
    @RequestMapping("/metadata-import/list-dbs")
    public Object listMetadataDbs(Integer serverId) {
        Object object = metadataService.listMetadataDbs(serverId);
        return success("success", "dbs", object);
    }

    @ResponseBody
    @RequestMapping("/table/api/search-tables")
    public Object searchTables(Integer dbId, String tableName) {
        return success("success", "tables", new ArrayList<>());
    }

    @ResponseBody
    @RequestMapping("/metadata-import/list-db-tables")
    public Object listMetadataDbTables(Integer serverId, String dbName, String tableKw) {
        List<MetadataDbTable> list = metadataService.listMetadataDbTables(serverId, dbName, tableKw);
        return success("data", list);
    }

    @ResponseBody
    @RequestMapping("/metadata-import/import-metadata.do")
    public Object importMetadata(
            Integer serverId, String dbName, @RequestParam(value = "tableNames[]") String[] tableNames) {
        metadataService.importMetadata(serverId, dbName, tableNames);
        return success();
    }
}
