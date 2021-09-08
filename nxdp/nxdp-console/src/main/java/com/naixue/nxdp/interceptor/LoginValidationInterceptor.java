package com.naixue.nxdp.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.naixue.nxdp.model.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import com.naixue.nxdp.web.BaseController;

/**
 * 默认权限
 *
 * @author zhaichuancheng
 */
@Component
public class LoginValidationInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        User currentUser =
                (User)
                        WebUtils.getSessionAttribute(
                                request, BaseController.Web.HTTP_SESSION_ATTRIBUTE_KEY_CURRENT_USER);
        if (currentUser == null || currentUser.getFlag() == User.Flag.FALSE) {
            throw new RuntimeException("无权操作");
        }
        return super.preHandle(request, response, handler);
    }
}
