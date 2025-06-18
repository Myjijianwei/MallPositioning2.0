package com.project.mapapp.model.dto.device;

import lombok.Data;

@Data
public class DeviceBindRequest {
    private String deviceId;
    private String deviceName;
    private Integer status;
}
