package com.lk.aizerocodeplatform.aop;

import com.lk.aizerocodeplatform.annotation.AuthCheck;
import com.lk.aizerocodeplatform.constant.UserConstant;
import com.lk.aizerocodeplatform.exception.BusinessException;
import com.lk.aizerocodeplatform.exception.ErrorCode;
import com.lk.aizerocodeplatform.exception.ThrowUtils;
import com.lk.aizerocodeplatform.model.vo.user.UserLoginVO;
import com.lk.aizerocodeplatform.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/22 14:36
 * 切面类,用于用户权限验证
 */
@Aspect
@Component
public class UserInterceptor {
    @Resource
    private UserService userService;

    /**
     * 用户权限拦截
     *
     * @param joinPoint 连接点
     * @param authCheck 权限注解
     */
    @Around(value = "@annotation(authCheck)")
    public Object doIntercept(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 从权限注解拿到指定的角色(user/admin)
        String mustRole = authCheck.mustRole();
        // 拿到request请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取用户登录态
        UserLoginVO currentUser = userService.getCurrentUserLoginVo(request);
        ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 拿到当前登录的用户角色
        String currentUserRole = currentUser.getUserRole();
        if (mustRole.isEmpty()) {
            // 不需要权限，直接放行
            return joinPoint.proceed();
        }
        if (mustRole.equals(UserConstant.ADMIN_ROLE) && !currentUserRole.equals(UserConstant.ADMIN_ROLE)) {
            // 必须管理员可以登录
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 放行
        return joinPoint.proceed();
    }
}
