package com.naixue.nxdp.interceptor;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.naixue.nxdp.service.MainMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import com.naixue.nxdp.model.MainMenu;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.web.BaseController;

@Component
public class MainMenuInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private MainMenuService mainMenuService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String xRequestedWith = request.getParameter("X-Requested-With");
        if (!"XMLHttpRequest".equals(xRequestedWith)) {
            @SuppressWarnings("unchecked")
            List<MainMenu> mainMenus =
                    (List<MainMenu>)
                            WebUtils.getSessionAttribute(
                                    request, BaseController.Web.HTTP_SESSION_ATTRIBUTE_KEY_MAIN_MENUS);
            if (CollectionUtils.isEmpty(mainMenus)) {
                User currentUser =
                        (User)
                                WebUtils.getSessionAttribute(
                                        request, BaseController.Web.HTTP_SESSION_ATTRIBUTE_KEY_CURRENT_USER);
                if (currentUser == null || currentUser.getFlag() == User.Flag.FALSE) {
                    WebUtils.setSessionAttribute(
                            request,
                            BaseController.Web.HTTP_SESSION_ATTRIBUTE_KEY_MAIN_MENUS,
                            mainMenuService.visitorMenus());
                } else {
                    if (User.PermissionLevel.ADMIN.getCode() == currentUser.getPermissionLevel()) {
                        WebUtils.setSessionAttribute(
                                request,
                                BaseController.Web.HTTP_SESSION_ATTRIBUTE_KEY_MAIN_MENUS,
                                mainMenuService.adminMenus());
                    } else {
                        WebUtils.setSessionAttribute(
                                request,
                                BaseController.Web.HTTP_SESSION_ATTRIBUTE_KEY_MAIN_MENUS,
                                mainMenuService.defaultMenus());
                    }
                }
            }
        }
        return super.preHandle(request, response, handler);
    }
}
