package com.lk.aizerocodeplatform.controller;

import com.lk.aizerocodeplatform.annotation.AuthCheck;
import com.lk.aizerocodeplatform.common.BaseResponse;
import com.lk.aizerocodeplatform.common.ResultUtils;
import com.lk.aizerocodeplatform.constant.UserConstant;
import com.lk.aizerocodeplatform.model.dto.app_featured_apply.*;
import com.lk.aizerocodeplatform.model.vo.app_featured_apply.AdminCheckVO;
import com.lk.aizerocodeplatform.model.vo.app_featured_apply.PageQueryFeatureApplyVO;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.lk.aizerocodeplatform.service.AppFeaturedApplyService;
import org.springframework.web.bind.annotation.RestController;


/**
 * 控制层。
 *
 * @author LK
 * @since 2026-04-26
 */
@Tag(name = "申请应用精选相关接口")
@RestController
@RequestMapping("/appFeaturedApply")
public class AppFeaturedApplyController {

    @Resource
    private AppFeaturedApplyService appFeaturedApplyService;

    @Operation(summary = "增加应用精选申请")
    @PostMapping(value = "/add")
    public BaseResponse<Long> addAppFeaturedApply(@RequestBody AddFeaturedApplyDTO addFeaturedApplyDTO, HttpServletRequest request) {
        return ResultUtils.success(appFeaturedApplyService.addAppFeaturedApply(addFeaturedApplyDTO, request));
    }

    @Operation(summary = "删除应用精选申请")
    @PostMapping(value = "/delete")
    public BaseResponse<Boolean> deleteAppFeaturedApply(@RequestBody DeleteFeaturedApplyDTO deleteFeaturedApplyDTO, HttpServletRequest request) {
        return ResultUtils.success(appFeaturedApplyService.deleteAppFeaturedApply(deleteFeaturedApplyDTO, request));
    }

    @Operation(summary = "更新应用精选申请")
    @PostMapping(value = "/update")
    public BaseResponse<Boolean> updateAppFeaturedApply(@RequestBody UpdateFeaturedApplyDTO updateFeaturedApplyDTO, HttpServletRequest request) {
        return ResultUtils.success(appFeaturedApplyService.updateAppFeaturedApply(updateFeaturedApplyDTO, request));
    }

    @Operation(summary = "用户分页查询应用精选申请")
    @PostMapping(value = "/pageQuery")
    public BaseResponse<Page<PageQueryFeatureApplyVO>> getPageQueryFeatureApplyVOList(@RequestBody PageQueryFeatureApplyDTO pageQueryFeatureApplyDTO, HttpServletRequest request) {
        return ResultUtils.success(appFeaturedApplyService.pageQueryAppFeaturedApply(pageQueryFeatureApplyDTO, request));
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员同意应用精选申请")
    @PostMapping(value = "/admin/agreeApply")
    public BaseResponse<String> agreeApply(@RequestBody AdminCheckDTO adminCheckDTO, HttpServletRequest request) {
        return ResultUtils.success(appFeaturedApplyService.agreeApplyByAdmin(adminCheckDTO, request));
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员拒绝应用精选申请")
    @PostMapping(value = "/admin/disagreeApply")
    public BaseResponse<String> disagreeApply(@RequestBody AdminCheckDTO adminCheckDTO, HttpServletRequest request) {
        return ResultUtils.success(appFeaturedApplyService.disagreeApplyByAdmin(adminCheckDTO, request));
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员撤销应用精选申请")
    @PostMapping(value = "/admin/cancelApply")
    public BaseResponse<String> cancelApply(@RequestBody AdminCheckDTO adminCheckDTO, HttpServletRequest request) {
        return ResultUtils.success(appFeaturedApplyService.cancelApplyByAdmin(adminCheckDTO, request));
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员分页查询应用精选申请")
    @PostMapping(value = "/admin/pageQueryApply")
    public BaseResponse<Page<AdminCheckVO>> adminPageQueryApply(@RequestBody AdminPageQueryFeatureApplyDTO adminPageQueryFeatureApplyDTO, HttpServletRequest request) {
        return ResultUtils.success(appFeaturedApplyService.adminCheckPageQuery(adminPageQueryFeatureApplyDTO, request));
    }

    @Operation(summary = "用户重新提交申请")
    @PostMapping(value = "/reAdd")
    public BaseResponse<String> reAddApply(@RequestBody ReAddFeaturedApplyDTO reAddFeaturedApplyDTO, HttpServletRequest request) {
        return ResultUtils.success(appFeaturedApplyService.reAddApply(reAddFeaturedApplyDTO, request));
    }

}
