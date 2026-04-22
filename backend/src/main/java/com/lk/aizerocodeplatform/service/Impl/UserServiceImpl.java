package com.lk.aizerocodeplatform.service.Impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.lk.aizerocodeplatform.constant.UserConstant;
import com.lk.aizerocodeplatform.exception.BusinessException;
import com.lk.aizerocodeplatform.exception.ErrorCode;
import com.lk.aizerocodeplatform.exception.ThrowUtils;
import com.lk.aizerocodeplatform.model.dto.*;
import com.lk.aizerocodeplatform.model.vo.UserLoginVO;
import com.lk.aizerocodeplatform.model.vo.UserVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.lk.aizerocodeplatform.model.entity.User;
import com.lk.aizerocodeplatform.mapper.UserMapper;
import com.lk.aizerocodeplatform.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

/**
 * 服务层实现。
 *
 * @author LK
 * @since 2026-04-21
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public Long userRegister(UserRegisterDTO userRegisterDTO) {
        String userAccount = userRegisterDTO.getUserAccount();
        String userPassword = userRegisterDTO.getUserPassword();
        String confirmPassword = userRegisterDTO.getConfirmPassword();
        // 1. 判断参数是否为空
        if (StrUtil.hasEmpty(userAccount, userPassword, confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码不能为空");
        }
        // 2. 判断账号、密码、确认密码长度是否满足要求
        if (userAccount.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度小于6位");
        }
        if (userPassword.length() < 8 || confirmPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度小于8位");
        }
        // 3. 判断两次输入的密码是否一致
        if (!userPassword.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 4. 判断账号是否已经存在
        long count = count(new QueryWrapper().eq("userAccount", userAccount));
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该账号已存在");
        }
        // 5. 密码加密后入库
        String encryptPassword = encryptPassword(userPassword);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName(UserConstant.DEFAULT_USERNAME_PREFIX + RandomUtil.randomString(6));
        user.setUserRole(UserConstant.DEFAULT_ROLE);
        save(user);
        return user.getId();
    }

    @Override
    public UserLoginVO userLogin(UserLoginDTO userLoginDTO, HttpServletRequest request) {
        String userAccount = userLoginDTO.getUserAccount();
        String userPassword = userLoginDTO.getUserPassword();
        // 判断账号密码是否为空
        if (StrUtil.hasEmpty(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码不能为空");
        }
        // 根据账号密码查询数据库
        QueryWrapper queryWrapper = new QueryWrapper();
        // 加密密码后与数据库比对
        String encryptPassword = encryptPassword(userPassword);
        queryWrapper.eq("userAccount", userAccount).eq("userPassword", encryptPassword);
        User user = getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户不存在");
        }
        // 记录用户登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_SESSION_KEY, user);
        return desensitizeUserInfo(user);
    }

    @Override
    public UserLoginVO getCurrentUserLoginVo(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_SESSION_KEY);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        return desensitizeUserInfo(user);
    }

    @Override
    public void userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.NOT_LOGIN_ERROR);
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_SESSION_KEY);
    }

    @Override
    public String encryptPassword(String password) {
        return DigestUtils.md5DigestAsHex((UserConstant.SALT + password + UserConstant.SALT).getBytes());
    }

    @Override
    public UserLoginVO desensitizeUserInfo(User user) {
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);
        return userLoginVO;
    }

    @Override
    public Long saveUser(AddUserDTO addUserDTO) {
        ThrowUtils.throwIf(addUserDTO == null, ErrorCode.PARAMS_ERROR);
        String userAccount = addUserDTO.getUserAccount();
        long count = count(new QueryWrapper().eq("userAccount", userAccount));
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号已存在");
        }
        User user = new User();
        BeanUtils.copyProperties(addUserDTO, user);
        // 增加用户时设置一个默认密码
        user.setUserPassword(encryptPassword("12345678"));
        save(user);
        return user.getId();
    }

    @Override
    public Boolean updateUser(UpdateUserDTO updateUserDTO) {
        ThrowUtils.throwIf(updateUserDTO == null, ErrorCode.PARAMS_ERROR);
        Long id = updateUserDTO.getId();
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (getById(id) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        User user = new User();
        BeanUtils.copyProperties(updateUserDTO, user);
        return updateById(user);
    }

    @Override
    public Boolean deleteUser(DeleteUserDTO deleteUserDTO) {
        ThrowUtils.throwIf(deleteUserDTO == null, ErrorCode.PARAMS_ERROR);
        Long userId = deleteUserDTO.getId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(getById(userId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        return removeById(deleteUserDTO.getId());
    }

    @Override
    public Page<UserVO> pageQuery(QueryUserDTO queryUserDTO) {
        int pageNum = queryUserDTO.getPageNum();
        int pageSize = queryUserDTO.getPageSize();
        Page<User> userPage = this.page(Page.of(pageNum, pageSize), getQueryWrapper(queryUserDTO));
        List<UserVO> userVOList = userPage.getRecords().stream()
                .map((this::getUserVoByUser))
                .toList();
        Page<UserVO> userVoPage = new Page<>(pageNum, pageSize);
        userVoPage.setRecords(userVOList);
        userVoPage.setTotalPage(userPage.getTotalPage());
        userVoPage.setTotalRow(userPage.getTotalRow());
        return userVoPage;
    }

    @Override
    public QueryWrapper getQueryWrapper(QueryUserDTO queryUserDTO) {
        Long id = queryUserDTO.getId();
        String userName = queryUserDTO.getUserName();
        String userAccount = queryUserDTO.getUserAccount();
        String userProfile = queryUserDTO.getUserProfile();
        String userRole = queryUserDTO.getUserRole();
        String sortField = queryUserDTO.getSortField();
        String sortOrder = queryUserDTO.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .eq("userRole", userRole)
                .like("userAccount", userAccount)
                .like("userProfile", userProfile)
                .like("userName", userName)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public UserVO getUserVoByUser(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
}
