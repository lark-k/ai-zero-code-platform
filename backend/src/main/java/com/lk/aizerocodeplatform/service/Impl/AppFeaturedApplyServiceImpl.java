package com.lk.aizerocodeplatform.service.Impl;

import com.lk.aizerocodeplatform.constant.AppFeaturedApplyConstant;
import com.lk.aizerocodeplatform.exception.BusinessException;
import com.lk.aizerocodeplatform.exception.ErrorCode;
import com.lk.aizerocodeplatform.exception.ThrowUtils;
import com.lk.aizerocodeplatform.model.dto.app_featured_apply.*;
import com.lk.aizerocodeplatform.model.entity.App;
import com.lk.aizerocodeplatform.model.vo.app_featured_apply.AdminCheckVO;
import com.lk.aizerocodeplatform.model.vo.app_featured_apply.PageQueryFeatureApplyVO;
import com.lk.aizerocodeplatform.model.vo.user.UserLoginVO;
import com.lk.aizerocodeplatform.service.AppService;
import com.lk.aizerocodeplatform.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.lk.aizerocodeplatform.model.entity.AppFeaturedApply;
import com.lk.aizerocodeplatform.mapper.AppFeaturedApplyMapper;
import com.lk.aizerocodeplatform.service.AppFeaturedApplyService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务层实现。
 *
 * @author LK
 * @since 2026-04-26
 */
@Service
public class AppFeaturedApplyServiceImpl extends ServiceImpl<AppFeaturedApplyMapper, AppFeaturedApply> implements AppFeaturedApplyService {
    @Resource
    private AppService appService;
    @Resource
    private UserService userService;

    @Override
    public Long addAppFeaturedApply(AddFeaturedApplyDTO addFeaturedApplyDTO, HttpServletRequest request) {
        // 1、校验参数
        ThrowUtils.throwIf(addFeaturedApplyDTO == null, ErrorCode.PARAMS_ERROR);
        Long appId = addFeaturedApplyDTO.getAppId();
        String applyReason = addFeaturedApplyDTO.getApplyReason();
        App app = appService.getById(appId);
        if (app == null) {
            // 验证所申请的应用是否存在
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        // 2、验证登录
        UserLoginVO currentUserLoginVo = userService.getCurrentUserLoginVo(request);
        if (currentUserLoginVo == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 3、只能增加自己应用的精选申请
        if (!app.getUserId().equals(currentUserLoginVo.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 判断该应用是否已经申请过精选
        long count = count(new QueryWrapper().eq("appId", appId));
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该应用已申请精选，请等待审核");
        }
        // 4、信息入库
        AppFeaturedApply appFeaturedApply = new AppFeaturedApply();
        appFeaturedApply.setAppId(appId);
        appFeaturedApply.setApplyReason(applyReason);
        appFeaturedApply.setUserId(currentUserLoginVo.getId());
        appFeaturedApply.setStatus("pending");
        save(appFeaturedApply);
        // 5、返回申请表id
        return appFeaturedApply.getId();
    }

    @Override
    public Boolean deleteAppFeaturedApply(DeleteFeaturedApplyDTO deleteFeaturedApply, HttpServletRequest request) {
        // 1、校验参数
        ThrowUtils.throwIf(deleteFeaturedApply == null, ErrorCode.PARAMS_ERROR);
        // 2、判断应用申请是否存在
        Long id = deleteFeaturedApply.getId();
        AppFeaturedApply appFeaturedApply = getById(id);
        if (appFeaturedApply == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "申请不存在");
        }
        // 3、判断登录信息
        UserLoginVO currentUserLoginVo = userService.getCurrentUserLoginVo(request);
        if (currentUserLoginVo == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 4、只能删除自己的申请记录
        if (!appFeaturedApply.getUserId().equals(currentUserLoginVo.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 5、删除
        boolean success = removeById(id);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除申请失败");
        }
        // 6、返回是否删除成功
        return true;
    }

    @Override
    public Boolean updateAppFeaturedApply(UpdateFeaturedApplyDTO updateFeaturedApplyDTO, HttpServletRequest request) {
        // 1、校验参数
        ThrowUtils.throwIf(updateFeaturedApplyDTO == null, ErrorCode.PARAMS_ERROR);
        Long id = updateFeaturedApplyDTO.getId();
        String applyReason = updateFeaturedApplyDTO.getApplyReason();
        // 2、判断应用申请是否存在
        AppFeaturedApply appFeaturedApply = getById(id);
        if (appFeaturedApply == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "申请不存在");
        }
        // 3、判断用户登录信息
        UserLoginVO currentUserLoginVo = userService.getCurrentUserLoginVo(request);
        if (currentUserLoginVo == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 4、只能更新自己的申请记录
        if (!appFeaturedApply.getUserId().equals(currentUserLoginVo.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 5、更新
        AppFeaturedApply updateAppFeaturedApply = new AppFeaturedApply();
        updateAppFeaturedApply.setApplyReason(applyReason);
        updateAppFeaturedApply.setId(id);
        updateAppFeaturedApply.setUpdateTime(LocalDateTime.now());
        boolean success = updateById(updateAppFeaturedApply);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新申请记录失败");
        }
        return true;
    }

    @Override
    public Page<PageQueryFeatureApplyVO> pageQueryAppFeaturedApply(PageQueryFeatureApplyDTO pageQueryFeatureApplyDTO, HttpServletRequest request) {
        // 1、校验参数
        ThrowUtils.throwIf(pageQueryFeatureApplyDTO == null, ErrorCode.PARAMS_ERROR);
        int pageNum = pageQueryFeatureApplyDTO.getPageNum();
        int pageSize = pageQueryFeatureApplyDTO.getPageSize();
        // 2、判断用户登录信息
        UserLoginVO currentUserLoginVo = userService.getCurrentUserLoginVo(request);
        if (currentUserLoginVo == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 3、封装分页查询条件
        QueryWrapper queryWrapper = getQueryWrapper(pageQueryFeatureApplyDTO);
        // 用户只能查看自己的申请记录
        queryWrapper.eq("userId", currentUserLoginVo.getId());
        // 4、分页查询
        Page<AppFeaturedApply> queryResult = this.page(Page.of(pageNum, pageSize), queryWrapper);
        List<AppFeaturedApply> records = queryResult.getRecords();
        List<PageQueryFeatureApplyVO> pageQueryFeatureApplyVOList = getPageQueryFeatureApplyVOList(records);
        // 5、返回信息
        Page<PageQueryFeatureApplyVO> pageQueryFeatureApplyVoPage = new Page<>(pageNum, pageSize, queryResult.getTotalRow());
        pageQueryFeatureApplyVoPage.setRecords(pageQueryFeatureApplyVOList);
        return pageQueryFeatureApplyVoPage;
    }

    @Override
    public QueryWrapper getQueryWrapper(PageQueryFeatureApplyDTO pageQueryFeatureApplyDTO) {
        Long id = pageQueryFeatureApplyDTO.getId();
        Long appId = pageQueryFeatureApplyDTO.getAppId();
        String applyReason = pageQueryFeatureApplyDTO.getApplyReason();
        String status = pageQueryFeatureApplyDTO.getStatus();
        String sortField = pageQueryFeatureApplyDTO.getSortField();
        String sortOrder = pageQueryFeatureApplyDTO.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .eq("appId", appId)
                .like("applyReason", applyReason)
                .eq("status", status)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public List<PageQueryFeatureApplyVO> getPageQueryFeatureApplyVOList(List<AppFeaturedApply> appFeaturedApplyList) {
        List<PageQueryFeatureApplyVO> pageQueryFeatureApplyVoList = new ArrayList<>();
        appFeaturedApplyList
                .forEach((appFeaturedApply) -> {
                    PageQueryFeatureApplyVO pageQueryFeatureApplyVO = new PageQueryFeatureApplyVO();
                    BeanUtils.copyProperties(appFeaturedApply, pageQueryFeatureApplyVO);
                    pageQueryFeatureApplyVoList.add(pageQueryFeatureApplyVO);
                });
        return pageQueryFeatureApplyVoList;
    }

    @Override
    public String agreeApplyByAdmin(AdminCheckDTO adminCheckDTO, HttpServletRequest request) {
        // 判断管理员是否登录
        UserLoginVO currentUserLoginVo = userService.getCurrentUserLoginVo(request);
        if (currentUserLoginVo == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long id = adminCheckDTO.getId();
        String reviewRemark = adminCheckDTO.getReviewRemark();
        // 判断申请是否存在
        AppFeaturedApply appFeaturedApply = getById(id);
        if (appFeaturedApply == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "申请不存在");
        }
        // 更新申请状态
        AppFeaturedApply updateFeaturedApply = new AppFeaturedApply();
        updateFeaturedApply.setId(id);
        updateFeaturedApply.setReviewRemark(reviewRemark);
        updateFeaturedApply.setReviewTime(LocalDateTime.now());
        updateFeaturedApply.setReviewUserId(currentUserLoginVo.getId());
        updateFeaturedApply.setStatus(AppFeaturedApplyConstant.AGREE_APPLY);
        updateById(updateFeaturedApply);
        return "已同意";
    }

    @Override
    public String disagreeApplyByAdmin(AdminCheckDTO adminCheckDTO, HttpServletRequest request) {
        // 判断管理员是否登录
        UserLoginVO currentUserLoginVo = userService.getCurrentUserLoginVo(request);
        if (currentUserLoginVo == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long id = adminCheckDTO.getId();
        String reviewRemark = adminCheckDTO.getReviewRemark();
        // 判断申请是否存在
        AppFeaturedApply appFeaturedApply = getById(id);
        if (appFeaturedApply == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "申请不存在");
        }
        // 更新申请状态
        AppFeaturedApply updateFeaturedApply = new AppFeaturedApply();
        updateFeaturedApply.setId(id);
        updateFeaturedApply.setReviewRemark(reviewRemark);
        updateFeaturedApply.setReviewTime(LocalDateTime.now());
        updateFeaturedApply.setReviewUserId(currentUserLoginVo.getId());
        updateFeaturedApply.setStatus(AppFeaturedApplyConstant.DISAGREE_APPLY);
        updateById(updateFeaturedApply);
        return "已拒绝";
    }

    @Override
    public String cancelApplyByAdmin(AdminCheckDTO adminCheckDTO, HttpServletRequest request) {
        // 判断管理员是否登录
        UserLoginVO currentUserLoginVo = userService.getCurrentUserLoginVo(request);
        if (currentUserLoginVo == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long id = adminCheckDTO.getId();
        String reviewRemark = adminCheckDTO.getReviewRemark();
        // 判断申请是否存在
        AppFeaturedApply appFeaturedApply = getById(id);
        if (appFeaturedApply == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "申请不存在");
        }
        // 更新申请状态
        AppFeaturedApply updateFeaturedApply = new AppFeaturedApply();
        updateFeaturedApply.setId(id);
        updateFeaturedApply.setReviewRemark(reviewRemark);
        updateFeaturedApply.setReviewTime(LocalDateTime.now());
        updateFeaturedApply.setReviewUserId(currentUserLoginVo.getId());
        updateFeaturedApply.setStatus(AppFeaturedApplyConstant.CANCEL_APPLY);
        updateById(updateFeaturedApply);
        return "已撤销";
    }

    @Override
    public Page<AdminCheckVO> adminCheckPageQuery(AdminPageQueryFeatureApplyDTO adminPageQueryFeatureApplyDTO, HttpServletRequest request) {
        // 判断参数
        ThrowUtils.throwIf(adminPageQueryFeatureApplyDTO == null, ErrorCode.PARAMS_ERROR);
        // 验证登录
        UserLoginVO currentUserLoginVo = userService.getCurrentUserLoginVo(request);
        if (currentUserLoginVo == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        int pageNum = adminPageQueryFeatureApplyDTO.getPageNum();
        int pageSize = adminPageQueryFeatureApplyDTO.getPageSize();
        // 封装查询条件
        QueryWrapper queryWrapper = getAdminCheckPageQueryWrapper(adminPageQueryFeatureApplyDTO);
        // 分页查询
        Page<AppFeaturedApply> queryResult = this.page(Page.of(pageNum, pageSize), queryWrapper);
        List<AppFeaturedApply> records = queryResult.getRecords();
        // 封装结果信息并返回
        List<AdminCheckVO> adminCheckPageVOList = getAdminCheckPageVOList(records);
        Page<AdminCheckVO> adminCheckVoPage = new Page<>(pageNum, pageSize, queryResult.getTotalRow());
        adminCheckVoPage.setRecords(adminCheckPageVOList);
        return adminCheckVoPage;
    }

    @Override
    public QueryWrapper getAdminCheckPageQueryWrapper(AdminPageQueryFeatureApplyDTO adminPageQueryFeatureApplyDTO) {
        Long id = adminPageQueryFeatureApplyDTO.getId();
        Long appId = adminPageQueryFeatureApplyDTO.getAppId();
        Long userId = adminPageQueryFeatureApplyDTO.getUserId();
        String applyReason = adminPageQueryFeatureApplyDTO.getApplyReason();
        String status = adminPageQueryFeatureApplyDTO.getStatus();
        String reviewRemark = adminPageQueryFeatureApplyDTO.getReviewRemark();
        Long reviewUserId = adminPageQueryFeatureApplyDTO.getReviewUserId();
        String sortField = adminPageQueryFeatureApplyDTO.getSortField();
        String sortOrder = adminPageQueryFeatureApplyDTO.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .eq("appId", appId)
                .eq("userId", userId)
                .like("applyReason", applyReason)
                .eq("status", status)
                .like("reviewRemark", reviewRemark)
                .eq("reviewUserId", reviewUserId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public List<AdminCheckVO> getAdminCheckPageVOList(List<AppFeaturedApply> appFeaturedApplyList) {
        ArrayList<AdminCheckVO> adminCheckVoList = new ArrayList<>();
        appFeaturedApplyList.forEach(appFeaturedApply -> {
            AdminCheckVO adminCheckVO = new AdminCheckVO();
            BeanUtils.copyProperties(appFeaturedApply, adminCheckVO);
            adminCheckVoList.add(adminCheckVO);
        });
        return adminCheckVoList;
    }
}