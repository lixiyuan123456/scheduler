package com.naixue.nxdp.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.config.CFG;
import com.naixue.nxdp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

import com.alibaba.fastjson.JSON;
import com.naixue.nxdp.attachment.hue.service.HueService;
import com.naixue.nxdp.attachment.hue.service.HueService.Account;

@Controller
@RequestMapping("/sso")
public class SSOController extends BaseController {

    public static final String JSESSIONID = "JSESSIONID";

    @Autowired
    HueService hueService;

    @ResponseBody
    @RequestMapping("/get_jsessionid.jsonp")
    public Object getJSessionId(
            HttpServletRequest request, @RequestParam("callback") String callback) {
        Cookie cookie = WebUtils.getCookie(request, JSESSIONID);
        return callback + "(" + JSON.toJSONString(cookie) + ")";
    }

    @ResponseBody
    @RequestMapping("/get-user-by-jsessionid.jsonp")
    public Object getUserByJSessionId(
            HttpServletRequest request, @RequestParam("callback") String callback) {
        Cookie cookie = WebUtils.getCookie(request, JSESSIONID);
        Map<String, Object> data = new HashMap<>();
        if (cookie != null) {
            data.put(JSESSIONID, cookie);
            data.put(BaseController.Web.HTTP_SESSION_ATTRIBUTE_KEY_CURRENT_USER, getCurrentUser(request));
        }
        return callback + "(" + JSON.toJSONString(data) + ")";
    }

    @ResponseBody
    @RequestMapping("/get-all-cookies.jsonp")
    public Object getAllCookies(
            HttpServletRequest request, @RequestParam("callback") String callback) {
        return callback + "(" + JSON.toJSONString(request.getCookies()) + ")";
    }

    @RequestMapping("/hue")
    public Object goHUE(HttpServletRequest request, Model model) {
        User currentUser = getCurrentUser(request);
        Account account = hueService.getDefaultHueAccount(currentUser.getPyName());
        model.addAttribute("account", account);
        model.addAttribute("hue_url", CFG.HUE_URL);
        return "attachment/hue/start";
    }
}
