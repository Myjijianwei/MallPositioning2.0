package com.project.mapapp.model.dto.alert;

import com.project.mapapp.model.enums.AlertType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertMessage {
    private AlertType type;
    private String title;
    private String message;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String triggeredAt;
}
