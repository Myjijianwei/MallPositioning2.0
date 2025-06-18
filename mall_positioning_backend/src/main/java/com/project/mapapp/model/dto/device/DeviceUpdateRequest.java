package com.project.mapapp.model.dto.device;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class DeviceUpdateRequest {
    /**
     * 设备ID
     */
    @TableId
    private String id;

    /**
     * 设备名称
     */
    private String name;

    /**
     * 设备描述信息，用于区分不同被监护人的设备
     */
    private String device_description;
}
