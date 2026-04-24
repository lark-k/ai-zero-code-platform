package com.lk.aizerocodeplatform.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/24 15:31
 * 增加应用请求
 */
@Data
public class AddAppDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 应用初始化的提示词
     */
    private String initPrompt;
}
