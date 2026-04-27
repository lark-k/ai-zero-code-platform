package com.lk.aizerocodeplatform.service;

import com.lk.aizerocodeplatform.model.dto.app_featured_apply.AddFeaturedApplyDTO;
import com.lk.aizerocodeplatform.model.dto.app_featured_apply.DeleteFeaturedApplyDTO;
import com.lk.aizerocodeplatform.model.dto.app_featured_apply.PageQueryFeatureApplyDTO;
import com.lk.aizerocodeplatform.model.dto.app_featured_apply.UpdateFeaturedApplyDTO;
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
}
