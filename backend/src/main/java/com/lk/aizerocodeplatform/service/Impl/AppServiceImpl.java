package com.lk.aizerocodeplatform.service.Impl;

import cn.hutool.core.collection.CollUtil;
import com.lk.aizerocodeplatform.constant.AppConstant;
import com.lk.aizerocodeplatform.enums.CodeGenTypeEnum;
import com.lk.aizerocodeplatform.exception.BusinessException;
import com.lk.aizerocodeplatform.exception.ErrorCode;
import com.lk.aizerocodeplatform.exception.ThrowUtils;
import com.lk.aizerocodeplatform.model.dto.app.AddAppDTO;
import com.lk.aizerocodeplatform.model.dto.app.DeleteAppDTO;
import com.lk.aizerocodeplatform.model.dto.app.QueryAppDTO;
import com.lk.aizerocodeplatform.model.dto.app.UpdateAppDTO;
import com.lk.aizerocodeplatform.model.entity.User;
import com.lk.aizerocodeplatform.model.vo.app.AppVO;
import com.lk.aizerocodeplatform.model.vo.user.UserLoginVO;
import com.lk.aizerocodeplatform.model.vo.user.UserVO;
import com.lk.aizerocodeplatform.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.lk.aizerocodeplatform.model.entity.App;
import com.lk.aizerocodeplatform.mapper.AppMapper;
import com.lk.aizerocodeplatform.service.AppService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 服务层实现。
 *
 * @author LK
 * @since 2026-04-24
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {
    @Resource
    private UserService userService;

    @Override
    public Long addApp(AddAppDTO addAppDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(addAppDTO == null, ErrorCode.PARAMS_ERROR);
        // 获取用户初始化的提示词
        String initPrompt = addAppDTO.getInitPrompt();
        ThrowUtils.throwIf(initPrompt == null, ErrorCode.PARAMS_ERROR, "用户初始化提示词为空");
        // 获取当前登录用户脱敏后的信息
        UserLoginVO currentUserLoginVo = userService.getCurrentUserLoginVo(request);
        ThrowUtils.throwIf(currentUserLoginVo == null, ErrorCode.NOT_LOGIN_ERROR);
        App app = new App();
        app.setInitPrompt(initPrompt);
        // 代码生成类型暂时默认为多文件模式
        app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        // 设置用户id
        app.setUserId(currentUserLoginVo.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        boolean success = save(app);
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用增加失败");
        }
        return app.getId();
    }

    @Override
    public Boolean updateApp(UpdateAppDTO updateAppDTO, HttpServletRequest request) {
        // 判断参数是否为空
        ThrowUtils.throwIf(updateAppDTO == null, ErrorCode.PARAMS_ERROR);
        Long id = updateAppDTO.getId();
        String appName = updateAppDTO.getAppName();
        ThrowUtils.throwIf(id == null || appName == null, ErrorCode.PARAMS_ERROR);
        // 获取当前登录用户脱敏后的信息
        UserLoginVO currentUserLoginVo = userService.getCurrentUserLoginVo(request);
        ThrowUtils.throwIf(currentUserLoginVo == null, ErrorCode.NOT_LOGIN_ERROR);
        // 根据应用id查询应用信息
        App app = getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.OPERATION_ERROR, "应用不存在");
        if (!app.getUserId().equals(currentUserLoginVo.getId())) {
            // 只能修改自己的应用
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        App newApp = new App();
        newApp.setAppName(appName);
        newApp.setId(id);
        newApp.setEditTime(LocalDateTime.now());
        boolean success = updateById(newApp);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "应用更新失败");
        }
        return true;
    }

    @Override
    public Boolean deleteApp(DeleteAppDTO deleteAppDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteAppDTO == null, ErrorCode.PARAMS_ERROR);
        // 获取应用id
        Long id = deleteAppDTO.getId();
        // 获取当前登录用户的脱敏信息
        UserLoginVO currentUserLoginVo = userService.getCurrentUserLoginVo(request);
        // 判断用户是否登录
        ThrowUtils.throwIf(currentUserLoginVo == null, ErrorCode.NOT_LOGIN_ERROR);
        // 判断要删除的id是否存在
        App app = getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.OPERATION_ERROR, "应用不存在");
        if (!app.getUserId().equals(currentUserLoginVo.getId())) {
            // 只能删除自己的应用
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean success = removeById(id);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "应用删除失败");
        }
        return true;
    }

    @Override
    public AppVO getAppById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        // 查询数据库得到应用信息
        App app = getById(id);
        // 判断应用是否存在
        ThrowUtils.throwIf(app == null, ErrorCode.OPERATION_ERROR, "应用不存在");
        return getAppVoByApp(app);
    }

    @Override
    public AppVO getAppVoByApp(App app) {
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        AppVO appVo = new AppVO();
        // 整合应用信息
        BeanUtils.copyProperties(app, appVo);
        Long userId = app.getUserId();
        User user = userService.getById(userId);
        UserVO userVo = userService.getUserVoByUser(user);
        // 整合作者信息
        appVo.setUserVo(userVo);
        return appVo;
    }

    @Override
    public Page<AppVO> getAppVoPage(QueryAppDTO queryAppDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(queryAppDTO == null, ErrorCode.PARAMS_ERROR);
        int pageNum = queryAppDTO.getPageNum();
        int pageSize = queryAppDTO.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        // 拿到当前登录用户的脱敏信息
        UserLoginVO userLoginVo = userService.getCurrentUserLoginVo(request);
        ThrowUtils.throwIf(userLoginVo == null, ErrorCode.NOT_LOGIN_ERROR);
        // 只能查询当前登录用户的应用信息
        queryAppDTO.setUserId(userLoginVo.getId());
        // 根据查询请求参数获取封装的查询条件
        QueryWrapper queryWrapper = getQueryWrapper(queryAppDTO);
        Page<App> pageOfApp = this.page(Page.of(pageNum, pageSize), queryWrapper);
        // 获取分页中的App全部信息
        List<App> pageOfAppRecords = pageOfApp.getRecords();
        // 将List<App>  ->   List<AppVO>
        List<AppVO> pageOfAppVoRecords = getAppVoListByAppList(pageOfAppRecords);
        Page<AppVO> appVoPage = new Page<>(pageNum, pageSize, pageOfApp.getTotalRow());
        appVoPage.setRecords(pageOfAppVoRecords);
        appVoPage.setTotalPage(pageOfApp.getTotalPage());
        return appVoPage;
    }

    @Override
    public QueryWrapper getQueryWrapper(QueryAppDTO queryAppDTO) {
        Long id = queryAppDTO.getId();
        String appName = queryAppDTO.getAppName();
        String cover = queryAppDTO.getCover();
        String initPrompt = queryAppDTO.getInitPrompt();
        String codeGenType = queryAppDTO.getCodeGenType();
        String deployKey = queryAppDTO.getDeployKey();
        Integer priority = queryAppDTO.getPriority();
        Long userId = queryAppDTO.getUserId();
        String sortField = queryAppDTO.getSortField();
        String sortOrder = queryAppDTO.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public List<AppVO> getAppVoListByAppList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVoMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVoByUser));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVoByApp(app);
            UserVO userVO = userVoMap.get(app.getUserId());
            appVO.setUserVo(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<AppVO> getAppVoPageForGood(QueryAppDTO queryAppDTO) {
        ThrowUtils.throwIf(queryAppDTO == null, ErrorCode.PARAMS_ERROR);
        int pageNum = queryAppDTO.getPageNum();
        int pageSize = queryAppDTO.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        // 设置查询条件：精选应用
        queryAppDTO.setPriority(AppConstant.GOOD_APP_PRIORITY);
        // 根据查询请求参数获取封装的查询条件
        QueryWrapper queryWrapper = getQueryWrapper(queryAppDTO);
        Page<App> pageOfApp = this.page(Page.of(pageNum, pageSize), queryWrapper);
        // 获取分页中的App全部信息
        List<App> pageOfAppRecords = pageOfApp.getRecords();
        // 将List<App>  ->   List<AppVO>
        List<AppVO> pageOfAppVoRecords = getAppVoListByAppList(pageOfAppRecords);
        Page<AppVO> appVoPage = new Page<>(pageNum, pageSize, pageOfApp.getTotalRow());
        appVoPage.setRecords(pageOfAppVoRecords);
        appVoPage.setTotalPage(pageOfApp.getTotalPage());
        return appVoPage;
    }
}
