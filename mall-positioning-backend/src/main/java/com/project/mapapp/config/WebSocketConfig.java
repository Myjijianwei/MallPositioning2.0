package com.project.mapapp.config;

import com.project.mapapp.websocket.GpsWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GpsWebSocketHandler gpsWebSocketHandler;

    public WebSocketConfig(GpsWebSocketHandler gpsWebSocketHandler) {
        this.gpsWebSocketHandler = gpsWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gpsWebSocketHandler, "/gps-websocket")
                .setAllowedOrigins("*") // 允许所有来源
                .setHandshakeHandler(new DefaultHandshakeHandler())
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }
}