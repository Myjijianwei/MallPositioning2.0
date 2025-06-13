package com.project.mapapp.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.project.mapapp.model.enums.AlertLevel;
import com.project.mapapp.model.enums.AlertStatus;
import com.project.mapapp.model.enums.AlertType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName(value ="alert")
@Data
public class Alert {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String device_id;
    private Long fence_id;

    private AlertType type;
    private AlertLevel level;
    private String message;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;

    @TableField("triggered_at")
    private LocalDateTime triggeredAt;

    @TableField("resolved_at")
    private LocalDateTime resolvedAt;

    private AlertStatus status;

    @TableField(exist = false)
    private String deviceName;

    @TableField(exist = false)
    private String fenceName;
}