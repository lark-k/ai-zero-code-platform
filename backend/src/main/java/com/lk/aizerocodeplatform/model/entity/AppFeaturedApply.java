package com.lk.aizerocodeplatform.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  实体类。
 *
 * @author LK
 * @since 2026-04-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("app_featured_apply")
public class AppFeaturedApply implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 申请id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 应用id
     */
    @Column("appId")
    private Long appId;

    /**
     * 申请人id
     */
    @Column("userId")
    private Long userId;

    /**
     * 申请理由
     */
    @Column("applyReason")
    private String applyReason;

    /**
     * 申请状态：pending-待审核，approved-已通过，rejected-已拒绝，canceled-已撤销
     */
    private String status;

    /**
     * 审核备注/拒绝原因
     */
    @Column("reviewRemark")
    private String reviewRemark;

    /**
     * 审核管理员id
     */
    @Column("reviewUserId")
    private Long reviewUserId;

    /**
     * 审核时间
     */
    @Column("reviewTime")
    private LocalDateTime reviewTime;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除：0-未删，1-已删
     */
    @Column(value = "isDelete",isLogicDelete = true)
    private Integer isDelete;

}
