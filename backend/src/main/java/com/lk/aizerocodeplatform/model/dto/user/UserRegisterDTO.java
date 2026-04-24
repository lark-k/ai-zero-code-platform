package com.lk.aizerocodeplatform.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/21 22:06
 * 用户注册DTO
 */
@Data
public class UserRegisterDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String userAccount;
    private String userPassword;
    private String confirmPassword;
}
