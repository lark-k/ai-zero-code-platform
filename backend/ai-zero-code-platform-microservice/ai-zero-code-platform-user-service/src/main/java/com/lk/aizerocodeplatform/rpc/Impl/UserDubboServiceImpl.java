package com.lk.aizerocodeplatform.rpc.Impl;

import com.lk.aizerocodeplatform.model.dto.user.AddUserDTO;
import com.lk.aizerocodeplatform.model.dto.user.DeleteUserDTO;
import com.lk.aizerocodeplatform.model.dto.user.QueryUserDTO;
import com.lk.aizerocodeplatform.model.dto.user.UpdateUserDTO;
import com.lk.aizerocodeplatform.model.dto.user.UserRegisterDTO;
import com.lk.aizerocodeplatform.model.entity.User;
import com.lk.aizerocodeplatform.model.vo.user.UserLoginVO;
import com.lk.aizerocodeplatform.model.vo.user.UserVO;
import com.lk.aizerocodeplatform.rpc.UserDubboService;
import com.lk.aizerocodeplatform.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collection;
import java.util.List;

@DubboService
public class UserDubboServiceImpl implements UserDubboService {

    @Resource
    private UserService userService;

    @Override
    public Long userRegister(UserRegisterDTO userRegisterDTO) {
        return userService.userRegister(userRegisterDTO);
    }

    @Override
    public Long saveUser(AddUserDTO addUserDTO) {
        return userService.saveUser(addUserDTO);
    }

    @Override
    public Boolean updateUser(UpdateUserDTO updateUserDTO) {
        return userService.updateUser(updateUserDTO);
    }

    @Override
    public Boolean deleteUser(DeleteUserDTO deleteUserDTO) {
        return userService.deleteUser(deleteUserDTO);
    }

    @Override
    public Page<UserVO> pageQuery(QueryUserDTO queryUserDTO) {
        return userService.pageQuery(queryUserDTO);
    }

    @Override
    public User getById(Long id) {
        return userService.getById(id);
    }

    @Override
    public List<User> listByIds(Collection<Long> ids) {
        return userService.listByIds(ids);
    }

    @Override
    public UserLoginVO desensitizeUserInfo(User user) {
        return userService.desensitizeUserInfo(user);
    }

    @Override
    public UserVO getUserVoByUser(User user) {
        return userService.getUserVoByUser(user);
    }
}
