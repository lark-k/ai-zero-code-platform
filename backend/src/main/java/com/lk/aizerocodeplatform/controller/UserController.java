package com.lk.aizerocodeplatform.controller;

import com.lk.aizerocodeplatform.common.BaseResponse;
import com.lk.aizerocodeplatform.common.ResultUtils;
import com.lk.aizerocodeplatform.model.dto.UserLoginDTO;
import com.lk.aizerocodeplatform.model.dto.UserRegisterDTO;
import com.lk.aizerocodeplatform.model.vo.UserLoginVO;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.lk.aizerocodeplatform.model.entity.User;
import com.lk.aizerocodeplatform.service.UserService;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
     * 保存。
     *
     * @param user
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody User user) {
        return userService.save(user);
    }

    /**
     * 根据主键删除。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return userService.removeById(id);
    }

    /**
     * 根据主键更新。
     *
     * @param user
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody User user) {
        return userService.updateById(user);
    }

    /**
     * 查询所有。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<User> list() {
        return userService.list();
    }

    /**
     * 根据主键获取。
     *
     * @param id 主键
     * @return 详情
     */
    @GetMapping("getInfo/{id}")
    public User getInfo(@PathVariable Long id) {
        return userService.getById(id);
    }

    /**
     * 分页查询。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<User> page(Page<User> page) {
        return userService.page(page);
    }

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

    @Operation(summary = "获取当前用户登录信息")
    @GetMapping(value = "/getCurrentUser")
    public BaseResponse<UserLoginVO> getCurrentUserInfo(HttpServletRequest request) {
        UserLoginVO currentUserLoginVo = userService.getCurrentUserLoginVo(request);
        return ResultUtils.success(currentUserLoginVo);
    }

    @Operation(summary = "用户注销")
    @GetMapping(value = "/logout")
    public BaseResponse<?> userLogout(HttpServletRequest request) {
        userService.userLogout(request);
        return ResultUtils.success(null);
    }
}
