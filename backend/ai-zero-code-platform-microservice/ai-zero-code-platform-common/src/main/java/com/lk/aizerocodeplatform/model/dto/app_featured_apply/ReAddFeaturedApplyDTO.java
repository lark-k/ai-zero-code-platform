package com.lk.aizerocodeplatform.model.dto.app_featured_apply;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/27 19:16
 * 用户重新申请精选应用请求
 */
@Data
public class ReAddFeaturedApplyDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 申请id
     */
    private Long id;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 申请理由
     */
    private String applyReason;

    /**
     * 申请状态：pending-待审核，approved-已通过，rejected-已拒绝，canceled-已撤销
     */
    private String status;


}
