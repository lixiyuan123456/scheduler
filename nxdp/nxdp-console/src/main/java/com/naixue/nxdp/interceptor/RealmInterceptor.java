package com.naixue.nxdp.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.naixue.nxdp.config.CFG;
import com.naixue.nxdp.model.Account;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import com.naixue.nxdp.dao.AccountRepository;
import com.naixue.nxdp.web.BaseController;

/**
 * 登录
 *
 * @author zhaichuancheng
 */
@Component
public class RealmInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        User user =
                (User)
                        WebUtils.getSessionAttribute(
                                request, BaseController.Web.HTTP_SESSION_ATTRIBUTE_KEY_CURRENT_USER);
        if (user == null) {
            Cookie uidCookie = WebUtils.getCookie(request, BaseController.Web.HTTP_COOKIE_KEY_SSO_UID);
            if (uidCookie == null) {
                response.sendRedirect(CFG.ZZ_URL_SSO_QRCODE);
                return false;
            }
            user = userService.getUserByUserId(uidCookie.getValue());
            if (user == null) {
                Account account = accountRepository.findOne(uidCookie.getValue());
                User falseUser = new User();
                falseUser.setId(account.getId());
                falseUser.setPyName(account.getPyname());
                falseUser.setName(account.getName());
                // falseUser.setEmail(String.valueOf(currentUser.get("email")));
                // falseUser.setMobile(String.valueOf(currentUser.get("mobile")));
                // falseUser.setIsLeader(currentUser.getInteger("isleader"));
                falseUser.setDeptId(account.getDeptId());
                falseUser.setDeptName(account.getDeptName());
                // falseUser.setDepartmentpath(String.valueOf(currentUser.get("departmentpath")));
                falseUser.setFlag(User.Flag.FALSE);
                user = falseUser;
            }
            WebUtils.setSessionAttribute(
                    request, BaseController.Web.HTTP_SESSION_ATTRIBUTE_KEY_CURRENT_USER, user);
        }
        request.setAttribute("user", user);
        return super.preHandle(request, response, handler);
    }
}
