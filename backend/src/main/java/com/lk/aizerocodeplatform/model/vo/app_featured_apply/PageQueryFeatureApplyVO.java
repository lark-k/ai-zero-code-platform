package com.lk.aizerocodeplatform.model.vo.app_featured_apply;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/27 11:31
 * 用户分页查询精选应用申请信息
 */
@Data
public class PageQueryFeatureApplyVO implements Serializable {
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

    /**
     * 审核备注/拒绝原因
     */
    private String reviewRemark;

    /**
     * 审核管理员id
     */
    private Long reviewUserId;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
