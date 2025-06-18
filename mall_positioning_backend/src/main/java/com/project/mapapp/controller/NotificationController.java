package com.project.mapapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.project.mapapp.common.BaseResponse;
import com.project.mapapp.common.ErrorCode;
import com.project.mapapp.common.ResultUtils;
import com.project.mapapp.exception.BusinessException;
import com.project.mapapp.mapper.ApplicationMapper;
import com.project.mapapp.mapper.UserMapper;
import com.project.mapapp.model.dto.Notification.NotificationMessage;
import com.project.mapapp.model.entity.Application;
import com.project.mapapp.model.entity.Notification;
import com.project.mapapp.service.NotificationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Resource
    private NotificationService notificationService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ApplicationMapper applicationMapper;

    @GetMapping("/list")
    public BaseResponse<List<NotificationMessage>> getNotifications(@RequestParam Long userId) {

        List<NotificationMessage> messages = new ArrayList<>();
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).orderByDesc("created_at");
        notificationService.list(queryWrapper).forEach(notification -> {
            Application application = applicationMapper.selectById(notification.getApplication_id());
            String userName = userMapper.selectById(application.getGuardian_id()).getUserName();
            NotificationMessage message = new NotificationMessage();
            BeanUtils.copyProperties(notification, message);
            message.setUserName(userName);
            message.setStatus(application.getStatus());
            messages.add(message);
        });

        return ResultUtils.success(messages);
    }

    @PostMapping("/markAsRead")
    public BaseResponse<Boolean> markAsRead(@RequestParam Long notificationId) {
        Notification notification = notificationService.getById(notificationId);
        if (notification == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "通知不存在");
        }
        notification.setIs_read(1);
        notificationService.updateById(notification);
        return ResultUtils.success(true);
    }

    @PostMapping("/markAllAsRead")
    public BaseResponse<Boolean> markAllAsRead(@RequestParam Long userId) {
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("is_read", 0);
        List<Notification> notifications = notificationService.list(queryWrapper);
        notifications.forEach(notification -> notification.setIs_read(1));
        notificationService.updateBatchById(notifications);
        return ResultUtils.success(true);
    }

    @GetMapping("/unreadCount")
    public BaseResponse<Integer> getUnreadNotificationCount(@RequestParam Long userId) {
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("is_read", false);
        int count = (int) notificationService.count(queryWrapper);
        return ResultUtils.success(count);
    }
}
