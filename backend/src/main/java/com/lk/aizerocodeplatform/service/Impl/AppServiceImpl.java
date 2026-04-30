package com.lk.aizerocodeplatform.service.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.lk.aizerocodeplatform.constant.AppConstant;
import com.lk.aizerocodeplatform.constant.CodeFileSaveConstant;
import com.lk.aizerocodeplatform.core.AiCodeGenFacade;
import com.lk.aizerocodeplatform.core.handler.StreamHandlerExecutor;
import com.lk.aizerocodeplatform.enums.ChatMessageTypeEnum;
import com.lk.aizerocodeplatform.enums.CodeGenTypeEnum;
import com.lk.aizerocodeplatform.exception.BusinessException;
import com.lk.aizerocodeplatform.exception.ErrorCode;
import com.lk.aizerocodeplatform.exception.ThrowUtils;
import com.lk.aizerocodeplatform.model.dto.app.*;
import com.lk.aizerocodeplatform.model.entity.User;
import com.lk.aizerocodeplatform.model.vo.app.AppVO;
import com.lk.aizerocodeplatform.model.vo.user.UserLoginVO;
import com.lk.aizerocodeplatform.model.vo.user.UserVO;
import com.lk.aizerocodeplatform.service.ChatHistoryService;
import com.lk.aizerocodeplatform.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.lk.aizerocodeplatform.model.entity.App;
import com.lk.aizerocodeplatform.mapper.AppMapper;
import com.lk.aizerocodeplatform.service.AppService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeanUtils;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.Serializable;
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
    @Resource
    private AiCodeGenFacade aiCodeGenFacade;
    @Resource
    private AppMapper appMapper;
    @Resource
    private ChatHistoryService chatHistoryService;
    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

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
        // 代码生成类型暂时默认为Vue模式
        app.setCodeGenType(CodeGenTypeEnum.VUE_PROJECT.getValue());
        // 设置用户id
        app.setUserId(currentUserLoginVo.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        app.setCover(" https://picsum.photos/320/180?t=" + RandomUtil.randomNumbers(6));
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
        String codeGenType = app.getCodeGenType();
        String deployKey = app.getDeployKey();
        if (!app.getUserId().equals(currentUserLoginVo.getId())) {
            // 只能删除自己的应用
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 未部署的文件路径
        String filePath = CodeFileSaveConstant.ROOT_PATH + File.separator + id + "_" + codeGenType;
        // 已部署的文件路径
        String deployFilePath = CodeFileSaveConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        boolean success = removeById(id);
        try {
            // 删除代码文件
            FileUtil.del(filePath);
        } catch (IORuntimeException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码文件删除失败");
        }
        if (deployKey != null) {
            try {
                // 删除已经部署的代码文件
                FileUtil.del(deployFilePath);
            } catch (IORuntimeException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署的代码文件删除失败");
            }
        }
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
    public QueryWrapper getQueryWrapperForGood(QueryAppDTO queryAppDTO) {
        Long id = queryAppDTO.getId();
        String appName = queryAppDTO.getAppName();
        String cover = queryAppDTO.getCover();
        String initPrompt = queryAppDTO.getInitPrompt();
        String codeGenType = queryAppDTO.getCodeGenType();
        String deployKey = queryAppDTO.getDeployKey();
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
                .in("priority", AppConstant.GOOD_APP_PRIORITY, AppConstant.TOP_GOOD_APP_PRIORITY)
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
        QueryWrapper queryWrapper = getQueryWrapperForGood(queryAppDTO);
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
    public Boolean deleteAppByAdmin(DeleteAppDTO deleteAppDTO) {
        ThrowUtils.throwIf(deleteAppDTO == null, ErrorCode.PARAMS_ERROR);
        // 准备删除应用的id
        Long id = deleteAppDTO.getId();
        App app = getById(id);
        String deployKey = app.getDeployKey();
        String codeGenType = app.getCodeGenType();
        // 未部署的文件路径
        String filePath = CodeFileSaveConstant.ROOT_PATH + File.separator + id + "_" + codeGenType;
        // 已部署的文件路径
        String deployFilePath = CodeFileSaveConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        boolean success = removeById(id);
        try {
            // 删除代码文件
            FileUtil.del(filePath);
        } catch (IORuntimeException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码文件删除失败");
        }
        if (deployKey != null) {
            try {
                // 删除已经部署的代码文件
                FileUtil.del(deployFilePath);
            } catch (IORuntimeException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署的代码文件删除失败");
            }
        }
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "应用删除失败");
        }
        return true;
    }

    @Override
    public Boolean updateAppByAdmin(AppAdminUpdateDTO appAdminUpdateDTO) {
        ThrowUtils.throwIf(appAdminUpdateDTO == null, ErrorCode.PARAMS_ERROR);
        Long id = appAdminUpdateDTO.getId();
        String appName = appAdminUpdateDTO.getAppName();
        String cover = appAdminUpdateDTO.getCover();
        Integer priority = appAdminUpdateDTO.getPriority();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        // 查询该id是否存在应用
        App app = getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        App newApp = new App();
        newApp.setId(id);
        newApp.setAppName(appName);
        newApp.setCover(cover);
        newApp.setPriority(priority);
        newApp.setEditTime(LocalDateTime.now());
        boolean success = updateById(newApp);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "应用更新失败");
        }
        return true;
    }

    @Override
    public Page<AppVO> getAppVoPageByAdmin(QueryAppDTO queryAppDTO) {
        ThrowUtils.throwIf(queryAppDTO == null, ErrorCode.PARAMS_ERROR);
        int pageNum = queryAppDTO.getPageNum();
        int pageSize = queryAppDTO.getPageSize();
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
    public AppVO getAppVoByAdmin(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        App app = getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        return getAppVoByApp(app);
    }

    @Override
    public Flux<String> chatToGenCode(String message, Long appId, UserLoginVO userLoginVO) {
        // 判断参数是否合法
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(userLoginVO == null, ErrorCode.NOT_LOGIN_ERROR);
        // 判断应用是否存在
        App app = getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 每个用户只能与自己的应用对话
        if (!userLoginVO.getId().equals(app.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 将用户消息保存到对话历史
        chatHistoryService.addChatHistory(appId, userLoginVO.getId(), message, ChatMessageTypeEnum.User.getValue());
        // 获取代码生成类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getByValue(codeGenType);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "代码生成类型错误");
        }
        // 调用门面类，获取与AI对话的回复内容
        Flux<String> codeContentStream = aiCodeGenFacade.generateCodeAndSaveStream(message, codeGenTypeEnum, appId);
        return streamHandlerExecutor.doExecute(codeContentStream, chatHistoryService, appId, userLoginVO, codeGenTypeEnum);
    }

    @Override
    public String appDeploy(Long appId, UserLoginVO userLoginVO) {
        // 校验参数
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR);
        // 验证登录
        ThrowUtils.throwIf(userLoginVO == null, ErrorCode.NOT_LOGIN_ERROR);
        // 查询应用信息
        App app = getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 数据库如果没有deployKey，则生成随机的6位deployKey；如果有，则使用原来的deployKey
        String deployKey = "";
        if (StrUtil.isBlank(app.getDeployKey())) {
            deployKey = RandomUtil.randomString(6);
        } else {
            deployKey = app.getDeployKey();
        }
        // 拿到代码生成类型
        String codeGenType = app.getCodeGenType();
        // 将该应用生成的代码文件夹从code_output复制到code_deploy目录下
        String sourceDir = CodeFileSaveConstant.ROOT_PATH + File.separator + appId + "_" + codeGenType;
        File sourcePath = new File(sourceDir);
        if (!sourcePath.exists()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        String targetDir = CodeFileSaveConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourcePath, new File(targetDir), true);
        } catch (IORuntimeException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败");
        }
        // 更新数据库
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setEditTime(LocalDateTime.now());
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean success = updateById(updateApp);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "部署失败，请稍后再试");
        }
        // 返回可访问的部署后的网站地址url
        return CodeFileSaveConstant.CODE_DEPLOY_HOST + "/" + deployKey;
    }

    @Override
    public Boolean stickToTop(Long appId) {
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR);
        App app = getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        Integer priority = app.getPriority();
        if (priority != 99) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只能置顶精选应用");
        }
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setEditTime(LocalDateTime.now());
        updateApp.setPriority(999);
        boolean success = updateById(updateApp);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "置顶失败");
        }
        return true;
    }

    @Override
    public Boolean cancelTop(Long appId) {
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR);
        App app = getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        Integer priority = app.getPriority();
        if (priority != 999) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只能取消置顶精选应用");
        }
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setEditTime(LocalDateTime.now());
        updateApp.setPriority(99);
        boolean success = updateById(updateApp);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "取消置顶失败");
        }
        return true;
    }

    @Override
    public String cancelDeploy(Long appId, UserLoginVO userLoginVO) {
        // 校验参数
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR);
        // 校验登录用户
        ThrowUtils.throwIf(userLoginVO == null, ErrorCode.NOT_LOGIN_ERROR);
        // 校验应用是否存在
        App app = getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        // 判断是否部署
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该应用未部署");
        }
        // 将deployKey置空
        int rows = appMapper.clearDeployInfo(appId);
        boolean success = rows > 0;
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用表更新失败");
        }
        // 删除部署的相关文件
        String deleteDir = CodeFileSaveConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.del(deleteDir);
        } catch (IORuntimeException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署文件删除失败");
        }
        return "取消部署成功";
    }

    /**
     * 由于无论是用户还是管理员删除应用时都是使用removeById方法，所以重写该方法，
     * 在该方法原有删除逻辑的基础上新增删除对话历史的逻辑。
     *
     * @param id 应用id
     * @return 是否删除成功
     */
    @Override
    public boolean removeById(@NonNull Serializable id) {
        Long appId = Long.valueOf(id.toString());
        // 删除该应用对应的对话历史数据
        chatHistoryService.deleteChatHistory(appId);
        // 执行原来的删除逻辑，根据id删除应用
        return super.removeById(id);
    }
}
