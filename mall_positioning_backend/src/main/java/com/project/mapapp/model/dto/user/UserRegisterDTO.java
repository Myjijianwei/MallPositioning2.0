package com.project.mapapp.model.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRegisterDTO {
    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String email;
    private String code;
    private String avatarUrl;
    private String username;
    private String userRole;
}