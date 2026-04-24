package com.lk.aizerocodeplatform.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/24 23:16
 */
@Data
public class AppAdminUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 优先级
     */
    private Integer priority;

}

