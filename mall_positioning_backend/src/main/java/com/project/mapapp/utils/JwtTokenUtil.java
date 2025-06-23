package com.project.mapapp.utils;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSignerUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-time}")
    private long expireTime;

    @Value("${jwt.issuer}")
    private String issuer;

    public String generateToken(Long userId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("iss", issuer);
        payload.put("iat", new Date());
        payload.put("exp", new Date(System.currentTimeMillis() + expireTime));

        return JWT.create()
                .addPayloads(payload)
                .setSigner(JWTSignerUtil.hs512(secret.getBytes()))
                .sign();
    }

    public boolean validateToken(String token) {
        token = extractToken(token);
        try {
            return JWT.of(token)
                    .setSigner(JWTSignerUtil.hs512(secret.getBytes()))
                    .validate(0);
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        token = extractToken(token);
        try {
            JWT jwt = JWTUtil.parseToken(token);
            Object userId = jwt.getPayload("userId");

            if (userId instanceof Number) {
                return ((Number) userId).longValue();
            }
            return Long.parseLong(userId.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private String extractToken(String header) {
        if (header == null) return null;
        if (header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return header;
    }
}