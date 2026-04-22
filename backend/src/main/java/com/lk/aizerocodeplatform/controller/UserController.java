package com.lk.aizerocodeplatform.controller;

import com.lk.aizerocodeplatform.annotation.AuthCheck;
import com.lk.aizerocodeplatform.common.BaseResponse;
import com.lk.aizerocodeplatform.common.ResultUtils;
import com.lk.aizerocodeplatform.constant.UserConstant;
import com.lk.aizerocodeplatform.model.dto.UserLoginDTO;
import com.lk.aizerocodeplatform.model.dto.UserRegisterDTO;
import com.lk.aizerocodeplatform.model.vo.UserLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.lk.aizerocodeplatform.service.UserService;
import org.springframework.web.bind.annotation.RestController;


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


}
