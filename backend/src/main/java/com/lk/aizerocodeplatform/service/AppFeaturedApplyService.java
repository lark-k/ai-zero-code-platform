package com.lk.aizerocodeplatform.service;

import com.lk.aizerocodeplatform.model.dto.app_featured_apply.*;
import com.lk.aizerocodeplatform.model.vo.app_featured_apply.AdminCheckVO;
import com.lk.aizerocodeplatform.model.vo.app_featured_apply.PageQueryFeatureApplyVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.lk.aizerocodeplatform.model.entity.AppFeaturedApply;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 服务层。
 *
 * @author LK
 * @since 2026-04-26
 */
public interface AppFeaturedApplyService extends IService<AppFeaturedApply> {
    /**
     * 增加精选应用申请
     *
     * @param addFeaturedApplyDTO 精选应用申请请求信息
     * @return 精选应用申请表的id
     */
    Long addAppFeaturedApply(AddFeaturedApplyDTO addFeaturedApplyDTO, HttpServletRequest request);

    /**
     * 删除精选应用申请
     *
     * @param deleteFeaturedApply 精选应用删除请求信息
     * @return 是否删除成功
     */
    Boolean deleteAppFeaturedApply(DeleteFeaturedApplyDTO deleteFeaturedApply, HttpServletRequest request);

    /**
     * 更新精选应用申请
     *
     * @param updateFeaturedApplyDTO 精选应用更新请求信息
     * @return 是否更新成功
     */
    Boolean updateAppFeaturedApply(UpdateFeaturedApplyDTO updateFeaturedApplyDTO, HttpServletRequest request);

    /**
     * 用户分页查询精选申请信息
     *
     * @param pageQueryFeatureApplyDTO 分页查询精选申请请求信息
     * @return 条件分页查询后的结果
     */
    Page<PageQueryFeatureApplyVO> pageQueryAppFeaturedApply(PageQueryFeatureApplyDTO pageQueryFeatureApplyDTO, HttpServletRequest request);

    /**
     * 根据分页查询信息封装查询条件
     *
     * @param pageQueryFeatureApplyDTO 分页查询信息
     * @return 分页查询条件
     */
    QueryWrapper getQueryWrapper(PageQueryFeatureApplyDTO pageQueryFeatureApplyDTO);

    /**
     * 将多个AppFeaturedApply对象转换为多个PageQueryFeatureApplyVO对象
     *
     * @param appFeaturedApplyList 多个AppFeaturedApply对象
     * @return 多个PageQueryFeatureApplyVO对象
     */
    List<PageQueryFeatureApplyVO> getPageQueryFeatureApplyVOList(List<AppFeaturedApply> appFeaturedApplyList);

    /**
     * 同意精选应用申请
     *
     * @param adminCheckDTO 管理员审查请求信息
     */
    String agreeApplyByAdmin(AdminCheckDTO adminCheckDTO, HttpServletRequest request);

    /**
     * 拒绝精选应用申请
     *
     * @param adminCheckDTO 管理员审查请求信息
     */
    String disagreeApplyByAdmin(AdminCheckDTO adminCheckDTO, HttpServletRequest request);

    /**
     * 撤销精选应用申请
     *
     * @param adminCheckDTO 管理员审查请求信息
     */
    String cancelApplyByAdmin(AdminCheckDTO adminCheckDTO, HttpServletRequest request);

    /**
     * 管理员分页条件查询审查列表
     *
     * @param adminPageQueryFeatureApplyDTO 分页查询信息
     * @param request                       request请求
     * @return 分页查询后的结果列表
     */
    Page<AdminCheckVO> adminCheckPageQuery(AdminPageQueryFeatureApplyDTO adminPageQueryFeatureApplyDTO, HttpServletRequest request);

    /**
     * 封装管理员分页查询条件
     *
     * @param adminPageQueryFeatureApplyDTO 分页查询请求信息
     * @return 查询条件
     */
    QueryWrapper getAdminCheckPageQueryWrapper(AdminPageQueryFeatureApplyDTO adminPageQueryFeatureApplyDTO);

    /**
     * 将AppFeaturedApply转换为AdminCheckVO
     */
    List<AdminCheckVO> getAdminCheckPageVOList(List<AppFeaturedApply> appFeaturedApplyList);

}
