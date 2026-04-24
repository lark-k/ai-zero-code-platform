package com.lk.aizerocodeplatform.controller;

import com.lk.aizerocodeplatform.common.BaseResponse;
import com.lk.aizerocodeplatform.common.ResultUtils;
import com.lk.aizerocodeplatform.model.dto.app.AddAppDTO;
import com.lk.aizerocodeplatform.model.dto.app.DeleteAppDTO;
import com.lk.aizerocodeplatform.model.dto.app.UpdateAppDTO;
import com.lk.aizerocodeplatform.model.vo.app.AppVO;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.lk.aizerocodeplatform.model.entity.App;
import com.lk.aizerocodeplatform.service.AppService;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 控制层。
 *
 * @author LK
 * @since 2026-04-24
 */
@Tag(name = "应用模块相关接口")
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Operation(summary = "增加应用")
    @PostMapping("/addApp")
    public BaseResponse<Long> addApp(@RequestBody AddAppDTO addAppDTO, HttpServletRequest request) {
        return ResultUtils.success(appService.addApp(addAppDTO, request));
    }

    @Operation(summary = "修改应用")
    @PostMapping("/updateApp")
    public BaseResponse<Boolean> updateApp(@RequestBody UpdateAppDTO updateAppDTO, HttpServletRequest request) {
        return ResultUtils.success(appService.updateApp(updateAppDTO, request));
    }

    @Operation(summary = "删除应用")
    @PostMapping("/deleteApp")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteAppDTO deleteAppDTO, HttpServletRequest request) {
        return ResultUtils.success(appService.deleteApp(deleteAppDTO, request));
    }

    @Operation(summary = "根据id查询应用信息")
    @GetMapping("/getAppById")
    public BaseResponse<AppVO> getAppVoById(Long id) {
        return ResultUtils.success(appService.getAppById(id));
    }
}
