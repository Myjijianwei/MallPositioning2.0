package com.project.mapapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.mapapp.manager.WebSocketSessionManager;
import com.project.mapapp.model.dto.location.LocationReportDTO;
import com.project.mapapp.model.dto.location.LocationResponseDTO;
import com.project.mapapp.service.WebSocketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LocationWebSocketFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private WebSocketService webSocketService;

    @MockBean
    private WebSocketSessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testLocationReportTriggersWebSocketNotification() throws Exception {
        // 1. 准备测试数据
        LocationReportDTO reportDTO = new LocationReportDTO();
        reportDTO.setDeviceId("test-device-001");
        reportDTO.setLatitude(new BigDecimal("39.907295"));
        reportDTO.setLongitude(new BigDecimal("116.391311"));
        reportDTO.setAccuracy(new BigDecimal("15.3"));
        reportDTO.setGuardianId(5L);

        // 2. 调用位置上报接口
        mockMvc.perform(post("/api/location/report")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(reportDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 3. 验证WebSocket通知被调用
        verify(webSocketService, times(1))
                .notifyGuardian(eq(5L), any(LocationResponseDTO.class));
    }
}