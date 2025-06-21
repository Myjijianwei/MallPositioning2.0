package com.project.mapapp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.project.mapapp.common.ErrorCode;
import com.project.mapapp.exception.BusinessException;
import com.project.mapapp.service.VerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 验证验证码
     * @param email
     * @param code
     */
    @Override
    public void validateCode(String email, String code) {
        String redisCode = stringRedisTemplate.opsForValue().get("verificationCode:" + email);
        if (StrUtil.isEmpty(redisCode) ||!code.equals(redisCode)) {
            log.error("验证码错误，邮箱: {}", email);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        deleteVerificationCode(email);
    }

    /**
     * 删除验证码
     * @param email
     */
    @Override
    public void deleteVerificationCode(String email) {
        try {
            stringRedisTemplate.delete("verificationCode:" + email);
            log.info("成功删除邮箱 {} 的验证码", email);
        } catch (Exception e) {
            log.error("删除邮箱 {} 的验证码时出现错误", email, e);
        }
    }
}
