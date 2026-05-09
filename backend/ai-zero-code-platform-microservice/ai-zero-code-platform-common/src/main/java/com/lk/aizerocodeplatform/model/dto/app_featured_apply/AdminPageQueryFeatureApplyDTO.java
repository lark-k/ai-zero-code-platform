package com.lk.aizerocodeplatform.model.dto.app_featured_apply;

import com.lk.aizerocodeplatform.common.PageRequest;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/27 16:24
 */
@Data
public class AdminPageQueryFeatureApplyDTO extends PageRequest implements Serializable {
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
     * 申请人id
     */
    private Long userId;

    /**
     * 申请理由
     */
    private String applyReason;

    /**
     * 申请状态：pending-待审核，approved-已通过，rejected-已拒绝，canceled-已撤销
     */
    private String status;

    /**
     * 审核备注/拒绝原因
     */
    private String reviewRemark;

    /**
     * 审核管理员id
     */
    private Long reviewUserId;

}
