package com.lk.aizerocodeplatform.controller;

import com.lk.aizerocodeplatform.annotation.AuthCheck;
import com.lk.aizerocodeplatform.common.BaseResponse;
import com.lk.aizerocodeplatform.common.ResultUtils;
import com.lk.aizerocodeplatform.constant.UserConstant;
import com.lk.aizerocodeplatform.model.dto.*;
import com.lk.aizerocodeplatform.model.vo.UserLoginVO;
import com.lk.aizerocodeplatform.model.vo.UserVO;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import com.lk.aizerocodeplatform.service.UserService;


/**
 * 控制层。
 *
 * @author LK
 * @since 2026-04-21
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户相关接口")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterDTO 用户注册信息
     */
    @Operation(summary = "用户注册")
    @PostMapping(value = "/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterDTO userRegisterDTO) {
        Long userId = userService.userRegister(userRegisterDTO);
        return ResultUtils.success(userId);
    }

    /**
     * 用户登录
     *
     * @param userLoginDTO 用户登录信息
     */
    @Operation(summary = "用户登录")
    @PostMapping(value = "/login")
    public BaseResponse<UserLoginVO> userLogin(@RequestBody UserLoginDTO userLoginDTO, HttpServletRequest request) {
        UserLoginVO userLoginVO = userService.userLogin(userLoginDTO, request);
        return ResultUtils.success(userLoginVO);
    }

    /**
     * 获取当前用户登录信息
     *
     * @param request 请求
     * @return 脱敏后的用户信息
     */
    @Operation(summary = "获取当前用户登录信息")
    @GetMapping(value = "/getCurrentUser")
    public BaseResponse<UserLoginVO> getCurrentUserInfo(HttpServletRequest request) {
        UserLoginVO currentUserLoginVo = userService.getCurrentUserLoginVo(request);
        return ResultUtils.success(currentUserLoginVo);
    }

    /**
     * 用户注销
     *
     * @param request 请求
     */
    @Operation(summary = "用户注销")
    @GetMapping(value = "/logout")
    public BaseResponse<?> userLogout(HttpServletRequest request) {
        userService.userLogout(request);
        return ResultUtils.success(null);
    }

    /**
     * 增加用户（仅支持管理员调用）
     *
     * @param addUserDTO 增加的用户信息
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "增加用户")
    @PostMapping("/save")
    public BaseResponse<Long> saveUser(@RequestBody AddUserDTO addUserDTO) {
        Long userId = userService.saveUser(addUserDTO);
        return ResultUtils.success(userId);
    }

    /**
     * 更新用户（仅支持管理员调用）
     *
     * @param updateUserDTO 更新的用户信息
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "更新用户")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UpdateUserDTO updateUserDTO) {
        return ResultUtils.success(userService.updateUser(updateUserDTO));
    }

    /**
     * 删除用户（仅支持管理员调用）
     *
     * @param deleteUserDTO 删除的用户信息
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "删除用户")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteUserDTO deleteUserDTO) {
        return ResultUtils.success(userService.deleteUser(deleteUserDTO));
    }

    /**
     * 条件分页查询
     *
     * @param queryUserDTO 分页查询条件
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "条件分页查询")
    @PostMapping("/page")
    public BaseResponse<Page<UserVO>> getUserPage(@RequestBody QueryUserDTO queryUserDTO) {
        return ResultUtils.success(userService.pageQuery(queryUserDTO));
    }

}
