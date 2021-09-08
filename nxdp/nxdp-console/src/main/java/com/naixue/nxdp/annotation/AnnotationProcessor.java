package com.naixue.nxdp.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;

import com.naixue.nxdp.model.User;
import com.naixue.nxdp.web.BaseController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Configuration
public class AnnotationProcessor {

    @Around("@annotation(com.zhuanzhuan.zzdp.annotation.Admin)")
    public Object permissionAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("注解com.zhuanzhuan.annotation.Admin处理器开始处理");
    /*MethodSignature ms = (MethodSignature) joinPoint.getSignature();
    Method currentMethod =
        joinPoint.getTarget().getClass().getMethod(ms.getName(), ms.getParameterTypes());
    Admin annotation = currentMethod.getAnnotation(com.zhuanzhuan.annotation.Admin.class);
    if (annotation != null) {

    }*/
        ServletRequestAttributes sra =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        User currentUser =
                (User)
                        WebUtils.getSessionAttribute(
                                sra.getRequest(), BaseController.Web.HTTP_SESSION_ATTRIBUTE_KEY_CURRENT_USER);
        if (User.PermissionLevel.ADMIN.getCode() != currentUser.getPermissionLevel()) {
            throw new RuntimeException("没有权限");
        }
        return joinPoint.proceed();
    }

    @Around("@annotation(com.zhuanzhuan.zzdp.annotation.Register)")
    public Object registerAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("注解com.zhuanzhuan.annotation.Register处理器开始处理");
        ServletRequestAttributes sra =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        User currentUser =
                (User)
                        WebUtils.getSessionAttribute(
                                sra.getRequest(), BaseController.Web.HTTP_SESSION_ATTRIBUTE_KEY_CURRENT_USER);
        // 判断是不是真实用户
        if (User.Flag.TRUE == currentUser.getFlag()) {
            throw new RuntimeException("已经注册过了");
        }
        return joinPoint.proceed();
    }

    @Around("@annotation(com.zhuanzhuan.zzdp.annotation.DistributedLock)")
    public Object DistributedLock() {
        return null;
    }
}
