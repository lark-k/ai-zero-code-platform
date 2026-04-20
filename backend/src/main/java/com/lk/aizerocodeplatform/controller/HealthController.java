package com.lk.aizerocodeplatform.controller;

import com.lk.aizerocodeplatform.common.BaseResponse;
import com.lk.aizerocodeplatform.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/20 18:10
 */
@RestController
public class HealthController {
    @GetMapping(value = "/healthTest")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}
