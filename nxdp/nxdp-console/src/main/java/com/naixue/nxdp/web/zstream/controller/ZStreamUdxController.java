package com.naixue.nxdp.web.zstream.controller;

import java.text.MessageFormat;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.config.CFG;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.naixue.zzdp.data.model.zstream.ZstreamUdx;
import com.naixue.zzdp.platform.service.zstream.ZstreamUdxService;
import com.naixue.nxdp.web.BaseController;

@RestController
@RequestMapping("/zstream/udx")
public class ZStreamUdxController extends BaseController {

    private static final String UDX_ROOT_PATH_PATTERN = "/zstream/udx/{0}/";

    @Autowired
    private ZstreamUdxService zstreamUdxService;

    @RequestMapping("/list.do")
    public Object list(HttpServletRequest request, DataTableRequest dataTable) {
        User currentUser = getCurrentUser(request);
        ZstreamUdx condition = JSON.parseObject(dataTable.getCondition(), ZstreamUdx.class);
        if (StringUtils.isEmpty(condition.getCreator())) {
            condition.setCreator(currentUser.getPyName());
        }
        Page<ZstreamUdx> page =
                zstreamUdxService.list(
                        condition, dataTable.getStart() / dataTable.getLength(), dataTable.getLength());
        DataTableResponse<ZstreamUdx> datatable =
                new DataTableResponse<ZstreamUdx>(
                        dataTable.getStart(),
                        dataTable.getLength(),
                        dataTable.getDraw(),
                        page.getTotalElements(),
                        page.getTotalElements(),
                        page.getContent());
        return datatable;
    }

    @RequestMapping("/upload.do")
    public Object upload(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        User currentUser = getCurrentUser(request);
        String proxyCode = currentUser.getHadoopBinding().getProxyCode();
        String pathPrefix =
                MessageFormat.format(CFG.WEBHDFS_ROOT_PATH + UDX_ROOT_PATH_PATTERN, proxyCode);
        String url = String.format(CFG.WEBHDFS_UPLOAD_URL, pathPrefix + file.getOriginalFilename());
        FileUtils.uploadToHDFS(url, file);
        zstreamUdxService.coverPreviousUdx4SameName(proxyCode, file.getOriginalFilename());
        return success(pathPrefix + file.getOriginalFilename());
    }

    @RequestMapping("/create.do")
    public Object create(HttpServletRequest request, ZstreamUdx udx) {
        User currentUser = getCurrentUser(request);
        udx.setCreator(currentUser.getPyName());
        udx.setProxyCode(currentUser.getHadoopBinding().getProxyCode());
        ZstreamUdx newUdx = zstreamUdxService.createUdx(udx);
        return success(newUdx);
    }

    @RequestMapping("/delete.do")
    public Object delete(HttpServletRequest request, Integer id) {
        User currentUser = getCurrentUser(request);
        if (!Objects.isNull(currentUser.getHadoopBinding())) {
            ZstreamUdx udx = zstreamUdxService.findUdxById(id);
            if (!Objects.isNull(udx)
                    && udx.getProxyCode().equals(currentUser.getHadoopBinding().getProxyCode())) {
                zstreamUdxService.deleteUdx(udx);
            }
        }
        return success();
    }

    @RequestMapping("/find-by-id.do")
    public Object findById(Integer id) {
        ZstreamUdx udx = zstreamUdxService.findUdxById(id);
        return success(udx);
    }
}
