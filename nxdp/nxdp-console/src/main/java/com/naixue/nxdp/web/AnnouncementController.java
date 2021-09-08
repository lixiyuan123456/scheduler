package com.naixue.nxdp.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.model.NoticeConfig;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: wangyu
 * @Created by 2018/1/29
 * 系统通知消息
 **/
@Controller
@Slf4j
@RequestMapping("/announcement/api")
public class AnnouncementController extends BaseController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 获取当前登录用户通知消息列表
     *
     * @return
     */
    @RequestMapping("/get-notice-message")
    @ResponseBody
    public Object getNoticeMessage(HttpServletRequest request) {
        User user = getCurrentUser(request);
        List<Map<String, Object>> messages = announcementService.getMessageList(user.getId());
        return success("success", "userMsg", messages);
    }

    /**
     * 读取通知消息明细
     *
     * @param request
     * @param msgId
     * @return
     */
    @RequestMapping("/view-notice-message")
    @ResponseBody
    public String viewNoticeMessage(HttpServletRequest request, Integer msgId) {
        Map<String, Object> resultMap = new HashMap<>();
        String status = "success";
        // 获取当前用户信息
        User user = getCurrentUser(request);
        if (user != null) {
            try {
                // 获取消息详细信息，并将消息保存为已读
                NoticeConfig message = announcementService.viewMessage(msgId, user.getId());
                resultMap.put("message", message);
            } catch (Exception e) {
                status = "failure";
                resultMap.put("msg", "读取通知消息错误!请重新刷新试试，并联系管理员");
                log.error("/view-notice-message", e);
            }
        }
        resultMap.put("status", status);
        return JSON.toJSONString(resultMap);
    }
}
