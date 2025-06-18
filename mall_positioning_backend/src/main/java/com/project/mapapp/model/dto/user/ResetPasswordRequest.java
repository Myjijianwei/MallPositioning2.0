package com.project.mapapp.model.dto.user;

import lombok.Data;
import java.io.Serializable;

/**
 * 重置密码请求
 */
@Data
public class ResetPasswordRequest implements Serializable {
    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String code;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String confirmPassword;

    private static final long serialVersionUID = 1L;
}