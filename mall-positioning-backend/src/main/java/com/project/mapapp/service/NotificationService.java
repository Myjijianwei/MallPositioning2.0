package com.project.mapapp.service;

import com.project.mapapp.model.entity.Notification;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author jjw
* @description 针对表【notification(通知表)】的数据库操作Service
* @createDate 2025-03-03 14:31:28
*/
public interface NotificationService extends IService<Notification> {

    void notifyGuardian(String guardianId, String message,String applicationId);
    void notifyWard(String wardDeviceId, String message,String applicationId);
    void notifyAdmin(String message,String applicationId);
}
