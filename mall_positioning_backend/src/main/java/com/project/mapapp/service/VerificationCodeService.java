package com.project.mapapp.service;

/**
 * 验证码验证服务
 */
public interface VerificationCodeService {
    void validateCode(String email, String code);
    void deleteVerificationCode(String email);
}
