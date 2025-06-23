package com.project.mapapp.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDTO {
    private String userName;
    private String email;
    private String userAvatar;
    private String userRole;
    private String userProfile;
}