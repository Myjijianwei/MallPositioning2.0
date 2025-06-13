package com.project.mapapp.service;

import com.project.mapapp.model.dto.device.DeviceBindRequest;
import com.project.mapapp.model.dto.device.DeviceUpdateRequest;
import com.project.mapapp.model.entity.Device;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.validation.constraints.NotBlank;

/**
* @author jjw
* @description 针对表【device(设备表)】的数据库操作Service
* @createDate 2025-03-03 14:31:28
*/
public interface DeviceService extends IService<Device> {

    Boolean bindDevice(String deviceId, Long userId,String email);

    DeviceBindRequest generateDeviceInfo();

    boolean updateDevice(DeviceUpdateRequest deviceUpdateRequest);

    boolean validateDevice(String wardDeviceId);

    boolean validateDeviceOwnership(@NotBlank String userId, @NotBlank String deviceId);
}
