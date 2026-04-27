package com.lk.aizerocodeplatform.model.dto.app_featured_apply;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/27 11:16
 * 更新精选应用请求信息
 */
@Data
public class UpdateFeaturedApplyDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 申请id
     */
    private Long id;

    /**
     * 申请理由
     */
    private String applyReason;

}
