package com.project.mapapp.controller;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.project.mapapp.common.BaseResponse;
import com.project.mapapp.common.ErrorCode;
import com.project.mapapp.common.ResultUtils;
import com.project.mapapp.exception.ThrowUtils;
import com.project.mapapp.mapper.DeviceMapper;
import com.project.mapapp.model.dto.device.DeviceBindRequest;
import com.project.mapapp.model.entity.Device;
import com.project.mapapp.model.entity.EmailTask;
import com.project.mapapp.model.entity.User;
import com.project.mapapp.service.DeviceService;
import com.project.mapapp.service.EmailTaskService;
import com.project.mapapp.service.UserService;
import com.project.mapapp.utils.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/msm")
@Slf4j
public class MsmController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private EmailTaskService emailTaskService;

    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping("sendEmail/{email}")
    public ResponseEntity<BaseResponse<String>> sendEmail(@PathVariable String email) {
        String verificationCode = RandomUtil.randomString(6);
        String message = String.format(
                "尊敬的监护人：您好！欢迎使用防走失监护系统。您的验证码为：%s（有效期为五分钟）。请勿泄露此验证码，以确保您的账户安全。",
                verificationCode
        );
        log.info("Sending verification code to email: {} with code: {}", email, verificationCode);

        try {
            // 将验证码存入缓存，设置有效期为5分钟
            redisTemplate.opsForValue().set("verificationCode:" + email, verificationCode, 6, TimeUnit.MINUTES);
            emailTaskService.addEmailTask(email, "验证码", message, EmailTask.EmailType.VERIFICATION_CODE);
            return new ResponseEntity<>(ResultUtils.success(verificationCode), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to send verification code to email: {}", email, e);
            return new ResponseEntity<>(ResultUtils.error(ErrorCode.EMAIL_SEND_FAIL), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("applyDevice/{email}")
    public ResponseEntity<BaseResponse<String>> applyDevice(@PathVariable String email, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<Device> deviceQueryWrapper = new QueryWrapper<>();
        deviceQueryWrapper.eq("user_id", loginUser.getId());
        Long l = deviceMapper.selectCount(deviceQueryWrapper);
        ThrowUtils.throwIf(l>0, ErrorCode.OPERATION_ERROR,"已存在绑定设备，请勿重复绑定");
        try {
            DeviceBindRequest deviceInfo = deviceService.generateDeviceInfo();
            String deviceMessage = new Gson().toJson(deviceInfo);
            redisTemplate.opsForValue().set("applyDeviceInfo:" + email, deviceMessage, 6, TimeUnit.MINUTES);
            emailTaskService.addEmailTask(email, "设备信息", deviceMessage, EmailTask.EmailType.DEVICE_INFO);
            log.info("Sending applyDeviceInfo code to email: {} with deviceId: {}", email, deviceInfo.getDeviceId());
            return new ResponseEntity<>(ResultUtils.success("设备信息已发送到您的邮箱"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to apply device and send email: {}", email, e);
            return new ResponseEntity<>(ResultUtils.error(ErrorCode.EMAIL_SEND_FAIL), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("applyDevice_app/{email}")
    public ResponseEntity<BaseResponse<String>> applyDeviceApp(
            @PathVariable String email,
            @RequestHeader("Authorization") String authHeader) {
        // 解析JWT获取用户ID
        String token = authHeader;
        // 如果你的JwtTokenUtil已自动去除Bearer前缀，这里直接传即可
        Long userId = jwtTokenUtil.getUserIdFromToken(token);
        if (userId == null) {
            return new ResponseEntity<>(ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "无效的token"), HttpStatus.UNAUTHORIZED);
        }
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            return new ResponseEntity<>(ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "用户不存在"), HttpStatus.UNAUTHORIZED);
        }

        QueryWrapper<Device> deviceQueryWrapper = new QueryWrapper<>();
        deviceQueryWrapper.eq("user_id", loginUser.getId());
        Long l = deviceMapper.selectCount(deviceQueryWrapper);
        ThrowUtils.throwIf(l > 0, ErrorCode.OPERATION_ERROR, "已存在绑定设备，请勿重复绑定");
        try {
            DeviceBindRequest deviceInfo = deviceService.generateDeviceInfo();
            String deviceMessage = new Gson().toJson(deviceInfo);
            redisTemplate.opsForValue().set("applyDeviceInfo:" + email, deviceMessage, 6, TimeUnit.MINUTES);
            emailTaskService.addEmailTask(email, "设备信息", deviceMessage, EmailTask.EmailType.DEVICE_INFO);
            log.info("Sending applyDeviceInfo code to email: {} with deviceId: {}", email, deviceInfo.getDeviceId());
            return new ResponseEntity<>(ResultUtils.success("设备信息已发送到您的邮箱"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to apply device and send email: {}", email, e);
            return new ResponseEntity<>(ResultUtils.error(ErrorCode.EMAIL_SEND_FAIL), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}