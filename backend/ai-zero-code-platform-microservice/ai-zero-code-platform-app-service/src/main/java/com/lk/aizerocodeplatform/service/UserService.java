package com.lk.aizerocodeplatform.service;

import com.lk.aizerocodeplatform.model.entity.User;
import com.lk.aizerocodeplatform.model.vo.user.UserLoginVO;
import com.lk.aizerocodeplatform.model.vo.user.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collection;
import java.util.List;

/**
 * 应用服务内使用的用户能力门面，底层通过 Dubbo 调用用户服务。
 */
public interface UserService {

    /**
     * 获取当前登录的用户信息
     *
     * @param request 请求
     * @return 当前登录的用户
     */
    UserLoginVO getCurrentUserLoginVo(HttpServletRequest request);

    /**
     * 根据 id 获取用户
     */
    User getById(Long id);

    /**
     * 批量获取用户
     */
    List<User> listByIds(Collection<Long> ids);

    /**
     * 将 user 转换为脱敏后的 userVO
     */
    UserVO getUserVoByUser(User user);
}
