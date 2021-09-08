package com.naixue.nxdp.interceptor;

import java.text.MessageFormat;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.naixue.nxdp.consts.HttpCookieConsts;
import com.naixue.nxdp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import com.google.common.base.Strings;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.web.BaseController;

@Component
public class AgentUserInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Cookie sourceUserCookie = WebUtils.getCookie(request, HttpCookieConsts.COOKIE_SOURCE_USER);
        Cookie agentUserCookie = WebUtils.getCookie(request, HttpCookieConsts.COOKIE_TARGET_USER);
        User sessionUser =
                (User)
                        WebUtils.getSessionAttribute(
                                request, BaseController.Web.HTTP_SESSION_ATTRIBUTE_KEY_CURRENT_USER);
        if (agentUserCookie != null
                && !Strings.isNullOrEmpty(agentUserCookie.getValue())
                && !sessionUser.getPyName().equals(agentUserCookie.getValue())) {
            User agentUser = userService.getUserByUserPyName(agentUserCookie.getValue());
            Assert.notNull(
                    agentUser,
                    MessageFormat.format(
                            "the agent user[{0}] who did you set is not exist.", agentUserCookie.getValue()));
            agentUser.setAgent(true);
            WebUtils.setSessionAttribute(
                    request, BaseController.Web.HTTP_SESSION_ATTRIBUTE_KEY_CURRENT_USER, agentUser);
        }
        if (agentUserCookie == null
                && sourceUserCookie != null
                && !Strings.isNullOrEmpty(sourceUserCookie.getValue())
                && !sessionUser.getPyName().equals(sourceUserCookie.getValue())) {
            User sourceUser = userService.getUserByUserPyName(sourceUserCookie.getValue());
            Assert.notNull(
                    sourceUser,
                    MessageFormat.format(
                            "the source user[{0}] who did you set is not exist.", sourceUserCookie.getValue()));
            WebUtils.setSessionAttribute(
                    request, BaseController.Web.HTTP_SESSION_ATTRIBUTE_KEY_CURRENT_USER, sourceUser);
        }
        return super.preHandle(request, response, handler);
    }
}
