package com.project.mapapp.config.RabbitMQ;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.project.mapapp.exception.BusinessException;
import com.project.mapapp.mapper.ApplicationMapper;
import com.project.mapapp.model.dto.application.ApplicationMessage;
import com.project.mapapp.model.entity.Application;
import com.project.mapapp.service.ApplicationService;
import com.project.mapapp.service.DeviceService;
import com.project.mapapp.service.NotificationService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class ApplicationConsumer {

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final int MAX_RETRY_COUNT = 3;

    @RabbitListener(queues = "apply_queue")
    public void receiveApplication(ApplicationMessage message,
                                   Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                   @Header(required = false, name = "x-retry-count") Integer retryCount) throws IOException {
        try {
            log.info("收到申请：监护人ID={}, 被监护人设备ID={}, 重试次数={}",
                    message.getGuardianId(), message.getWardDeviceId(), retryCount);

            // 查询申请记录
            Application application = getApplicationRecord(message);
            if (application == null) {
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 处理申请逻辑
            processApplication(message, application);

            // 手动确认消息
            channel.basicAck(deliveryTag, false);

        } catch (BusinessException e) {
            log.error("业务异常处理申请: {}", e.getMessage());
            handleBusinessException(message, channel, deliveryTag, retryCount);

        } catch (Exception e) {
            log.error("系统异常处理申请：监护人ID={}, 错误信息={}",
                    message.getGuardianId(), e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false); // 直接进入死信队列
        }
    }

    private Application getApplicationRecord(ApplicationMessage message) {
        QueryWrapper<Application> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ward_device_id", message.getWardDeviceId())
                .eq("guardian_id", message.getGuardianId());
        return applicationMapper.selectOne(queryWrapper);
    }

    private void processApplication(ApplicationMessage message, Application application) {
        Long aId = applicationService.getApplicationId(message.getGuardianId(), message.getWardDeviceId());
        String applicationId = String.valueOf(aId);

        // 通知监护人
        notificationService.notifyGuardian(message.getGuardianId(), "申请已提交，等待审批", applicationId);

        // 自动审批逻辑
        boolean isDeviceValid = deviceService.validateDevice(message.getWardDeviceId());
        if (isDeviceValid) {
            handleValidDevice(message, application, applicationId);
        } else {
            handleInvalidDevice(message, application, applicationId);
        }

        // 更新申请状态
        applicationMapper.updateById(application);
    }

    private void handleValidDevice(ApplicationMessage message, Application application, String applicationId) {
        application.setStatus("PENDING_CONFIRMATION");
        log.info("申请已通过初审，等待被监护人确认：被监护人设备ID={}", message.getWardDeviceId());

        notificationService.notifyGuardian(message.getGuardianId(),
                "申请审核已通过，等待被监护人确认", applicationId);
        notificationService.notifyWard(message.getWardDeviceId(),
                "您有一条待确认的绑定申请", applicationId);
    }

    private void handleInvalidDevice(ApplicationMessage message, Application application, String applicationId) {
        application.setStatus("REJECTED");
        log.info("申请审核被拒绝：被监护人设备ID={}", message.getWardDeviceId());

        notificationService.notifyGuardian(message.getGuardianId(),
                "申请审核被拒绝，设备ID无效", applicationId);
        notificationService.notifyWard(message.getWardDeviceId(),
                "绑定申请被拒绝", applicationId);
    }

    private void handleBusinessException(ApplicationMessage message,
                                         Channel channel,
                                         long deliveryTag,
                                         Integer retryCount) throws IOException {
        if (retryCount != null && retryCount >= MAX_RETRY_COUNT) {
            log.warn("达到最大重试次数，转入死信队列");
            channel.basicNack(deliveryTag, false, false); // 进入死信队列
        } else {
            log.info("准备重试消息");
            channel.basicAck(deliveryTag, false);
            resendMessageWithRetry(message, retryCount);
        }
    }

    private void resendMessageWithRetry(ApplicationMessage message, Integer retryCount) {
        MessagePostProcessor processor = m -> {
            MessageProperties props = m.getMessageProperties();
            props.setHeader("x-retry-count", retryCount == null ? 1 : retryCount + 1);
            return m;
        };

        rabbitTemplate.convertAndSend("apply_exchange",
                "apply_retry_routing_key",
                message,
                processor);
    }

//    // 死信队列处理器
//    @RabbitListener(queues = "dlx.apply_queue")
//    public void processFailedApplications(ApplicationMessage message) {
//        log.error("处理失败申请：监护人ID={}, 被监护人设备ID={}",
//                message.getGuardianId(), message.getWardDeviceId());
//
//        // 记录失败日志或通知管理员
//        notificationService.notifyAdmin(
//                "申请处理失败",
//                "监护人ID: " + message.getGuardianId() +
//                        ", 设备ID: " + message.getWardDeviceId());
//    }
}