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
}