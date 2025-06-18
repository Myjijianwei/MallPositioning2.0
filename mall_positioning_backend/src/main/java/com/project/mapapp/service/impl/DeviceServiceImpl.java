package com.project.mapapp.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.project.mapapp.common.ErrorCode;
import com.project.mapapp.exception.BusinessException;
import com.project.mapapp.exception.ThrowUtils;
import com.project.mapapp.model.dto.device.DeviceBindRequest;
import com.project.mapapp.model.dto.device.DeviceUpdateRequest;
import com.project.mapapp.model.entity.Device;
import com.project.mapapp.service.DeviceService;
import com.project.mapapp.mapper.DeviceMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @author jjw
 * @description 针对表【device(设备表)】的数据库操作Service实现
 * @createDate 2025-03-03 14:31:28
 */
@Service
@Slf4j
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device>
        implements DeviceService {

    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Boolean bindDevice(String deviceId, Long userId, String email) {
        ThrowUtils.throwIf(deviceId == null||userId==null, ErrorCode.PARAMS_ERROR);
        // 检查设备是否已绑定
//        Device device_isExist = deviceMapper.selectById(deviceId);
//        ThrowUtils.throwIf(device_isExist != null, ErrorCode.PARAMS_ERROR);

        //判断设备ID是否还有效
        String redisDeviceMessage = stringRedisTemplate.opsForValue().get("applyDeviceInfo:" + email);
        DeviceBindRequest redisDeviceInfo = new Gson().fromJson(redisDeviceMessage, DeviceBindRequest.class);
        if (ObjUtil.isEmpty(redisDeviceInfo) || !deviceId.equals(redisDeviceInfo.getDeviceId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        } else {
            deleteVerificationCode(email);
        }

        // 未绑定则绑定设备
        Device device = new Device();
        device.setId(deviceId);
        device.setName(redisDeviceInfo.getDeviceName());
        device.setUser_id(userId);
        device.setStatus(1); // 已绑定
        return deviceMapper.insert(device) > 0;
    }

    private void deleteVerificationCode(String email) {
        try {
            stringRedisTemplate.delete("applyDeviceInfo:" + email);
            log.info("成功删除邮箱 {} 申请的设备信息", email);
        } catch (Exception e) {
            log.error("删除邮箱 {} 申请的设备信息时出现错误", email, e);
        }
    }

    @Override
    public DeviceBindRequest generateDeviceInfo() {
        DeviceBindRequest deviceInfo = new DeviceBindRequest();
        String deviceId = UUID.randomUUID().toString();
        deviceInfo.setDeviceId(deviceId);
        deviceInfo.setDeviceName("Device_" + RandomUtil.randomString(6));
        deviceInfo.setStatus(0);
        return deviceInfo;
    }

    @Override
    public boolean updateDevice(DeviceUpdateRequest deviceUpdateRequest) {
        // 参数校验
        if (deviceUpdateRequest == null) {
            throw new IllegalArgumentException("设备更新请求不能为空");
        }

        String deviceId = deviceUpdateRequest.getId();
        String deviceName = deviceUpdateRequest.getName();
        String deviceDescription = deviceUpdateRequest.getDevice_description();

        if (StringUtils.isBlank(deviceId)) {
            throw new IllegalArgumentException("设备 ID 不能为空");
        }

        // 构建更新对象
        Device device = new Device();
        device.setName(deviceName);
        device.setDevice_description(deviceDescription);

        try {
            // 使用 Lambda 表达式构建 QueryWrapper
            QueryWrapper<Device> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Device::getId, deviceId);

            // 执行更新操作
            int updateCount = deviceMapper.update(device, queryWrapper);

            // 返回更新结果
            if (updateCount > 0) {
                log.info("设备 ID: {} 更新成功", deviceId);
                return true;
            } else {
                log.warn("设备 ID: {} 更新失败，未找到匹配的设备", deviceId);
                return false;
            }
        } catch (Exception e) {
            log.error("设备 ID: {} 更新失败，原因: {}", deviceId, e.getMessage(), e);
            throw new RuntimeException("设备更新失败，请稍后重试", e);
        }
    }

    @Override
    public boolean validateDevice(String deviceId) {
        // 检查设备ID是否存在
        Device device = deviceMapper.selectById(deviceId);
        return device != null;
    }

    @Override
    public boolean validateDeviceOwnership(String userId, String deviceId) {
        QueryWrapper<Device> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("device_id", deviceId);
        queryWrapper.eq("user_id", userId);
        Long l = deviceMapper.selectCount(queryWrapper);
        return l != null && l > 0;
    }


}




