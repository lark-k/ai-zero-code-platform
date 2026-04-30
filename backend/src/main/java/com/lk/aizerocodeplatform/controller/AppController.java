package com.lk.aizerocodeplatform.controller;

import com.lk.aizerocodeplatform.annotation.AuthCheck;
import com.lk.aizerocodeplatform.common.BaseResponse;
import com.lk.aizerocodeplatform.common.ResultUtils;
import com.lk.aizerocodeplatform.constant.UserConstant;
import com.lk.aizerocodeplatform.model.dto.app.*;
import com.lk.aizerocodeplatform.model.vo.app.AppVO;
import com.lk.aizerocodeplatform.model.vo.user.UserLoginVO;
import com.lk.aizerocodeplatform.service.UserService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import com.lk.aizerocodeplatform.service.AppService;
import reactor.core.publisher.Flux;


/**
 * 控制层。
 *
 * @author LK
 * @since 2026-04-24
 */
@Tag(name = "应用模块相关接口")
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;
    @Resource
    private UserService userService;

    @Operation(summary = "增加应用")
    @PostMapping("/addApp")
    public BaseResponse<Long> addApp(@RequestBody AddAppDTO addAppDTO, HttpServletRequest request) {
        return ResultUtils.success(appService.addApp(addAppDTO, request));
    }

    @Operation(summary = "修改应用")
    @PostMapping("/updateApp")
    public BaseResponse<Boolean> updateApp(@RequestBody UpdateAppDTO updateAppDTO, HttpServletRequest request) {
        return ResultUtils.success(appService.updateApp(updateAppDTO, request));
    }

    @Operation(summary = "删除应用")
    @PostMapping("/deleteApp")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteAppDTO deleteAppDTO, HttpServletRequest request) {
        return ResultUtils.success(appService.deleteApp(deleteAppDTO, request));
    }

    @Operation(summary = "根据id查询应用信息")
    @GetMapping("/getAppById")
    public BaseResponse<AppVO> getAppVoById(Long id) {
        return ResultUtils.success(appService.getAppById(id));
    }

    @Operation(summary = "分页查询应用信息(包括作者的脱敏信息)")
    @PostMapping("/getAppVoListByPage")
    public BaseResponse<Page<AppVO>> getAppVoByPage(@RequestBody QueryAppDTO queryAppDTO, HttpServletRequest request) {
        return ResultUtils.success(appService.getAppVoPage(queryAppDTO, request));
    }

    @Operation(summary = "分页查询精选应用信息(包括作者的脱敏信息)")
    @PostMapping("/getAppVoListByPageForGood")
    public BaseResponse<Page<AppVO>> getAppVoPageForGood(@RequestBody QueryAppDTO queryAppDTO) {
        return ResultUtils.success(appService.getAppVoPageForGood(queryAppDTO));
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员删除应用")
    @PostMapping("/admin/delete")
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteAppDTO deleteAppDTO) {
        return ResultUtils.success(appService.deleteAppByAdmin(deleteAppDTO));
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员更新应用")
    @PostMapping("/admin/update")
    public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateDTO appAdminUpdateDTO) {
        return ResultUtils.success(appService.updateAppByAdmin(appAdminUpdateDTO));
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员分页查询应用")
    @PostMapping("/admin/pageQuery")
    public BaseResponse<Page<AppVO>> queryPageByAdmin(@RequestBody QueryAppDTO queryAppDTO) {
        return ResultUtils.success(appService.getAppVoPageByAdmin(queryAppDTO));
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员查询单个应用信息")
    @PostMapping("/admin/getApp")
    public BaseResponse<AppVO> getAppVoByAdmin(Long id) {
        return ResultUtils.success(appService.getAppVoByAdmin(id));
    }

    @Operation(summary = "用户对话生成应用")
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatToGenCode(@RequestParam String message,
                                                       @RequestParam Long appId,
                                                       HttpServletRequest request) {
        UserLoginVO currentUserLoginVo = userService.getCurrentUserLoginVo(request);
        return appService.chatToGenCode(message, appId, currentUserLoginVo);
    }

    @Operation(summary = "用户部署应用")
    @PostMapping(value = "/deploy")
    public BaseResponse<String> appDeploy(@RequestBody AppDeployDTO appDeployDTO, HttpServletRequest request) {
        return ResultUtils.success(appService.appDeploy(appDeployDTO.getAppId(), userService.getCurrentUserLoginVo(request)));
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "置顶精选应用")
    @GetMapping(value = "/toTop")
    public BaseResponse<Boolean> stickToTop(@RequestParam Long appId) {
        return ResultUtils.success(appService.stickToTop(appId));
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "取消置顶精选应用")
    @GetMapping(value = "/cancelTop")
    public BaseResponse<Boolean> cancelTop(@RequestParam Long appId) {
        return ResultUtils.success(appService.cancelTop(appId));
    }

    @Operation(summary = "取消应用部署")
    @GetMapping(value = "/cancelDeploy")
    public BaseResponse<String> cancelDeploy(@RequestParam Long appId, HttpServletRequest request) {
        UserLoginVO currentUserLoginVo = userService.getCurrentUserLoginVo(request);
        return ResultUtils.success(appService.cancelDeploy(appId, currentUserLoginVo));
    }
}
