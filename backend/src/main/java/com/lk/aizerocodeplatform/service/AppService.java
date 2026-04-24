package com.lk.aizerocodeplatform.service;

import com.lk.aizerocodeplatform.model.dto.app.AddAppDTO;
import com.lk.aizerocodeplatform.model.dto.app.DeleteAppDTO;
import com.lk.aizerocodeplatform.model.dto.app.UpdateAppDTO;
import com.lk.aizerocodeplatform.model.vo.app.AppVO;
import com.mybatisflex.core.service.IService;
import com.lk.aizerocodeplatform.model.entity.App;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 服务层。
 *
 * @author LK
 * @since 2026-04-24
 */
public interface AppService extends IService<App> {
    /**
     * 增加应用
     *
     * @param addAppDTO 增加应用请求
     * @param request   request请求
     * @return 新应用的id
     */
    Long addApp(AddAppDTO addAppDTO, HttpServletRequest request);

    /**
     * 更新应用（暂时只支持修改应用名称）
     * 只能更新自己的应用
     *
     * @param updateAppDTO 更新应用请求
     * @param request      request请求
     * @return 是否更新成功
     */
    Boolean updateApp(UpdateAppDTO updateAppDTO, HttpServletRequest request);

    /**
     * 删除应用
     * 只能删除自己的应用
     *
     * @param deleteAppDTO 删除应用请求
     * @param request      request请求
     * @return 是否删除成功
     */
    Boolean deleteApp(DeleteAppDTO deleteAppDTO, HttpServletRequest request);

    /**
     * 根据id查询该应用的详细信息（包括作者信息）
     *
     * @param id 应用id
     * @return 应用的详细信息（包括作者信息）
     */
    AppVO getAppById(Long id);

    /**
     * 根据app信息得到appvo信息
     *
     * @param app app信息
     * @return appVO信息
     */
    AppVO getAppVoByApp(App app);
}
