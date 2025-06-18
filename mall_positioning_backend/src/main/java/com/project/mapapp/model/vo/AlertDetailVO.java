package com.project.mapapp.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlertDetailVO extends AlertVO {
    private String fenceId;
    private String fenceName;
    private String coordinates;
    private LocalDateTime resolvedAt;
}
