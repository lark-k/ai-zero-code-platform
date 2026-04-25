package com.lk.aizerocodeplatform.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/25 22:30
 */
@Data
public class AppDeployDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 应用 id
     */
    private Long appId;

}

