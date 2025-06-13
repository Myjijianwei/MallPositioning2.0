package com.project.mapapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.mapapp.common.ErrorCode;
import com.project.mapapp.exception.BusinessException;
import com.project.mapapp.mapper.NotificationMapper;
import com.project.mapapp.mapper.UserMapper;
import com.project.mapapp.mapper.WardMapper;
import com.project.mapapp.model.dto.application.ApplicationMessage;
import com.project.mapapp.model.entity.Application;
import com.project.mapapp.model.entity.Notification;
import com.project.mapapp.model.entity.Ward;
import com.project.mapapp.model.enums.ApplicationStatus;
import com.project.mapapp.service.ApplicationService;
import com.project.mapapp.mapper.ApplicationMapper;
import com.project.mapapp.service.NotificationService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author jjw
 * @description 针对表【application】的数据库操作Service实现
 * @createDate 2025-03-23 15:33:22
 */
@Service
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application>
        implements ApplicationService {

    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private WardMapper wardMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public Application submitApplication(String guardianId, String wardDeviceId) {
        // 保存申请到数据库
        Application application = new Application();
        application.setGuardian_id(guardianId);
        application.setWard_device_id(wardDeviceId);
        application.setStatus("PENDING");
        applicationMapper.insert(application);

        // 发送消息到 RabbitMQ
        ApplicationMessage message = new ApplicationMessage();
        message.setGuardianId(guardianId);
        message.setWardDeviceId(wardDeviceId);
        message.setStatus("PENDING");
        message.setTimestamp(LocalDateTime.now().toString());

        rabbitTemplate.convertAndSend("apply_exchange", "apply_routing_key", message);

        return application;
    }

    @Override
    public boolean confirmApplication(Long notificationId, Boolean isApproved) {
        Notification notification = notificationMapper.selectById(notificationId);
        String applicationId = notification.getApplication_id();
        // 查询申请记录
        Application application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "申请记录不存在");
        }

        // 检查申请状态是否为“待确认”，使用枚举来判断
        if (!ApplicationStatus.PENDING_CONFIRMATION.getCode().equals(application.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "申请状态不支持确认操作");
        }

        // 更新申请状态，使用枚举来设置状态
        if (isApproved) {
            application.setStatus(ApplicationStatus.APPROVED.getCode());
            notificationService.notifyGuardian(application.getGuardian_id(), "您的申请已通过", applicationId);
            notificationService.notifyWard(application.getWard_device_id(), "绑定申请已通过", applicationId);
        } else {
            application.setStatus(ApplicationStatus.REJECTED.getCode());
            notificationService.notifyGuardian(application.getGuardian_id(), "您的申请被拒绝", applicationId);
            notificationService.notifyWard(application.getWard_device_id(), "绑定申请被拒绝", applicationId);
        }

        // 更新申请记录
        int count = applicationMapper.updateById(application);
        //更新被监护人信息
        String wardDeviceId = application.getWard_device_id();
        Long wardId = userMapper.selectById(wardDeviceId).getId();
        Ward ward = new Ward();
        ward.setUserId(Long.valueOf(application.getGuardian_id()));
        ward.setId(wardId); // 这里设置正确的id值
        int count1 = wardMapper.updateById(ward);
        return count + count1 == 2;
    }

    @Override
    public Long getApplicationId(String guardianId, String wardDeviceId) {
        QueryWrapper<Application> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("guardian_id", guardianId);
        queryWrapper.eq("ward_device_id", wardDeviceId);
        return applicationMapper.selectOne(queryWrapper).getId();
    }
}




