package com.project.mapapp.manager;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

@Service
public class WebSocketSessionManager {
    private final ConcurrentMap<Long, ConcurrentMap<String, WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ScheduledFuture<?>> heartbeatTasks = new ConcurrentHashMap<>();

    public void addSession(Long guardianId, String deviceId, WebSocketSession session) {
        sessions.computeIfAbsent(guardianId, k -> new ConcurrentHashMap<>())
                .compute(deviceId, (k, oldSession) -> {
                    if (oldSession != null && oldSession.isOpen()) {
                        try {
                            oldSession.close();
                        } catch (IOException e) {
                            // Ignore close exception
                        }
                    }
                    return session;
                });
    }

    public void removeSession(Long guardianId, String deviceId) {
        if (guardianId == null || deviceId == null) return;

        ConcurrentMap<String, WebSocketSession> deviceSessions = sessions.get(guardianId);
        if (deviceSessions != null) {
            WebSocketSession session = deviceSessions.remove(deviceId);
            if (session != null && session.isOpen()) {
                try {
                    session.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if (deviceSessions.isEmpty()) {
                sessions.remove(guardianId);
            }
        }

        // Cancel heartbeat task
        String taskKey = generateTaskKey(guardianId, deviceId);
        ScheduledFuture<?> task = heartbeatTasks.remove(taskKey);
        if (task != null) {
            task.cancel(false);
        }
    }

    public List<WebSocketSession> getSessions(Long guardianId) {
        if (guardianId == null) return Collections.emptyList();

        ConcurrentMap<String, WebSocketSession> deviceSessions = sessions.get(guardianId);
        return deviceSessions != null ?
                new CopyOnWriteArrayList<>(deviceSessions.values()) :
                Collections.emptyList();
    }

    public void registerHeartbeatTask(Long guardianId, String deviceId, ScheduledFuture<?> task) {
        if (guardianId == null || deviceId == null || task == null) return;
        heartbeatTasks.put(generateTaskKey(guardianId, deviceId), task);
    }

    private String generateTaskKey(Long guardianId, String deviceId) {
        return guardianId + ":" + deviceId;
    }

    public void cleanupAll() {
        sessions.forEach((guardianId, deviceSessions) -> {
            deviceSessions.forEach((deviceId, session) -> {
                if (session.isOpen()) {
                    try {
                        session.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            });
        });
        sessions.clear();

        heartbeatTasks.values().forEach(task -> task.cancel(false));
        heartbeatTasks.clear();
    }

    // 设备会话映射
    private final ConcurrentMap<String, WebSocketSession> deviceSessions = new ConcurrentHashMap<>();
    // 监护人会话映射
    private final ConcurrentMap<Long, WebSocketSession> guardianSessions = new ConcurrentHashMap<>();

    public void addDeviceSession(String deviceId, WebSocketSession session) {
        deviceSessions.put(deviceId, session);
    }

    public WebSocketSession getDeviceSession(String deviceId) {
        return deviceSessions.get(deviceId);
    }

    public void addGuardianSession(Long guardianId, WebSocketSession session) {
        guardianSessions.put(guardianId, session);
    }
}