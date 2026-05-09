package com.lk.aizerocodeplatform.rpc;

import com.lk.aizerocodeplatform.model.dto.app.QueryAppDTO;
import com.lk.aizerocodeplatform.model.vo.app.AppVO;
import com.lk.aizerocodeplatform.model.vo.user.UserLoginVO;
import com.mybatisflex.core.paginate.Page;
import reactor.core.publisher.Flux;

public interface AppDubboService {

    AppVO getAppById(Long id);

    Page<AppVO> getAppVoPageForGood(QueryAppDTO queryAppDTO);

    Flux<String> chatToGenCode(String message, Long appId, UserLoginVO userLoginVO);

    String appDeploy(Long appId, UserLoginVO userLoginVO);

    Boolean stickToTop(Long appId);

    Boolean cancelTop(Long appId);

    String cancelDeploy(Long appId, UserLoginVO userLoginVO);
}
