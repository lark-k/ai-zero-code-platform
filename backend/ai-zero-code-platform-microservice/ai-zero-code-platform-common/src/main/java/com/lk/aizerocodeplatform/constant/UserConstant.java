package com.lk.aizerocodeplatform.constant;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/21 22:41
 * 用户相关常量值
 */
public interface UserConstant {
    /**
     * 用户登录时session对应的key
     */
    String USER_LOGIN_SESSION_KEY = "user";
    /**
     * 密码加密-“盐”
     */
    String SALT = "d5parnEj";

    /**
     * 默认用户角色为user
     */
    String DEFAULT_ROLE = "user";
    /**
     * 管理员
     */
    String ADMIN_ROLE = "admin";

    /**
     * 默认的用户名前缀
     */
    String DEFAULT_USERNAME_PREFIX = "default_";
}
