package com.lk.aizerocodeplatform.rpc.Impl;

import com.lk.aizerocodeplatform.model.dto.app.QueryAppDTO;
import com.lk.aizerocodeplatform.model.vo.app.AppVO;
import com.lk.aizerocodeplatform.model.vo.user.UserLoginVO;
import com.lk.aizerocodeplatform.rpc.AppDubboService;
import com.lk.aizerocodeplatform.service.AppService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import reactor.core.publisher.Flux;

@DubboService
public class AppDubboServiceImpl implements AppDubboService {

    @Resource
    private AppService appService;

    @Override
    public AppVO getAppById(Long id) {
        return appService.getAppById(id);
    }

    @Override
    public Page<AppVO> getAppVoPageForGood(QueryAppDTO queryAppDTO) {
        return appService.getAppVoPageForGood(queryAppDTO);
    }

    @Override
    public Flux<String> chatToGenCode(String message, Long appId, UserLoginVO userLoginVO) {
        return appService.chatToGenCode(message, appId, userLoginVO);
    }

    @Override
    public String appDeploy(Long appId, UserLoginVO userLoginVO) {
        return appService.appDeploy(appId, userLoginVO);
    }

    @Override
    public Boolean stickToTop(Long appId) {
        return appService.stickToTop(appId);
    }

    @Override
    public Boolean cancelTop(Long appId) {
        return appService.cancelTop(appId);
    }

    @Override
    public String cancelDeploy(Long appId, UserLoginVO userLoginVO) {
        return appService.cancelDeploy(appId, userLoginVO);
    }
}
