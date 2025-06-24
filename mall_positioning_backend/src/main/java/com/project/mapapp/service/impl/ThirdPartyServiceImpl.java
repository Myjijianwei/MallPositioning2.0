package com.project.mapapp.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import com.project.mapapp.service.ThirdPartyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

@Service
@Slf4j
public class ThirdPartyServiceImpl implements ThirdPartyService {

    @Value("${thirdparty.avatar-api-url}")
    private String avatarApiUrl;
    @Value("${thirdparty.username-api-url}")
    private String usernameApiUrl;
    @Value("${thirdparty.api-id}")
    private String apiId;
    @Value("${thirdparty.api-key}")
    private String apiKey;
    @Value("${thirdparty.default-avatar-url}")
    private String defaultAvatarUrl;
    @Value("${thirdparty.default-username}")
    private String defaultUsername;

    @Override
    public String getRandomAvatar() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", apiId);
            params.put("key", apiKey);
            params.put("type", 1);
            params.put("imgtype", 2);

            String response = HttpUtil.get(avatarApiUrl, params);
            return parseResponse(response);
        } catch (Exception e) {
            log.error("获取头像失败，使用默认头像", e);
            return defaultAvatarUrl;
        }
    }

    @Override
    public String getRandomUsername() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", apiId);
            params.put("key", apiKey);

            String response = HttpUtil.get(usernameApiUrl, params);
            return parseResponse(response);
        } catch (Exception e) {
            log.error("获取随机用户名失败，使用默认用户名", e);
            return defaultUsername;
        }
    }

    private String parseResponse(String response) {
        try {
            JSONObject jsonObject = JSONUtil.parseObj(response);
            String msg = jsonObject.getStr("msg");
            // 检查是否为接口频率限制等错误提示
            if (msg == null || msg.trim().isEmpty() ||
                    msg.contains("调用频次过快") ||
                    msg.contains("请登录接口盒子") ||
                    msg.length() > 200) {
                throw new RuntimeException("第三方接口返回异常: " + response);
            }
            return msg;
        } catch (Exception e) {
            log.error("解析响应结果失败，响应内容：{}", response, e);
            throw new RuntimeException("解析响应失败");
        }
    }
}

