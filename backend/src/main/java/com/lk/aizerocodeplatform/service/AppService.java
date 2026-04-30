package com.lk.aizerocodeplatform.service;

import com.lk.aizerocodeplatform.model.dto.app.*;
import com.lk.aizerocodeplatform.model.vo.app.AppVO;
import com.lk.aizerocodeplatform.model.vo.user.UserLoginVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.lk.aizerocodeplatform.model.entity.App;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.List;

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

    /**
     * 分页条件查询应用信息，包括用户脱敏后的信息（只能查询当前登录用户的应用）
     *
     * @param queryAppDTO 查询应用信息的请求
     * @param request     request请求
     * @return 分页后的应用信息
     */
    Page<AppVO> getAppVoPage(QueryAppDTO queryAppDTO, HttpServletRequest request);

    /**
     * 根据查询请求参数封装查询条件
     *
     * @param queryAppDTO 查询请求参数
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(QueryAppDTO queryAppDTO);

    /**
     * 封装精选应用的查询条件
     *
     * @param queryAppDTO 查询请求参数
     * @return 查询条件
     */
    QueryWrapper getQueryWrapperForGood(QueryAppDTO queryAppDTO);

    /**
     * 将List App 转换为 List AppVO
     */
    List<AppVO> getAppVoListByAppList(List<App> appList);

    /**
     * 分页条件查询《精选应用》信息，包括用户脱敏后的信息
     * 用户不登陆也可以查看
     *
     * @param queryAppDTO 查询应用信息的请求
     * @return 分页后的应用信息
     */
    Page<AppVO> getAppVoPageForGood(QueryAppDTO queryAppDTO);

    /**
     * 管理员删除应用
     *
     * @param deleteAppDTO 删除请求信息
     * @return 是否删除成功
     */
    Boolean deleteAppByAdmin(DeleteAppDTO deleteAppDTO);

    /**
     * 管理员更新应用
     *
     * @param appAdminUpdateDTO 更新请求信息
     * @return 是否更新成功
     */
    Boolean updateAppByAdmin(AppAdminUpdateDTO appAdminUpdateDTO);

    /**
     * 管理员分页查询
     *
     * @param queryAppDTO 分页查询请求
     * @return 分页查询得到的信息
     */
    Page<AppVO> getAppVoPageByAdmin(QueryAppDTO queryAppDTO);

    /**
     * 管理员查询单个应用详细信息
     *
     * @param id 应用id
     * @return 应用信息（包括用户脱敏信息）
     */
    AppVO getAppVoByAdmin(Long id);

    /**
     * 对话生成代码
     *
     * @param message     用户消息
     * @param appId       应用id
     * @param userLoginVO 登录用户的脱敏信息
     * @return 流式响应
     */
    Flux<String> chatToGenCode(String message, Long appId, UserLoginVO userLoginVO);

    /**
     * 应用部署
     *
     * @param appId       应用id
     * @param userLoginVO 用户登录信息
     * @return 部署后网站可访问的链接地址
     */
    String appDeploy(Long appId, UserLoginVO userLoginVO);

    /**
     * 精选应用置顶
     *
     * @param appId 应用id
     * @return 是否成功
     */
    Boolean stickToTop(Long appId);

    /**
     * 取消精选应用置顶
     *
     * @param appId 应用id
     * @return 是否成功
     */
    Boolean cancelTop(Long appId);

    /**
     * 取消部署
     *
     * @param appId       应用id
     * @param userLoginVO 当前登录的用户信息
     * @return 是否取消部署成功
     */
    String cancelDeploy(Long appId, UserLoginVO userLoginVO);
}
