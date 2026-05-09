package com.lk.aizerocodeplatform.service.Impl;

import com.lk.aizerocodeplatform.constant.UserConstant;
import com.lk.aizerocodeplatform.exception.ErrorCode;
import com.lk.aizerocodeplatform.exception.ThrowUtils;
import com.lk.aizerocodeplatform.model.entity.User;
import com.lk.aizerocodeplatform.model.vo.user.UserLoginVO;
import com.lk.aizerocodeplatform.model.vo.user.UserVO;
import com.lk.aizerocodeplatform.rpc.UserDubboService;
import com.lk.aizerocodeplatform.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 应用服务通过 Dubbo 调用用户服务，保持原应用模块内 UserService 的使用方式不变。
 */
@Service
public class UserDubboAdapterServiceImpl implements UserService {

    @DubboReference(check = false)
    private UserDubboService userDubboService;

    @Override
    public UserLoginVO getCurrentUserLoginVo(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_SESSION_KEY);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        return userDubboService.desensitizeUserInfo(user);
    }

    @Override
    public User getById(Long id) {
        return userDubboService.getById(id);
    }

    @Override
    public List<User> listByIds(Collection<Long> ids) {
        return userDubboService.listByIds(ids);
    }

    @Override
    public UserVO getUserVoByUser(User user) {
        return userDubboService.getUserVoByUser(user);
    }
}
