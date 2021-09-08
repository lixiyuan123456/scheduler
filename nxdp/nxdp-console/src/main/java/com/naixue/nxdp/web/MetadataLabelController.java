package com.naixue.nxdp.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.model.User;
import com.naixue.nxdp.model.metadata.MetadataLabel;
import com.naixue.nxdp.service.MetadataLabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Validated
@Controller
@RequestMapping("/metadata/label")
public class MetadataLabelController extends BaseController {

    @Autowired
    MetadataLabelService metadataLabelService;

    @ResponseBody
    @RequestMapping("/list")
    public Object listByLevel(Integer parentId, Integer level) {
        Assert.notNull(parentId, "请求参数parentId不允许为空");
        Assert.notNull(level, "请求参数level不允许为空");
        return metadataLabelService.findAll(parentId, level);
    }

    @ResponseBody
    @RequestMapping(value = "/save-or-update")
    public Object saveOrUpdate(HttpServletRequest request, MetadataLabel lebel) {
        User currentUser = getCurrentUser(request);
        MetadataLabel metadataLebel = metadataLabelService.save(currentUser, lebel);
        return success("data", metadataLebel);
    }

    @ResponseBody
    @RequestMapping(value = "/delete")
    public Object delete(HttpServletRequest request, Integer id) {
        User currentUser = getCurrentUser(request);
        metadataLabelService.delete(currentUser, id);
        return success();
    }

    @ResponseBody
    @RequestMapping(value = "/children")
    public Object getChildren(Integer parentId) {
        List<MetadataLabel> list = metadataLabelService.findAll(parentId, 2);
        return success("data", list);
    }
}
