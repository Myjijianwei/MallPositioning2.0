package com.project.mapapp.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求
 */
@Data
public class UserUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 身份
     */
    private String userRole;

    /**
     * 用户邮箱
     */
    private String email;

    private String code;

    private static final long serialVersionUID = 1L;
}