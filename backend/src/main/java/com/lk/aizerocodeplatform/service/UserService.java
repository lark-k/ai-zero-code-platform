package com.lk.aizerocodeplatform.service;

import com.lk.aizerocodeplatform.model.dto.UserLoginDTO;
import com.lk.aizerocodeplatform.model.dto.UserRegisterDTO;
import com.lk.aizerocodeplatform.model.vo.UserLoginVO;
import com.mybatisflex.core.service.IService;
import com.lk.aizerocodeplatform.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 服务层。
 *
 * @author LK
 * @since 2026-04-21
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userRegisterDTO 用户注册信息
     * @return 用户id
     */
    Long userRegister(UserRegisterDTO userRegisterDTO);

    /**
     * 用户登录
     *
     * @param userLoginDTO 用户登录信息
     * @return 用户信息
     */
    UserLoginVO userLogin(UserLoginDTO userLoginDTO, HttpServletRequest request);

    /**
     * 获取当前登录的用户信息
     *
     * @param request 请求
     * @return 当前登录的用户
     */
    UserLoginVO getCurrentUserLoginVo(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request 请求
     */
    void userLogout(HttpServletRequest request);

    /**
     * 加密密码md5
     *
     * @param password 原密码
     * @return
     */
    String encryptPassword(String password);

    /**
     * 用户信息脱敏返回给前端
     *
     * @param user 源用户信息
     * @return 脱敏后的用户信息
     */
    UserLoginVO desensitizeUserInfo(User user);
}
