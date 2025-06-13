package com.project.mapapp.service.impl;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.mapapp.mapper.DeviceMapper;
import com.project.mapapp.model.entity.Device;
import com.project.mapapp.model.entity.Notification;
import com.project.mapapp.service.NotificationService;
import com.project.mapapp.mapper.NotificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
* @author jjw
* @description 针对表【notification(通知表)】的数据库操作Service实现
* @createDate 2025-03-03 14:31:28
*/
@Slf4j
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    private final DeviceMapper deviceMapper;
    private final NotificationMapper notificationMapper;


    public NotificationServiceImpl(DeviceMapper deviceMapper, NotificationMapper notificationMapper) {
        this.deviceMapper = deviceMapper;
        this.notificationMapper = notificationMapper;
    }

    @Override
    public void notifyGuardian(String guardianId, String message,String applicationId) {
        // 创建通知
        Notification notification = new Notification();
        notification.setUser_id(guardianId);
        notification.setMessage(message);
        notification.setIs_read(0);
        notification.setApplication_id(applicationId);

        // 保存通知到数据库
        this.save(notification);
        log.info("站内通知已发送：监护人ID={}, 消息内容={}", guardianId, message);
    }

    @Override
    public void notifyWard(String wardDeviceId, String message,String applicationId) {
        // 根据设备ID查询被监护人用户ID
        QueryWrapper<Device> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", wardDeviceId);
        Device device = deviceMapper.selectOne(queryWrapper);

        if (device == null) {
            log.error("未找到对应的设备记录：设备ID={}", wardDeviceId);
            return;
        }

        // 创建通知
        Notification notification = new Notification();
        notification.setUser_id(String.valueOf(device.getUser_id()));
        notification.setMessage(message);
        notification.setIs_read(0);
        notification.setApplication_id(applicationId);


        // 保存通知到数据库
        this.save(notification);
        log.info("站内通知已发送：被监护人ID={}, 消息内容={}", device.getUser_id(), message);
    }

    @Override
    public void notifyAdmin(String message,String applicationId) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setIs_read(0);
        notification.setApplication_id(applicationId);
        this.save(notification);
        log.info("站内通知已发送：消息内容={}",  message);

    }
}




