package com.project.mapapp.controller_app;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.project.mapapp.common.BaseResponse;
import com.project.mapapp.common.ErrorCode;
import com.project.mapapp.common.ResultUtils;
import com.project.mapapp.exception.BusinessException;
import com.project.mapapp.model.dto.user.UserLoginRequest;
import com.project.mapapp.model.dto.user.UserRegisterRequest;
import com.project.mapapp.model.entity.User;
import com.project.mapapp.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static com.project.mapapp.common.ErrorCode.PARAMS_ERROR;

@RestController
@RequestMapping("/app/user")
@Slf4j
public class AppUserController {

    @Value("${jwt.secret}")
    private String JWT_SECRET;

    @Value("${jwt.expire-time}")
    private long EXPIRE_TIME;

    @Resource
    private UserService userService;



    @PostMapping("/login")
    @ApiOperation("app端用户登录-用户名密码")
    public BaseResponse<String> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(PARAMS_ERROR);
        }
        // 只做最基础的判空，具体校验交给 Service
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(PARAMS_ERROR, "账号或密码不能为空");
        }
    
        User user = userService.userLogin(userAccount, userPassword, request);

        // 生成JWT Token
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", user.getId());
        payload.put("exp", (System.currentTimeMillis() + EXPIRE_TIME) / 1000);

        String token = JWT.create()
                .addPayloads(payload)
                .setKey(JWT_SECRET.getBytes())
                .sign();
        user.setToken(token);
        return ResultUtils.success(token);
    }

    @PostMapping("/loginByEmail")
    @ApiOperation("app端用户登录-邮箱验证码")
    public BaseResponse<String> loginByEmail(@RequestBody UserLoginRequest user, HttpServletRequest request) {
        log.info("Received login request: email={}, code={}", user.getEmail(), user.getCode());
        try {
            String email = user.getEmail();
            String code = user.getCode();
            if (StrUtil.isEmpty(email) || StrUtil.isEmpty(code)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
            User user1 = userService.loginByEmail(email, code, request);

            // 生成JWT Token
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", user1.getId());
            payload.put("exp", (System.currentTimeMillis() + EXPIRE_TIME) / 1000);

            String token = JWT.create()
                    .addPayloads(payload)
                    .setKey(JWT_SECRET.getBytes())
                    .sign();
            user1.setToken(token);
            return ResultUtils.success(token);
        } catch (Exception e) {
            log.error("Login failed: ", e);
            throw e;
        }
    }

//    @PostMapping("/register")
//    @ApiOperation("app端用户注册")
//    public BaseResponse<String> userRegister(@RequestBody UserRegisterRequest userRegisterRequest, HttpServletRequest request) {
//        if (userRegisterRequest == null) {
//            throw new BusinessException(PARAMS_ERROR);
//        }
//        // 只做最基础的判空，具体校验交给 Service
//        String userAccount = userRegisterRequest.getUserAccount();
//        String userPassword = userRegisterRequest.getUserPassword();
//        String checkPassword = userRegisterRequest.getCheckPassword();
//        String email = userRegisterRequest.getEmail();
//        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, email)) {
//            throw new BusinessException(PARAMS_ERROR, "账号、密码、确认密码或邮箱不能为空");
//        }
//        if (!userPassword.equals(checkPassword)) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
//        }
//
//        User user = userService.userRegister(userAccount, userPassword, email, request);
//
//        // 生成JWT Token
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("userId", user.getId());
//        payload.put("exp", (System.currentTimeMillis() + EXPIRE_TIME) / 1000);
//
//        String token = JWT.create()
//                .addPayloads(payload)
//                .setKey(JWT_SECRET.getBytes())
//                .sign();
//        user.setToken(token);
//
//        return ResultUtils.success(token);
//    }
}
