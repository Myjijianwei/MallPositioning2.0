# 软件著作权源程序文档
**生成时间**: 2025-04-03 15:47:50

## 文件: UserService.java
**路径**: `src/main/java/com/project/mapapp/service/UserService.java`
```java
package com.project.mapapp.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.project.mapapp.model.dto.user.UserUpdateRequest;
import com.project.mapapp.model.entity.User;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务
 * @author jjw
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-01-17 09:59:13
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword,String email,String code,String avatarUrl,String username,String userRole);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    User loginByEmail(String email, String code, HttpServletRequest request);

    /**
     * 重置密码
     * @param email 邮箱
     * @param code 验证码
     * @param newPassword 新密码
     * @param confirmPassword 确认密码
     * @return 是否成功
     */
    boolean resetPassword(String email, String code, String newPassword, String confirmPassword);
    boolean updateUser(UserUpdateRequest userUpdateRequest);
    void validateEmailAndCode(UserUpdateRequest userUpdateRequest, User currentUser);
}
```

## 文件: DeviceService.java
**路径**: `src/main/java/com/project/mapapp/service/DeviceService.java`
```java
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
```

## 文件: EmailTaskService.java
**路径**: `src/main/java/com/project/mapapp/service/EmailTaskService.java`
```java
package com.project.mapapp.service;

import com.google.gson.Gson;
import com.project.mapapp.model.dto.device.DeviceBindRequest;
import com.project.mapapp.model.entity.Device;
import com.project.mapapp.model.entity.EmailTask;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class EmailTaskService {
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    public EmailTaskService(StringRedisTemplate redisTemplate, JavaMailSender mailSender) {
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
    }

    public void addEmailTask(String to, String subject, String body, EmailTask.EmailType emailType) {
        EmailTask task = new EmailTask(to, subject, body, emailType);
        redisTemplate.opsForList().rightPush("emailQueue", new Gson().toJson(task));
    }

    @Scheduled(fixedRate = 1000)
    public void processEmailTasks() {
        while (true) {
            String taskJson = redisTemplate.opsForList().leftPop("emailQueue");
            if (taskJson == null) break;
            EmailTask task = new Gson().fromJson(taskJson, EmailTask.class);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(task.getTo());
            message.setSubject(task.getSubject());
            if (task.getEmailType() == EmailTask.EmailType.VERIFICATION_CODE) {
                message.setText(task.getBody());
            } else if (task.getEmailType() == EmailTask.EmailType.DEVICE_INFO) {
                DeviceBindRequest deviceInfo = new Gson().fromJson(task.getBody(), DeviceBindRequest.class);
                String statusText = deviceInfo.getStatus() == 0 ? "未绑定" : "已绑定";
                String deviceMessage = "尊敬的用户：您好，欢迎使用防走失监护系统！\n您申请的设备信息如下：\n" +
                        "设备 ID：" + deviceInfo.getDeviceId() + "\n" +
                        "设备名称：" + deviceInfo.getDeviceName() + "\n" +
                        "状态：" + statusText + "\n" +
                        "请于5分钟之内完成设备绑定操作，否则设备Id失效";
                message.setText(deviceMessage);
            }
            mailSender.send(message);
        }
    }
}```

## 文件: GuardianWardRelationService.java
**路径**: `src/main/java/com/project/mapapp/service/GuardianWardRelationService.java`
```java
package com.project.mapapp.service;

import com.project.mapapp.model.entity.GuardianWardRelation;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author jjw
* @description 针对表【guardian_ward_relation(监护人与被监护人关系表)】的数据库操作Service
* @createDate 2025-03-22 10:37:44
*/
public interface GuardianWardRelationService extends IService<GuardianWardRelation> {

}
```

## 文件: GeoFenceService.java
**路径**: `src/main/java/com/project/mapapp/service/GeoFenceService.java`
```java
package com.project.mapapp.service;

import com.project.mapapp.common.BaseResponse;
import com.project.mapapp.model.dto.geofence.GeoFenceCreateRequest;
import com.project.mapapp.model.dto.geofence.GeoFenceUpdateRequest;
import com.project.mapapp.model.dto.location.LocationResponseDTO;
import com.project.mapapp.model.entity.GeoFence;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.validation.Valid;
import java.util.List;

/**
* @author jjw
* @description 针对表【geo_fence(电子围栏表)】的数据库操作Service
* @createDate 2025-03-03 14:31:28
*/
public interface GeoFenceService extends IService<GeoFence> {

    Boolean createGeoFence(GeoFenceCreateRequest getGeoFenceCreateRequest);

    List<GeoFence> listFences(String deviceId, Long id);

    boolean deleteGeoFence(Long id, Long id1);

    boolean updateGeoFence(@Valid GeoFenceUpdateRequest updateRequest, Long id);

    void checkLocation(LocationResponseDTO location);


}
```

## 文件: WebSocketService.java
**路径**: `src/main/java/com/project/mapapp/service/WebSocketService.java`
```java
package com.project.mapapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.mapapp.manager.WebSocketSessionManager;
import com.project.mapapp.model.dto.alert.AlertMessage;
import com.project.mapapp.model.dto.location.LocationResponseDTO;
import com.project.mapapp.model.dto.websocket.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public void notifyGuardian(Long guardianId, LocationResponseDTO location) {
        // 增强日志
        log.info("准备推送消息给监护人: {}, 活跃会话数: {}",
                guardianId,
                sessionManager.getSessions(guardianId).size());

        String message;
        try {
            message = objectMapper.writeValueAsString(location);
            log.debug("序列化消息: {}", message); // 调试日志
        } catch (JsonProcessingException e) {
            log.error("序列化失败", e);
            return;
        }

        sessionManager.getSessions(guardianId).forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                    log.debug("消息已发送到会话: {}", session.getId());
                } else {
                    log.warn("会话已关闭: {}", session.getId());
                }
            } catch (IOException e) {
                log.error("发送失败，移除会话: {}", session.getId(), e);
                sessionManager.removeSession(guardianId, location.getDeviceId());
            }
        });
    }

    // 新增警报推送方法
    public void pushAlert(Long guardianId, AlertMessage alert) {
        try {
            WebSocketMessage<AlertMessage> message = new WebSocketMessage<>(
                    "ALERT",
                    alert
            );
            String jsonMessage = objectMapper.writeValueAsString(message);

            sessionManager.getSessions(guardianId).forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(jsonMessage));
                    }
                } catch (IOException e) {
                    log.error("警报推送失败: {}", e.getMessage());
                }
            });
        } catch (JsonProcessingException e) {
            log.error("序列化警报消息失败: {}", e.getMessage());
        }
    }
}```

## 文件: WardService.java
**路径**: `src/main/java/com/project/mapapp/service/WardService.java`
```java
package com.project.mapapp.service;

import com.project.mapapp.model.entity.Ward;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author jjw
* @description 针对表【ward(被监护人信息表)】的数据库操作Service
* @createDate 2025-03-22 12:57:23
*/
public interface WardService extends IService<Ward> {

}
```

## 文件: SystemLogService.java
**路径**: `src/main/java/com/project/mapapp/service/SystemLogService.java`
```java
package com.project.mapapp.service;

import com.project.mapapp.model.entity.SystemLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author jjw
* @description 针对表【system_log(系统日志表)】的数据库操作Service
* @createDate 2025-03-03 14:31:28
*/
public interface SystemLogService extends IService<SystemLog> {

}
```

## 文件: ApplicationService.java
**路径**: `src/main/java/com/project/mapapp/service/ApplicationService.java`
```java
package com.project.mapapp.service;

import com.project.mapapp.model.entity.Application;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author jjw
* @description 针对表【application】的数据库操作Service
* @createDate 2025-03-23 15:33:22
*/
public interface ApplicationService extends IService<Application> {

    Application submitApplication(String guardianId, String wardDeviceId);

    boolean confirmApplication(Long applicationId, Boolean isApproved);

    Long getApplicationId(String guardianId, String wardDeviceId);

}
```

## 文件: OssService.java
**路径**: `src/main/java/com/project/mapapp/service/OssService.java`
```java
package com.project.mapapp.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssService {
    String uploadFileAvatar(MultipartFile file);
}
```

## 文件: LocationDataService.java
**路径**: `src/main/java/com/project/mapapp/service/LocationDataService.java`
```java
package com.project.mapapp.service;

import com.project.mapapp.model.dto.location.LocationResponseDTO;
import com.project.mapapp.model.entity.LocationData;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
* @author jjw
* @description 针对表【location_data_test(位置数据表)】的数据库操作Service
* @createDate 2025-03-25 09:36:34
*/
public interface LocationDataService extends IService<LocationData> {
    boolean processLocation(
            String deviceId,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal accuracy,
            Long guardianId
            );

    LocationResponseDTO getLatestLocation(String deviceId, Long guardianId);

    List<LocationData> queryHistory(String deviceId, LocalDateTime start, LocalDateTime end);
}
```

## 文件: WifiFingerprintService.java
**路径**: `src/main/java/com/project/mapapp/service/WifiFingerprintService.java`
```java
package com.project.mapapp.service;

import com.project.mapapp.model.entity.WifiFingerprint;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author jjw
* @description 针对表【wifi_fingerprint(WIFI指纹表)】的数据库操作Service
* @createDate 2025-03-03 14:31:28
*/
public interface WifiFingerprintService extends IService<WifiFingerprint> {

}
```

## 文件: DeviceServiceImpl.java
**路径**: `src/main/java/com/project/mapapp/service/impl/DeviceServiceImpl.java`
```java
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




```

## 文件: ApplicationServiceImpl.java
**路径**: `src/main/java/com/project/mapapp/service/impl/ApplicationServiceImpl.java`
```java
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




```

## 文件: GeoFenceServiceImpl.java
**路径**: `src/main/java/com/project/mapapp/service/impl/GeoFenceServiceImpl.java`
```java
package com.project.mapapp.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.mapapp.common.ErrorCode;
import com.project.mapapp.exception.BusinessException;
import com.project.mapapp.exception.ThrowUtils;
import com.project.mapapp.mapper.DeviceMapper;
import com.project.mapapp.mapper.LocationDataMapper;
import com.project.mapapp.model.dto.alert.AlertMessage;
import com.project.mapapp.model.dto.geofence.GeoFenceCreateRequest;
import com.project.mapapp.model.dto.geofence.GeoFenceUpdateRequest;
import com.project.mapapp.model.dto.location.LocationResponseDTO;
import com.project.mapapp.model.entity.Alert;
import com.project.mapapp.model.entity.GeoFence;
import com.project.mapapp.mapper.GeoFenceMapper;
import com.project.mapapp.model.entity.LocationData;
import com.project.mapapp.model.enums.AlertType;
import com.project.mapapp.service.AlertService;
import com.project.mapapp.service.DeviceService;
import com.project.mapapp.service.GeoFenceService;
import com.project.mapapp.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoFenceServiceImpl extends ServiceImpl<GeoFenceMapper, GeoFence>
        implements GeoFenceService {

    private final GeoFenceMapper geoFenceMapper;
    private final AlertService alertService;
    private final WebSocketService webSocketService;
    private final DeviceService deviceService;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final LocationDataMapper locationDataMapper;
    private final DeviceMapper deviceMapper;

    @Override
    public Boolean createGeoFence(GeoFenceCreateRequest request) {
        // 参数校验
        ThrowUtils.throwIf(Objects.isNull(request), ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(CollectionUtils.isEmpty(request.getCoordinates()) ||
                request.getCoordinates().size() < 3, ErrorCode.PARAMS_ERROR, "至少需要3个坐标点");

        // 构建围栏实体
        GeoFence fence = new GeoFence();
        fence.setUser_id(request.getUserId());
        fence.setDevice_id(request.getDeviceId());
        fence.setName(request.getName());
        fence.setCoordinates(JSON.toJSONString(request.getCoordinates()));

        // 保存到数据库
        return this.save(fence);
    }

    @Override
    public List<GeoFence> listFences(String deviceId, Long userId) {
        // 参数校验
        ThrowUtils.throwIf(deviceId == null || userId == null,
                ErrorCode.PARAMS_ERROR, "设备ID和用户ID不能为空");

        // 构建查询条件
        QueryWrapper<GeoFence> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("device_id", deviceId)
                .eq("user_id", userId)
                .orderByDesc("created_at");

        // 执行查询
        return geoFenceMapper.selectList(queryWrapper);
    }

    @Override
    public boolean deleteGeoFence(Long fenceId, Long userId) {
        // 参数校验
        ThrowUtils.throwIf(fenceId == null || userId == null,
                ErrorCode.PARAMS_ERROR, "围栏ID和用户ID不能为空");

        // 检查围栏是否存在及权限
        GeoFence fence = this.getById(fenceId);
        ThrowUtils.throwIf(fence == null, ErrorCode.NOT_FOUND_ERROR, "围栏不存在");

        // 执行删除
        return this.removeById(fenceId);
    }

    @Override
    public boolean updateGeoFence(GeoFenceUpdateRequest updateRequest, Long userId) {
        // 参数校验
        ThrowUtils.throwIf(updateRequest == null || updateRequest.getId() == null || userId == null,
                ErrorCode.PARAMS_ERROR, "参数不能为空");
        if (updateRequest.getCoordinates() != null) {
            ThrowUtils.throwIf(updateRequest.getCoordinates().size() < 3,
                    ErrorCode.PARAMS_ERROR, "至少需要3个坐标点");
        }

        // 查询现有围栏
        GeoFence existingFence = this.getById(updateRequest.getId());
        ThrowUtils.throwIf(existingFence == null,
                ErrorCode.NOT_FOUND_ERROR, "围栏不存在");

        // 更新字段
        boolean needUpdate = false;
        if (updateRequest.getName() != null && !updateRequest.getName().equals(existingFence.getName())) {
            existingFence.setName(updateRequest.getName());
            needUpdate = true;
        }
        if (updateRequest.getCoordinates() != null) {
            String newCoordinates = JSON.toJSONString(updateRequest.getCoordinates());
            if (!newCoordinates.equals(existingFence.getCoordinates())) {
                existingFence.setCoordinates(newCoordinates);
                needUpdate = true;
            }
        }

        // 执行更新
        return !needUpdate || this.updateById(existingFence);
    }

    @Override
    public void checkLocation(LocationResponseDTO location) {
        List<GeoFence> fences = geoFenceMapper.selectByDeviceId(location.getDeviceId());
        if (CollectionUtils.isEmpty(fences)) return;

        try {
            Coordinate coord = new Coordinate(
                    location.getLongitude().doubleValue(),
                    location.getLatitude().doubleValue()
            );
            Point point = geometryFactory.createPoint(coord);

            for (GeoFence fence : fences) {
                Polygon polygon = parseCoordinates((String) fence.getCoordinates());
                if (!polygon.contains(point)) {
                    handleFenceBreach(fence, location);
                }
            }
        } catch (Exception e) {
            log.error("地理围栏校验失败", e);
        }
    }

    private Polygon parseCoordinates(String coordinatesJson) {
        try {
            List<List<Double>> coordinates = JSON.parseObject(
                    coordinatesJson,
                    new TypeReference<List<List<Double>>>() {}
            );

            Coordinate[] coords = coordinates.stream()
                    .map(p -> new Coordinate(p.get(0), p.get(1)))
                    .toArray(Coordinate[]::new);

            // 闭合多边形
            if (coords.length > 0 && !coords[0].equals(coords[coords.length-1])) {
                coords = Arrays.copyOf(coords, coords.length + 1);
                coords[coords.length - 1] = coords[0];
            }

            return geometryFactory.createPolygon(coords);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "围栏坐标格式错误");
        }
    }

    private void handleFenceBreach(GeoFence fence, LocationResponseDTO location) {
        try {
            // 1. 检查是否存在未解决的相同警报
            if (alertService.hasPendingAlert(fence.getId(), location.getDeviceId())) {
                return;
            }


            // 3. 创建新警报记录
            Alert alert = new Alert();
            alert.setDevice_id(location.getDeviceId());
            alert.setFence_id(fence.getId());
            alert.setType(AlertType.valueOf(AlertType.GEO_FENCE.name()));
            alert.setMessage(String.format("设备越出围栏%s", fence.getName()));
            alert.setLatitude(location.getLatitude());
            alert.setLongitude(location.getLongitude());


            // 4. 保存警报（在事务中）
            alertService.save(alert);

            // 5. 获取监护人ID
            Long guardianId = getGuardianId(location.getDeviceId());
            if (guardianId == null) {
                log.warn("未找到设备关联的监护人: {}", location.getDeviceId());
                return;
            }

            String deviceName = deviceMapper.selectById(location.getDeviceId()).getName();

            // 6. 发送WebSocket通知
            webSocketService.pushAlert(
                    guardianId,
                    new AlertMessage(
                            AlertType.GEO_FENCE,
                            "围栏报警",
                            String.format("%s越出%s围栏",
                                    deviceName,
                                    fence.getName()),
                            location.getLongitude(),
                            location.getLatitude(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    )
            );
        } catch (Exception e) {
            log.error("处理围栏越界异常: deviceId={}, fenceId={}",
                    location.getDeviceId(), fence.getId(), e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"处理围栏越界失败");
        }
    }

    private Long getGuardianId(String deviceId) {
        QueryWrapper<LocationData> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("guardian_id")
                .eq("device_id", deviceId)
                .orderByDesc("create_time")
                .last("LIMIT 1");

        LocationData locationData = locationDataMapper.selectOne(queryWrapper);
        return locationData != null ? locationData.getGuardian_id() : null;
    }
}```

## 文件: OssServiceImpl.java
**路径**: `src/main/java/com/project/mapapp/service/impl/OssServiceImpl.java`
```java
package com.project.mapapp.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.project.mapapp.service.OssService;
import com.project.mapapp.utils.ConstantPropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;


@Service
public class OssServiceImpl implements OssService {

    private static final Logger logger = LoggerFactory.getLogger(OssServiceImpl.class);
    // 允许的文件类型
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif");

    @Override
    public String uploadFileAvatar(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            logger.error("上传的文件为空，无法进行上传操作。");
            return null;
        }

        // 检查文件类型
        if (!ALLOWED_FILE_TYPES.contains(file.getContentType())) {
            logger.error("不支持的文件类型: {}", file.getContentType());
            return null;
        }

        // 工具类获取值
        String endpoint = ConstantPropertiesUtils.getEND_POINT();
        String accessKeyId = ConstantPropertiesUtils.getACCESS_KEY_ID();
        String accessKeySecret = ConstantPropertiesUtils.getACCESS_KEY_SECRET();
        String bucketName = ConstantPropertiesUtils.getBUCKET_NAME();

        OSS ossClient = null;
        InputStream inputStream = null;
        try {
            // 创建 OSS 实例
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            // 获取上传文件输入流
            inputStream = file.getInputStream();
            // 获取文件名称
            String fileName = file.getOriginalFilename();

            // 生成唯一文件名，这里使用雪花算法，避免UUID可能存在的字符问题
            String uniqueId = IdWorker.getIdStr();
            if (StrUtil.isNotEmpty(fileName)) {
                int dotIndex = fileName.lastIndexOf(".");
                if (dotIndex != -1) {
                    String fileSuffix = fileName.substring(dotIndex);
                    fileName = uniqueId + fileSuffix;
                } else {
                    fileName = uniqueId + fileName;
                }
            } else {
                fileName = uniqueId;
            }

            // 把文件按照日期进行分类
            String datePath = new DateTime().toString("yyyy/MM/dd");
            fileName = datePath + "/" + fileName;

            // 调用 oss 方法实现上传
            ossClient.putObject(bucketName, fileName, inputStream);
            logger.info("文件上传成功，文件路径: {}", fileName);

            // 手动拼接上传到阿里云 oss 的路径
            return "https://" + bucketName + "." + endpoint + "/" + fileName;
        } catch (IOException e) {
            logger.error("读取文件输入流时发生错误", e);
        } catch (Exception e) {
            logger.error("上传文件到 OSS 时发生错误", e);
        } finally {
            // 关闭输入流
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("关闭文件输入流时发生错误", e);
                }
            }
            // 关闭 OSSClient
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }
}```

## 文件: AlertServiceImpl.java
**路径**: `src/main/java/com/project/mapapp/service/impl/AlertServiceImpl.java`
```java
package com.project.mapapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.mapapp.mapper.AlertMapper;
import com.project.mapapp.model.entity.Alert;
import com.project.mapapp.model.enums.AlertStatus;
import com.project.mapapp.model.enums.AlertType;
import com.project.mapapp.service.AlertService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertServiceImpl extends ServiceImpl<AlertMapper, Alert> implements AlertService {

    private final AlertMapper alertMapper;

    public AlertServiceImpl(AlertMapper alertMapper) {
        this.alertMapper = alertMapper;
    }

    @Override
    public boolean hasPendingAlert(Long fenceId, String deviceId) {
        QueryWrapper<Alert> query = new QueryWrapper<>();
        query.eq("device_id", deviceId)
                .eq("fence_id", fenceId)
                .eq("status", "PENDING")
                .last("LIMIT 1");
        return this.getOne(query) != null;
    }

    @Override
    public boolean resolveAlert(Long alertId) {
        Alert alert = new Alert();
        alert.setId(alertId);
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        return this.updateById(alert);
    }

    @Override
    public List<Alert> getDeviceAlerts(String deviceId, AlertType type, boolean onlyPending) {
        QueryWrapper<Alert> query = new QueryWrapper<>();
        query.eq("device_id", deviceId);

        if (type != null) {
            query.eq("type", type.name());
        }

        if (onlyPending) {
            query.eq("status", "PENDING");
        }

        query.orderByDesc("triggered_at");
        return this.list(query);
    }

    @Override
    public boolean createAlert(Alert alert) {
        return this.save(alert);
    }
}```

## 文件: NotificationServiceImpl.java
**路径**: `src/main/java/com/project/mapapp/service/impl/NotificationServiceImpl.java`
```java
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




```

## 文件: GuardianWardRelationServiceImpl.java
**路径**: `src/main/java/com/project/mapapp/service/impl/GuardianWardRelationServiceImpl.java`
```java
package com.project.mapapp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.mapapp.model.entity.GuardianWardRelation;
import com.project.mapapp.service.GuardianWardRelationService;
import com.project.mapapp.mapper.GuardianWardRelationMapper;
import org.springframework.stereotype.Service;

/**
* @author jjw
* @description 针对表【guardian_ward_relation(监护人与被监护人关系表)】的数据库操作Service实现
* @createDate 2025-03-22 10:37:44
*/
@Service
public class GuardianWardRelationServiceImpl extends ServiceImpl<GuardianWardRelationMapper, GuardianWardRelation>
    implements GuardianWardRelationService{

}




```

## 文件: LocationDataServiceImpl.java
**路径**: `src/main/java/com/project/mapapp/service/impl/LocationDataServiceImpl.java`
```java
package com.project.mapapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.mapapp.mapper.GeoFenceMapper;
import com.project.mapapp.model.dto.location.LocationResponseDTO;
import com.project.mapapp.model.entity.LocationData;
import com.project.mapapp.service.GeoFenceService;
import com.project.mapapp.service.LocationDataService;
import com.project.mapapp.mapper.LocationDataMapper;
import com.project.mapapp.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
* @author jjw
* @description 针对表【location_data_test(位置数据表)】的数据库操作Service实现
* @createDate 2025-03-25 09:36:34
*/
@Service
public class LocationDataServiceImpl extends ServiceImpl<LocationDataMapper, LocationData>
    implements LocationDataService {

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private LocationDataMapper locationDataTestMapper;

    @Autowired
    private GeoFenceService geoFenceService;

    /**
     * 处理位置上报
     */
    @Override
    @Transactional
    public boolean processLocation(
            String deviceId,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal accuracy,
            Long guardianId) {

        // 1. 保存到数据库
        LocationData location = new LocationData();
        location.setDevice_id(deviceId);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(accuracy);
        location.setGuardian_id(guardianId);

        int insert = locationDataTestMapper.insert(location);

        // 2. 通过WebSocket通知监护人
        LocationResponseDTO dto = convertToResponseDTO(location);
        webSocketService.notifyGuardian(guardianId, dto);
        geoFenceService.checkLocation(dto);
        return insert > 0;
    }

    /**
     * 获取最新位置
     */
    public LocationResponseDTO getLatestLocation(String deviceId, Long guardianId) {
        QueryWrapper<LocationData> query = new QueryWrapper<>();
        query.eq("device_id", deviceId)
                .eq("guardian_id", guardianId)
                .orderByDesc("create_time")
                .last("LIMIT 1");

        LocationData location = locationDataTestMapper.selectOne(query);
        return convertToResponseDTO(location);
    }

    @Override
    public List<LocationData> queryHistory(String deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<LocationData> queryWrapper = new QueryWrapper<>();

        // 设备ID条件
        queryWrapper.eq("device_id", deviceId);

        // 时间范围条件
        if (startTime != null) {
            queryWrapper.ge("create_time", startTime);
        }
        if (endTime != null) {
            queryWrapper.le("create_time", endTime);
        }

        // 按时间升序排序
        queryWrapper.orderByAsc("create_time");

        return this.list(queryWrapper);
    }

    private LocationResponseDTO convertToResponseDTO(LocationData location) {
        if (location == null) return null;

        LocationResponseDTO dto = new LocationResponseDTO();
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        dto.setAccuracy(location.getAccuracy());
        dto.setDeviceId(location.getDevice_id());
        dto.setCreateTime(String.valueOf(location.getCreate_time()));
        return dto;
    }

}




```

## 文件: UserServiceImpl.java
**路径**: `src/main/java/com/project/mapapp/service/impl/UserServiceImpl.java`
```java
package com.project.mapapp.service.impl;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.mapapp.common.ErrorCode;
import com.project.mapapp.exception.BusinessException;
import com.project.mapapp.mapper.UserMapper;
import com.project.mapapp.mapper.WardMapper;
import com.project.mapapp.model.dto.user.UserUpdateRequest;
import com.project.mapapp.model.entity.User;
import com.project.mapapp.model.entity.Ward;
import com.project.mapapp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

import static com.project.mapapp.constant.UserConstant.*;
import static org.apache.commons.lang3.StringUtils.isAnyBlank;


/**
 * 用户服务实现类
 *
 * @author jjw
 * @author jjw
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-01-17 09:59:13
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "TUTE";
    @Autowired
    private WardMapper wardMapper;

    @Transactional(rollbackFor = Exception.class)
    public long userRegister(String userAccount, String userPassword, String checkPassword, String email, String code, String avatarUrl, String username, String userRole) {
        log.info("开始用户注册流程，用户账号: {}", userAccount);
        // 校验参数
        validateParams(userAccount, userPassword, checkPassword, email, code);
        // 校验账户和邮箱是否重复
        checkDuplicate(userAccount, email);
        // 校验验证码
        validateVerificationCode(email, code);

        // 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setEmail(email);
        user.setUserAvatar(avatarUrl);
        user.setUserName(username);
        user.setUserRole(userRole);


        boolean saveResult = this.save(user);
        if (!saveResult) {
            log.error("注册失败，数据库错误，用户账号: {}", userAccount);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }

        if(userRole.equals(WARD_ROLE)){
            Ward ward = new Ward();
            ward.setId(user.getId());
            int insert = wardMapper.insert(ward);
            log.info("insert:{}", insert);
        }

        log.info("用户注册成功，用户 ID: {}", user.getId());
        return user.getId();
    }

    private void validateParams(String userAccount, String userPassword, String checkPassword, String email, String code) {
        if (isAnyBlank(userAccount, userPassword, checkPassword)) {
            log.error("用户注册参数为空，用户账号: {}", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            log.error("用户账号过短，用户账号: {}", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            log.error("用户密码过短，用户账号: {}", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            log.error("两次输入的密码不一致，用户账号: {}", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        if (!ReUtil.isMatch("^[A-Za-z0-9+_.-]+@(.+)$", email)) {
            log.error("邮箱格式不正确，用户账号: {}", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }
    }

    private void checkDuplicate(String userAccount, String email) {
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User existingUser = this.getOne(queryWrapper);
        if (existingUser != null) {
            log.error("该账号已存在，用户账号: {}", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该账号已存在");
        }

        // 邮箱不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User user_email = this.getOne(queryWrapper);
        if (user_email != null) {
            log.error("该邮箱已注册，用户账号: {}", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已注册");
        }
    }

    private void validateVerificationCode(String email, String code) {
        String redisCode = stringRedisTemplate.opsForValue().get("verificationCode:" + email);
        if (StrUtil.isEmpty(redisCode) ||!code.equals(redisCode)) {
            log.error("验证码错误，邮箱: {}", email);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        deleteVerificationCode(email);
    }



    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return user;
    }

    @Override
    public boolean updateUser(UserUpdateRequest userUpdateRequest) {
        // 1. 参数校验
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 2. 获取当前用户信息
        User currentUser = this.getById(userUpdateRequest.getId());
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        String email = userUpdateRequest.getEmail();
        String code = userUpdateRequest.getCode();

        if (StrUtil.isNotBlank(code)) {
            // 邮箱不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email", email);
            User user_email = userMapper.selectOne(queryWrapper);
            if (user_email != null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已注册");
            }

            // 判断验证码
            String redisCode = stringRedisTemplate.opsForValue().get("verificationCode:" + email);
            if (StrUtil.isEmpty(redisCode) || !code.equals(redisCode)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
            } else {
                deleteVerificationCode(email);
            }
        }


        // 4. 更新用户信息
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        return this.updateById(user);
    }

    /**
     * 校验邮箱和验证码
     *
     * @param userUpdateRequest 用户更新请求
     * @param currentUser       当前用户信息
     */
    public void validateEmailAndCode(UserUpdateRequest userUpdateRequest, User currentUser) {
        String newEmail = userUpdateRequest.getEmail();

        // 3.1 如果邮箱未修改，直接返回
        if (newEmail.equals(currentUser.getEmail())) {
            return;
        }

        // 3.2 校验邮箱格式
        if (!ReUtil.isMatch("^[A-Za-z0-9+_.-]+@(.+)$", newEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }

        // 3.3 校验邮箱是否已被注册
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", newEmail);
        User existingUser = this.getOne(queryWrapper);
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已被注册");
        }

        // 3.4 校验验证码
        String redisCode = stringRedisTemplate.opsForValue().get("verificationCode:" + newEmail);
        if (StrUtil.isEmpty(redisCode) || !userUpdateRequest.getCode().equals(redisCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        } else {
            // 验证通过后，删除 Redis 中的验证码
            stringRedisTemplate.delete("verificationCode:" + newEmail);
        }
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && ADMIN_ROLE.equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public User loginByEmail(String email, String code, HttpServletRequest request) {
        // 校验输入字段
        if (StrUtil.isEmpty(code) || StrUtil.isEmpty(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱或验证码为空");
        }
        if (!ReUtil.isMatch("^[A-Za-z0-9+_.-]+@(.+)$", email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }

        // 判断验证码
        String redisCode = stringRedisTemplate.opsForValue().get("verificationCode:" + email);
        if (StrUtil.isEmpty(redisCode) || !code.equals(redisCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        } else {
            deleteVerificationCode(email);
        }

        // 判断邮箱是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User user = userMapper.selectOne(queryWrapper);
        if (Objects.isNull(user)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱未注册");
        }

        // 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        // 登录成功
        return user;
    }

    private void deleteVerificationCode(String email) {
        try {
            stringRedisTemplate.delete("verificationCode:" + email);
            log.info("成功删除邮箱 {} 的验证码", email);
        } catch (Exception e) {
            log.error("删除邮箱 {} 的验证码时出现错误", email, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(String email, String code, String newPassword, String confirmPassword) {
        // 参数校验
        if (StringUtils.isAnyBlank(email, code, newPassword, confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }

        // 校验密码长度
        if (newPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能少于8位");
        }

        // 校验两次密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 校验验证码
        String redisCode = stringRedisTemplate.opsForValue().get("verificationCode:" + email);
        if (StringUtils.isBlank(redisCode) || !redisCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误或已过期");
        }

        // 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 加密新密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());

        // 更新密码
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setUserPassword(encryptPassword);
        boolean result = this.updateById(updateUser);

        // 删除验证码
        if (result) {
            stringRedisTemplate.delete("verificationCode:" + email);
        }

        return result;
    }
}```

## 文件: WardServiceImpl.java
**路径**: `src/main/java/com/project/mapapp/service/impl/WardServiceImpl.java`
```java
package com.project.mapapp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.mapapp.model.entity.Ward;
import com.project.mapapp.service.WardService;
import com.project.mapapp.mapper.WardMapper;
import org.springframework.stereotype.Service;

/**
* @author jjw
* @description 针对表【ward(被监护人信息表)】的数据库操作Service实现
* @createDate 2025-03-22 12:57:23
*/
@Service
public class WardServiceImpl extends ServiceImpl<WardMapper, Ward>
    implements WardService{

}




```

## 文件: MsmServiceImpl.java
**路径**: `src/main/java/com/project/mapapp/service/impl/MsmServiceImpl.java`
```java
package com.project.mapapp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.project.mapapp.service.MsmService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class MsmServiceImpl implements MsmService {

    /**
     * 发送短信
     *
     * @param map
     * @param phone
     * @return
     */
    @Override
    public boolean send(Map<String, Object> map, String phone) {
        if (StringUtils.isEmpty(phone)) return false;
//LTAI5tEkiLLMGxovkUwF3AxP
        //j4SNZrhOCgGm0wxe0KKk12SEDRv1JN
        DefaultProfile profile =
                DefaultProfile.getProfile("default", "", "");
        IAcsClient client = new DefaultAcsClient(profile);

        //设置相关固定的参数
        CommonRequest request = new CommonRequest();
        //request.setProtocol(ProtocolType.HTTPS);
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");

        //设置发送相关的参数
        request.putQueryParameter("PhoneNumbers", phone); //手机号
        request.putQueryParameter("SignName", "我的MapApp迷失守护者"); //申请阿里云 签名名称
        request.putQueryParameter("TemplateCode", "SMS_180051135"); //申请阿里云 模板code
        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(map)); //验证码数据，转换json数据传递

        try {
            //最终发送
            CommonResponse response = client.getCommonResponse(request);
            return response.getHttpResponse().isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
```

## 文件: WifiFingerprintServiceImpl.java
**路径**: `src/main/java/com/project/mapapp/service/impl/WifiFingerprintServiceImpl.java`
```java
package com.project.mapapp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.mapapp.model.entity.WifiFingerprint;
import com.project.mapapp.service.WifiFingerprintService;
import com.project.mapapp.mapper.WifiFingerprintMapper;
import org.springframework.stereotype.Service;

/**
* @author jjw
* @description 针对表【wifi_fingerprint(WIFI指纹表)】的数据库操作Service实现
* @createDate 2025-03-03 14:31:28
*/
@Service
public class WifiFingerprintServiceImpl extends ServiceImpl<WifiFingerprintMapper, WifiFingerprint>
    implements WifiFingerprintService{

}




```

## 文件: MallAreaService.java
**路径**: `src/main/java/com/project/mapapp/service/MallAreaService.java`
```java
package com.project.mapapp.service;

import com.project.mapapp.model.entity.MallArea;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author jjw
* @description 针对表【mall_area(商场区域表)】的数据库操作Service
* @createDate 2025-03-03 14:31:28
*/
public interface MallAreaService extends IService<MallArea> {

}
```

## 文件: AlertService.java
**路径**: `src/main/java/com/project/mapapp/service/AlertService.java`
```java
package com.project.mapapp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.mapapp.model.entity.Alert;
import com.project.mapapp.model.enums.AlertType;

import java.util.List;

/**
* @author jjw
* @description 针对表【alert(报警记录表)】的数据库操作Service
* @createDate 2025-03-03 14:31:28
*/
public interface AlertService extends IService<Alert> {

    /**
     * 检查是否存在未解决的围栏警报
     */
    boolean hasPendingAlert(Long fenceId, String deviceId);

    /**
     * 标记警报为已解决
     */
    boolean resolveAlert(Long alertId);

    /**
     * 获取设备警报列表
     */
    List<Alert> getDeviceAlerts(String deviceId, AlertType type, boolean onlyPending);

    /**
     * 创建新警报
     */
    boolean createAlert(Alert alert);
}
```

## 文件: NotificationService.java
**路径**: `src/main/java/com/project/mapapp/service/NotificationService.java`
```java
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
```

## 文件: MsmService.java
**路径**: `src/main/java/com/project/mapapp/service/MsmService.java`
```java
package com.project.mapapp.service;

import java.util.Map;

public interface MsmService {
    boolean send(Map<String, Object> map, String phone);
}
```

## 文件: WebSocketSessionManager.java
**路径**: `src/main/java/com/project/mapapp/manager/WebSocketSessionManager.java`
```java
package com.project.mapapp.manager;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

@Service
public class WebSocketSessionManager {
    private final ConcurrentMap<Long, ConcurrentMap<String, WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ScheduledFuture<?>> heartbeatTasks = new ConcurrentHashMap<>();

    public void addSession(Long guardianId, String deviceId, WebSocketSession session) {
        sessions.computeIfAbsent(guardianId, k -> new ConcurrentHashMap<>())
                .compute(deviceId, (k, oldSession) -> {
                    if (oldSession != null && oldSession.isOpen()) {
                        try {
                            oldSession.close();
                        } catch (IOException e) {
                            // Ignore close exception
                        }
                    }
                    return session;
                });
    }

    public void removeSession(Long guardianId, String deviceId) {
        if (guardianId == null || deviceId == null) return;

        ConcurrentMap<String, WebSocketSession> deviceSessions = sessions.get(guardianId);
        if (deviceSessions != null) {
            WebSocketSession session = deviceSessions.remove(deviceId);
            if (session != null && session.isOpen()) {
                try {
                    session.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if (deviceSessions.isEmpty()) {
                sessions.remove(guardianId);
            }
        }

        // Cancel heartbeat task
        String taskKey = generateTaskKey(guardianId, deviceId);
        ScheduledFuture<?> task = heartbeatTasks.remove(taskKey);
        if (task != null) {
            task.cancel(false);
        }
    }

    public List<WebSocketSession> getSessions(Long guardianId) {
        if (guardianId == null) return Collections.emptyList();

        ConcurrentMap<String, WebSocketSession> deviceSessions = sessions.get(guardianId);
        return deviceSessions != null ?
                new CopyOnWriteArrayList<>(deviceSessions.values()) :
                Collections.emptyList();
    }

    public void registerHeartbeatTask(Long guardianId, String deviceId, ScheduledFuture<?> task) {
        if (guardianId == null || deviceId == null || task == null) return;
        heartbeatTasks.put(generateTaskKey(guardianId, deviceId), task);
    }

    private String generateTaskKey(Long guardianId, String deviceId) {
        return guardianId + ":" + deviceId;
    }

    public void cleanupAll() {
        sessions.forEach((guardianId, deviceSessions) -> {
            deviceSessions.forEach((deviceId, session) -> {
                if (session.isOpen()) {
                    try {
                        session.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            });
        });
        sessions.clear();

        heartbeatTasks.values().forEach(task -> task.cancel(false));
        heartbeatTasks.clear();
    }

    // 设备会话映射
    private final ConcurrentMap<String, WebSocketSession> deviceSessions = new ConcurrentHashMap<>();
    // 监护人会话映射
    private final ConcurrentMap<Long, WebSocketSession> guardianSessions = new ConcurrentHashMap<>();

    public void addDeviceSession(String deviceId, WebSocketSession session) {
        deviceSessions.put(deviceId, session);
    }

    public WebSocketSession getDeviceSession(String deviceId) {
        return deviceSessions.get(deviceId);
    }

    public void addGuardianSession(Long guardianId, WebSocketSession session) {
        guardianSessions.put(guardianId, session);
    }
}```

## 文件: SpringContextUtils.java
**路径**: `src/main/java/com/project/mapapp/utils/SpringContextUtils.java`
```java
package com.project.mapapp.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring 上下文获取工具
 *
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        SpringContextUtils.applicationContext = applicationContext;
    }

    /**
     * 通过名称获取 Bean
     *
     * @param beanName
     * @return
     */
    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    /**
     * 通过 class 获取 Bean
     *
     * @param beanClass
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }

    /**
     * 通过名称和类型获取 Bean
     *
     * @param beanName
     * @param beanClass
     * @param <T>
     * @return
     */
    public static <T> T getBean(String beanName, Class<T> beanClass) {
        return applicationContext.getBean(beanName, beanClass);
    }
}```

## 文件: NetUtils.java
**路径**: `src/main/java/com/project/mapapp/utils/NetUtils.java`
```java
package com.project.mapapp.utils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

/**
 * 网络工具类
 *
 */
public class NetUtils {

    /**
     * 获取客户端 IP 地址
     *
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (ip.equals("127.0.0.1")) {
                // 根据网卡取本机配置的 IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (inet != null) {
                    ip = inet.getHostAddress();
                }
            }
        }
        // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        if (ip == null) {
            return "127.0.0.1";
        }
        return ip;
    }

}
```

## 文件: ConstantPropertiesUtils.java
**路径**: `src/main/java/com/project/mapapp/utils/ConstantPropertiesUtils.java`
```java
package com.project.mapapp.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ConstantPropertiesUtils {

    private static String END_POINT;
    private static String ACCESS_KEY_ID;
    private static String ACCESS_KEY_SECRET;
    private static String BUCKET_NAME;

    @Value("${aliyun.oss.endpoint}")
    private String endPoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @PostConstruct
    public void init() {
        END_POINT = endPoint;
        ACCESS_KEY_ID = accessKeyId;
        ACCESS_KEY_SECRET = accessKeySecret;
        BUCKET_NAME = bucketName;
    }

    public static String getEND_POINT() {
        return END_POINT;
    }

    public static String getACCESS_KEY_ID() {
        return ACCESS_KEY_ID;
    }

    public static String getACCESS_KEY_SECRET() {
        return ACCESS_KEY_SECRET;
    }

    public static String getBUCKET_NAME() {
        return BUCKET_NAME;
    }
}```

## 文件: SqlUtils.java
**路径**: `src/main/java/com/project/mapapp/utils/SqlUtils.java`
```java
package com.project.mapapp.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * SQL 工具
 *
 */
public class SqlUtils {

    /**
     * 校验排序字段是否合法（防止 SQL 注入）
     *
     * @param sortField
     * @return
     */
    public static boolean validSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }
}
```

## 文件: GpsWebSocketHandler.java
**路径**: `src/main/java/com/project/mapapp/websocket/GpsWebSocketHandler.java`
```java
package com.project.mapapp.websocket;

import com.project.mapapp.manager.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
@Service
public class GpsWebSocketHandler extends TextWebSocketHandler {
    private static final long HEARTBEAT_INTERVAL = 30; // 秒 (与前端对齐)
    private static final long HEARTBEAT_TIMEOUT = 40;  // 秒

    private final WebSocketSessionManager sessionManager;
    private final ScheduledExecutorService heartbeatExecutor;

    @Autowired
    public GpsWebSocketHandler(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "websocket-heartbeat");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long wardId = getWardId(session);
        String deviceId = getDeviceId(session);

        if (wardId == null || deviceId == null) {
            log.warn("连接参数缺失 - wardId: {}, deviceId: {}", wardId, deviceId);
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        log.info("WebSocket连接建立 - wardId: {}, deviceId: {}", wardId, deviceId);
        sessionManager.addSession(wardId, deviceId, session);
        startHeartbeat(wardId, deviceId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if ("heartbeat".equals(payload)) {
            log.debug("收到心跳响应 - sessionId: {}", session.getId());
            return;
        }
        log.debug("收到消息: {}", payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long wardId = getWardId(session);
        String deviceId = getDeviceId(session);

        if (wardId != null && deviceId != null) {
            log.info("WebSocket连接关闭 - wardId: {}, deviceId: {}, 状态: {}",
                    wardId, deviceId, status);
            sessionManager.removeSession(wardId, deviceId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long wardId = getWardId(session);
        String deviceId = getDeviceId(session);

        log.error("传输错误 - wardId: {}, deviceId: {}, 错误: {}",
                wardId, deviceId, exception.getMessage());

        if (wardId != null && deviceId != null) {
            sessionManager.removeSession(wardId, deviceId);
        }

        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException e) {
            log.debug("关闭会话时出错", e);
        }
    }

    private void startHeartbeat(Long wardId, String deviceId, WebSocketSession session) {
        ScheduledFuture<?> future = heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                if (session.isOpen()) {
                    // 发送Ping消息
                    session.sendMessage(new PingMessage());

                    // 发送文本心跳(兼容性)
                    session.sendMessage(new TextMessage("heartbeat"));

                    log.debug("发送心跳检测 - wardId: {}, deviceId: {}", wardId, deviceId);

                    // 设置超时检测
                    heartbeatExecutor.schedule(() -> {
                        if (session.isOpen()) {
                            log.warn("心跳超时 - 关闭连接 - wardId: {}, deviceId: {}", wardId, deviceId);
                            closeSession(session, CloseStatus.SESSION_NOT_RELIABLE);
                        }
                    }, HEARTBEAT_TIMEOUT, TimeUnit.SECONDS);
                }
            } catch (IOException e) {
                log.warn("心跳发送失败 - 关闭连接 - wardId: {}, deviceId: {}", wardId, deviceId);
                closeSession(session, CloseStatus.SESSION_NOT_RELIABLE);
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);

        sessionManager.registerHeartbeatTask(wardId, deviceId, future);
    }

    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            if (session.isOpen()) {
                session.close(status);
            }
        } catch (IOException e) {
            log.debug("关闭会话时出错", e);
        }

        Long wardId = getWardId(session);
        String deviceId = getDeviceId(session);
        if (wardId != null && deviceId != null) {
            sessionManager.removeSession(wardId, deviceId);
        }
    }

    private Long getWardId(WebSocketSession session) {
        try {
            // 将字符串数组转换为流
            Optional<String> wardIdParam = Arrays.stream(session.getUri().getQuery().split("&"))
                    .filter(param -> param.startsWith("wardId="))
                    .findFirst();

            return wardIdParam.map(param -> Long.parseLong(param.split("=")[1])).orElse(null);
        } catch (Exception e) {
            log.error("解析wardId失败", e);
            return null;
        }
    }

    private String getDeviceId(WebSocketSession session) {
        try {
            // 将字符串数组转换为流
            Optional<String> deviceIdParam = Arrays.stream(session.getUri().getQuery().split("&"))
                    .filter(param -> param.startsWith("deviceId="))
                    .findFirst();

            return deviceIdParam.map(param -> param.split("=")[1]).orElse(null);
        } catch (Exception e) {
            log.error("解析deviceId失败", e);
            return null;
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            heartbeatExecutor.shutdownNow();
            sessionManager.cleanupAll();
            log.info("WebSocket处理器已关闭");
        } catch (Exception e) {
            log.error("关闭时出错", e);
        }
    }
}```

## 文件: MapAppApplication.java
**路径**: `src/main/java/com/project/mapapp/MapAppApplication.java`
```java
package com.project.mapapp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication()
//@MapperScan("com.project.mapapp.mapper")
@EnableTransactionManagement
public class MapAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(MapAppApplication.class, args);
    }

}
```

