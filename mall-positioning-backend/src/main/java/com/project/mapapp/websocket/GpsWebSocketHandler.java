package com.project.mapapp.websocket;

import com.project.mapapp.manager.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
@Service
public class GpsWebSocketHandler extends TextWebSocketHandler {
    private static final long HEARTBEAT_INTERVAL = 30; // 秒 (与前端对齐)
    private static final long HEARTBEAT_TIMEOUT = 40;  // 秒

    private final WebSocketSessionManager sessionManager;
    private final ScheduledExecutorService heartbeatExecutor;

    @Autowired
    public GpsWebSocketHandler(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "websocket-heartbeat");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long wardId = getWardId(session);
        String deviceId = getDeviceId(session);

        if (wardId == null || deviceId == null) {
            log.warn("连接参数缺失 - wardId: {}, deviceId: {}", wardId, deviceId);
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        log.info("WebSocket连接建立 - wardId: {}, deviceId: {}", wardId, deviceId);
        sessionManager.addSession(wardId, deviceId, session);
        startHeartbeat(wardId, deviceId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if ("heartbeat".equals(payload)) {
            log.debug("收到心跳响应 - sessionId: {}", session.getId());
            return;
        }
        log.debug("收到消息: {}", payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long wardId = getWardId(session);
        String deviceId = getDeviceId(session);

        if (wardId != null && deviceId != null) {
            log.info("WebSocket连接关闭 - wardId: {}, deviceId: {}, 状态: {}",
                    wardId, deviceId, status);
            sessionManager.removeSession(wardId, deviceId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long wardId = getWardId(session);
        String deviceId = getDeviceId(session);

        log.error("传输错误 - wardId: {}, deviceId: {}, 错误: {}",
                wardId, deviceId, exception.getMessage());

        if (wardId != null && deviceId != null) {
            sessionManager.removeSession(wardId, deviceId);
        }

        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException e) {
            log.debug("关闭会话时出错", e);
        }
    }

    private void startHeartbeat(Long wardId, String deviceId, WebSocketSession session) {
        ScheduledFuture<?> future = heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                if (session.isOpen()) {
                    // 发送Ping消息
                    session.sendMessage(new PingMessage());

                    // 发送文本心跳(兼容性)
                    session.sendMessage(new TextMessage("heartbeat"));

                    log.debug("发送心跳检测 - wardId: {}, deviceId: {}", wardId, deviceId);

                    // 设置超时检测
                    heartbeatExecutor.schedule(() -> {
                        if (session.isOpen()) {
                            log.warn("心跳超时 - 关闭连接 - wardId: {}, deviceId: {}", wardId, deviceId);
                            closeSession(session, CloseStatus.SESSION_NOT_RELIABLE);
                        }
                    }, HEARTBEAT_TIMEOUT, TimeUnit.SECONDS);
                }
            } catch (IOException e) {
                log.warn("心跳发送失败 - 关闭连接 - wardId: {}, deviceId: {}", wardId, deviceId);
                closeSession(session, CloseStatus.SESSION_NOT_RELIABLE);
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);

        sessionManager.registerHeartbeatTask(wardId, deviceId, future);
    }

    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            if (session.isOpen()) {
                session.close(status);
            }
        } catch (IOException e) {
            log.debug("关闭会话时出错", e);
        }

        Long wardId = getWardId(session);
        String deviceId = getDeviceId(session);
        if (wardId != null && deviceId != null) {
            sessionManager.removeSession(wardId, deviceId);
        }
    }

    private Long getWardId(WebSocketSession session) {
        try {
            // 将字符串数组转换为流
            Optional<String> wardIdParam = Arrays.stream(session.getUri().getQuery().split("&"))
                    .filter(param -> param.startsWith("wardId="))
                    .findFirst();

            return wardIdParam.map(param -> Long.parseLong(param.split("=")[1])).orElse(null);
        } catch (Exception e) {
            log.error("解析wardId失败", e);
            return null;
        }
    }

    private String getDeviceId(WebSocketSession session) {
        try {
            // 将字符串数组转换为流
            Optional<String> deviceIdParam = Arrays.stream(session.getUri().getQuery().split("&"))
                    .filter(param -> param.startsWith("deviceId="))
                    .findFirst();

            return deviceIdParam.map(param -> param.split("=")[1]).orElse(null);
        } catch (Exception e) {
            log.error("解析deviceId失败", e);
            return null;
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            heartbeatExecutor.shutdownNow();
            sessionManager.cleanupAll();
            log.info("WebSocket处理器已关闭");
        } catch (Exception e) {
            log.error("关闭时出错", e);
        }
    }
}