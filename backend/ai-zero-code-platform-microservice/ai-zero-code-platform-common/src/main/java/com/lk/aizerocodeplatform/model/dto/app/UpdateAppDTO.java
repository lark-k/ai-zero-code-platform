package com.lk.aizerocodeplatform.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/24 16:19
 * 更新app请求
 */
@Data
public class UpdateAppDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * app的id
     */
    private Long id;
    /**
     * app名称（暂时只支持修改应用名）
     */
    private String appName;
}
