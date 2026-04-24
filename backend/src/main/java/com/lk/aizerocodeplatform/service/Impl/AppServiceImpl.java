package com.lk.aizerocodeplatform.service.Impl;

import com.lk.aizerocodeplatform.enums.CodeGenTypeEnum;
import com.lk.aizerocodeplatform.exception.BusinessException;
import com.lk.aizerocodeplatform.exception.ErrorCode;
import com.lk.aizerocodeplatform.exception.ThrowUtils;
import com.lk.aizerocodeplatform.model.dto.app.AddAppDTO;
import com.lk.aizerocodeplatform.model.dto.app.UpdateAppDTO;
import com.lk.aizerocodeplatform.model.vo.user.UserLoginVO;
import com.lk.aizerocodeplatform.service.UserService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.lk.aizerocodeplatform.model.entity.App;
import com.lk.aizerocodeplatform.mapper.AppMapper;
import com.lk.aizerocodeplatform.service.AppService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


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
}
