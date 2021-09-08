package com.naixue.nxdp.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.naixue.nxdp.consts.HttpCookieConsts;
import com.naixue.nxdp.model.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agentUser")
public class AgentUserController extends BaseController {

    @RequestMapping("/enableAgent.do")
    public Object enableAgent(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final String agentUser) {
        final User currentUser = getCurrentUser(request);
        Cookie sourceCookie = new Cookie(HttpCookieConsts.COOKIE_SOURCE_USER, currentUser.getPyName());
        sourceCookie.setPath("/");
        response.addCookie(sourceCookie);
        Cookie targetCookie = new Cookie(HttpCookieConsts.COOKIE_TARGET_USER, agentUser);
        targetCookie.setPath("/");
        response.addCookie(targetCookie);
        return success();
    }

    @RequestMapping("/disableAgent.do")
    public Object disableAgent(final HttpServletRequest request, final HttpServletResponse response) {
        Cookie cookie = new Cookie(HttpCookieConsts.COOKIE_TARGET_USER, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return success();
    }
}
