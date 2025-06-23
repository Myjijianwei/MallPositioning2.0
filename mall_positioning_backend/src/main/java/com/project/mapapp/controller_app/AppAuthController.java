package com.project.mapapp.controller_app;

import cn.hutool.core.util.StrUtil;
import com.project.mapapp.common.BaseResponse;
import com.project.mapapp.common.ErrorCode;
import com.project.mapapp.common.ResultUtils;
import com.project.mapapp.exception.BusinessException;
import com.project.mapapp.model.dto.user.ProfileDTO;
import com.project.mapapp.model.dto.user.UserLoginRequest;
import com.project.mapapp.model.dto.user.UserRegisterDTO;
import com.project.mapapp.model.dto.user.UserRegisterRequest;
import com.project.mapapp.model.entity.User;
import com.project.mapapp.service.ThirdPartyService;
import com.project.mapapp.service.UserService;
import com.project.mapapp.utils.JwtTokenUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.project.mapapp.common.ErrorCode.PARAMS_ERROR;

@Api(tags = "APP用户管理")
@RestController
@RequestMapping("/app/auth")
@Slf4j
public class AppAuthController {

    @Resource
    private UserService userService;

    @Resource
    private ThirdPartyService thirdPartyService;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/login")
    @ApiOperation("用户名密码登录")
    public BaseResponse<String> userLogin(@RequestBody UserLoginRequest userLoginRequest,
                                          HttpServletRequest request) {
        validateLoginRequest(userLoginRequest);

        User user = userService.userLogin(
                userLoginRequest.getUserAccount(),
                userLoginRequest.getUserPassword(),
                request
        );

        return ResultUtils.success(jwtTokenUtil.generateToken(user.getId()));
    }

    @PostMapping("/loginByEmail")
    @ApiOperation("邮箱验证码登录")
    public BaseResponse<String> loginByEmail(@RequestBody UserLoginRequest userLoginRequest,HttpServletRequest request) {
        if (StrUtil.hasBlank(userLoginRequest.getEmail(), userLoginRequest.getCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱和验证码不能为空");
        }

        User user = userService.loginByEmail(
                userLoginRequest.getEmail(),
                userLoginRequest.getCode(),
                request
        );

        return ResultUtils.success(jwtTokenUtil.generateToken(user.getId()));
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public BaseResponse<Long> userRegister(@RequestBody @Valid UserRegisterRequest request) {
        UserRegisterDTO dto = buildRegisterDTO(request);
        long userId = userService.userRegister(dto);
        return ResultUtils.success(userId);
    }

    @GetMapping("/checkToken")
    @ApiOperation("Token校验")
    public BaseResponse<Boolean> checkToken(@RequestHeader("Authorization") String token) {
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResultUtils.success(jwtTokenUtil.validateToken(token));
    }

    @PostMapping("/refreshToken")
    @ApiOperation("刷新Token")
    public BaseResponse<String> refreshToken(@RequestHeader("Authorization") String oldToken) {
        Long userId = jwtTokenUtil.getUserIdFromToken(oldToken);
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResultUtils.success(jwtTokenUtil.generateToken(userId));
    }

    /**
     * 获取用户资料
     * @param token 通过拦截器从请求头获取的JWT
     * @return 用户资料DTO
     */
    @GetMapping("/profile")
    public BaseResponse<ProfileDTO> getProfile(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtTokenUtil.getUserIdFromToken(token);
        ProfileDTO profile = userService.getUserProfile(userId);
        return ResultUtils.success(profile);
    }

    // 辅助方法
    private void validateLoginRequest(UserLoginRequest request) {
        if (request == null || StringUtils.isAnyBlank(
                request.getUserAccount(),
                request.getUserPassword()
        )) {
            throw new BusinessException(PARAMS_ERROR, "账号密码不能为空");
        }
    }

    private UserRegisterDTO buildRegisterDTO(UserRegisterRequest request) {
        return UserRegisterDTO.builder()
                .userAccount(request.getUserAccount())
                .userPassword(request.getUserPassword())
                .checkPassword(request.getCheckPassword())
                .email(request.getEmail())
                .code(request.getCode())
                .avatarUrl(thirdPartyService.getRandomAvatar())
                .username(thirdPartyService.getRandomUsername())
                .userRole(request.getUserRole())
                .build();
    }
}