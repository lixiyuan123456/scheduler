package com.naixue.nxdp.web;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.config.CFG;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.model.metadata.MetadataHiveDb;
import com.naixue.nxdp.model.metadata.MetadataHiveTable;
import com.naixue.nxdp.model.metadata.MetadataLabel;
import com.naixue.nxdp.util.SqlParserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.naixue.zzdp.common.util.HiveSQLParser;
import com.naixue.nxdp.service.HadoopBindingService;
import com.naixue.nxdp.service.MetadataHiveDbService;
import com.naixue.nxdp.service.MetadataHiveTableService;
import com.naixue.nxdp.service.MetadataLabelService;
import com.naixue.nxdp.service.UserService;

@Controller
@RequestMapping("/metadata/hive-table")
public class MetadataHiveTableController extends BaseController {

    @Autowired
    MetadataHiveTableService metadataHiveTableService;

    @Autowired
    UserService userService;
    @Autowired
    MetadataLabelService metadataLebelService;
    @Autowired
    HadoopBindingService hadoopBindingService;
    @Autowired
    private MetadataHiveDbService metadataHiveDbService;

    @RequestMapping("/list")
    public Object list(HttpServletRequest request, Model model) {
        model.addAttribute("users", userService.listUsers());
        model.addAttribute("currentUser", getCurrentUser(request));
        model.addAttribute("tableStatusList", MetadataHiveTable.TableStatus.values());
        return "metadata/hive-table-list";
    }

    @RequestMapping("/view")
    public Object view(Integer tableId, Model model) {
        model.addAttribute("tableId", tableId);
        return "metadata/hive-table-view";
    }

    @RequestMapping("/listTableEditions.do")
    @ResponseBody
    public Object listTableEditions(Integer tableId) {
        MetadataHiveTable table = metadataHiveTableService.getTableByTableId(tableId);
        return success(table);
    }

    @RequestMapping("/save.do")
    @ResponseBody
    public Object save(HttpServletRequest request, MetadataHiveTable.TableEdition tableEdition) {
        User currentUser = getCurrentUser(request);
        MetadataHiveTable table;
        if (tableEdition.getTableId() == null) {
            table = new MetadataHiveTable();
            table.setCreatorId(currentUser.getId());
        } else {
            table = metadataHiveTableService.getTableByTableId(tableEdition.getTableId());
            Assert.notNull(table, "表不存在，ID=" + tableEdition.getTableId());
            if (MetadataHiveTable.TableStatus.FORMAL.getCode().equals(table.getStatus())) {
                throw new RuntimeException("表在库中已经创建成功了，不允许再次修改");
            }
        }
        table.setModifierId(currentUser.getId());
        tableEdition.setModifierId(currentUser.getId());
        table.setLastEdition(tableEdition);
        MetadataHiveTable newTable = metadataHiveTableService.save(table);
        return success("data", newTable.getLastEdition());
    }

    @RequestMapping("/list.do")
    @ResponseBody
    public Object listMetadataHiveTables(DataTableRequest dataTable) {
        Page<MetadataHiveTable> page =
                metadataHiveTableService.list(
                        JSON.parseObject(dataTable.getCondition(), MetadataHiveTable.class),
                        dataTable.getStart() / dataTable.getLength(),
                        dataTable.getLength());
        return new DataTableResponse<MetadataHiveTable>(
                dataTable.getStart(),
                dataTable.getLength(),
                dataTable.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    @RequestMapping("/delete.do")
    @ResponseBody
    public Object delete(HttpServletRequest request, Integer id) {
        User currentUser = getCurrentUser(request);
        metadataHiveTableService.delete(currentUser, id);
        return success();
    }

    @RequestMapping("/edit")
    public Object edit(HttpServletRequest request, Integer id, Model model) {
        MetadataHiveTable.TableEdition tableEdition =
                metadataHiveTableService.getTableEditionByEditionId(id);
        model.addAttribute("table", tableEdition);
        User currentUser = getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("dbs", metadataHiveDbService.getAuthorizedMetadataHiveDbs(currentUser));
        model.addAttribute("tablePartitions", MetadataHiveTable.TablePartition.values());
        List<MetadataLabel> tableLebels = metadataLebelService.findAllFirstLevel();
        model.addAttribute("tableLebels", tableLebels);
        if (tableEdition != null && tableEdition.getLabelId() != null) {
            List<MetadataLabel> childTableLebels =
                    metadataLebelService.findAll(tableEdition.getLabelId(), 2);
            model.addAttribute("childTableLebels", childTableLebels);
        }
        model.addAttribute("tableGroups", MetadataHiveTable.TableGroup.values());
        model.addAttribute("tableLevels", MetadataHiveTable.TableLevel.values());
        // 更新方式
        model.addAttribute("tableUpdateTypes", MetadataHiveTable.TableUpdateType.values());
        // 存储格式
        model.addAttribute("tableStorageFormats", MetadataHiveTable.TableStorageFormat.values());
        return "metadata/hive-table";
    }

    @RequestMapping("/create")
    public Object createHiveTable(HttpServletRequest request, Model model) {
        User currentUser = getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("dbs", metadataHiveDbService.getAuthorizedMetadataHiveDbs(currentUser));
        model.addAttribute("tablePartitions", MetadataHiveTable.TablePartition.values());
        List<MetadataLabel> tableLebels = metadataLebelService.findAllFirstLevel();
        model.addAttribute("tableLebels", tableLebels);
        if (!CollectionUtils.isEmpty(tableLebels)) {
            MetadataLabel metadataLebel = tableLebels.get(0);
            List<MetadataLabel> childTableLebels = metadataLebelService.findAll(metadataLebel.getId(), 2);
            model.addAttribute("childTableLebels", childTableLebels);
        }
        model.addAttribute("tableGroups", MetadataHiveTable.TableGroup.values());
        model.addAttribute("tableLevels", MetadataHiveTable.TableLevel.values());
        // 更新方式
        model.addAttribute("tableUpdateTypes", MetadataHiveTable.TableUpdateType.values());
        // 存储格式
        model.addAttribute("tableStorageFormats", MetadataHiveTable.TableStorageFormat.values());
        // location
        String hadoopUserGroupName =
                hadoopBindingService.getHadoopUserGroupName(currentUser.getDeptId());
        String locationPrefix = CFG.HDFS_CLUSTER_URL_PREFIX + "/" + hadoopUserGroupName + "/warehouse/";
        model.addAttribute("locationPrefix", locationPrefix);
        return "metadata/hive-table";
    }

    @RequestMapping("/build-hive-table-sql.do")
    @ResponseBody
    public Object buildHiveTableSql(HttpServletRequest request, Integer id) {
        User currentUser = getCurrentUser(request);
        MetadataHiveTable.TableEdition table = metadataHiveTableService.getTableEditionByEditionId(id);
        if (table == null) {
            throw new RuntimeException("表设计历史版本不存在，id=" + id);
        }
        if (User.PermissionLevel.ADMIN.getCode() != currentUser.getPermissionLevel()
                && !table.getCreatorId().equals(currentUser.getId())) {
            throw new RuntimeException("无权操作");
        }
        String sql = metadataHiveTableService.parseHiveTableSql(id);
        return success("data", sql);
    }

    @RequestMapping("/execute-hive-table-sql.do")
    @ResponseBody
    public Object executeHiveTableSql(HttpServletRequest request, Integer id, String sql) {
        User currentUser = getCurrentUser(request);
        MetadataHiveTable.TableEdition table = metadataHiveTableService.getTableEditionByEditionId(id);
        if (table == null) {
            throw new RuntimeException("表设计历史版本不存在，id=" + id);
        }
        if (User.PermissionLevel.ADMIN.getCode() != currentUser.getPermissionLevel()
                && !table.getCreatorId().equals(currentUser.getId())) {
            throw new RuntimeException("无权操作");
        }
        metadataHiveTableService.executeHiveTableSql(currentUser, table, sql);
        return success();
    }

    @ResponseBody
    @RequestMapping("/parse-columns-from-ddl.do")
    public Object parseDDL2Columns(String dialect, String ddl) {
        if (!StringUtils.isEmpty(ddl)) {
            List<SqlParserUtils.Column> columns = SqlParserUtils.parseColumnsFromCreateTableSQL(ddl, dialect);
            return success("data", columns);
        }
        return success();
    }

    @ResponseBody
    @RequestMapping("/parseSQL.do")
    public Object parseSQL(String sql) throws Exception {
        Map<String, Object> table = HiveSQLParser.parseSQL(sql);
        return success(table);
    }

    @ResponseBody
    @RequestMapping("/list-dbs.do")
    public Object listDbs() {
        List<MetadataHiveDb> dbs = metadataHiveDbService.listDbs();
        return success(dbs);
    }

    @ResponseBody
    @RequestMapping("/deleteTableEditionByTableEditionId.do")
    public Object deleteTableEditionByTableEditionId(Integer tableEditionId) {
        metadataHiveTableService.deleteTableEditionByTableEditionId(tableEditionId);
        return success();
    }
}
