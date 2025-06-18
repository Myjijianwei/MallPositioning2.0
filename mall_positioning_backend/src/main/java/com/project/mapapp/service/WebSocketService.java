package com.project.mapapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.mapapp.manager.WebSocketSessionManager;
import com.project.mapapp.model.dto.alert.AlertMessage;
import com.project.mapapp.model.dto.location.LocationResponseDTO;
import com.project.mapapp.model.dto.websocket.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public void notifyGuardian(Long guardianId, LocationResponseDTO location) {
        // 增强日志
        log.info("准备推送消息给监护人: {}, 活跃会话数: {}",
                guardianId,
                sessionManager.getSessions(guardianId).size());

        String message;
        try {
            message = objectMapper.writeValueAsString(location);
            log.debug("序列化消息: {}", message); // 调试日志
        } catch (JsonProcessingException e) {
            log.error("序列化失败", e);
            return;
        }

        sessionManager.getSessions(guardianId).forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                    log.debug("消息已发送到会话: {}", session.getId());
                } else {
                    log.warn("会话已关闭: {}", session.getId());
                }
            } catch (IOException e) {
                log.error("发送失败，移除会话: {}", session.getId(), e);
                sessionManager.removeSession(guardianId, location.getDeviceId());
            }
        });
    }

    // 新增警报推送方法
    public void pushAlert(Long guardianId, AlertMessage alert) {
        try {
            WebSocketMessage<AlertMessage> message = new WebSocketMessage<>(
                    "ALERT",
                    alert
            );
            String jsonMessage = objectMapper.writeValueAsString(message);

            sessionManager.getSessions(guardianId).forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(jsonMessage));
                    }
                } catch (IOException e) {
                    log.error("警报推送失败: {}", e.getMessage());
                }
            });
        } catch (JsonProcessingException e) {
            log.error("序列化警报消息失败: {}", e.getMessage());
        }
    }
}