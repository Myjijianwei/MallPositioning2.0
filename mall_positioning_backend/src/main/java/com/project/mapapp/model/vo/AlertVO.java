package com.project.mapapp.model.vo;

import com.project.mapapp.model.enums.AlertStatus;
import com.project.mapapp.model.enums.AlertType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlertVO {
    private Long id;
    private String deviceId;
    private String deviceName;
    private AlertType type;
    private String message;
    private LocalDateTime triggeredAt;
    private AlertStatus status;
}
