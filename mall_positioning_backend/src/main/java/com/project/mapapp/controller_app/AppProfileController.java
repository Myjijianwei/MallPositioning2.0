package com.project.mapapp.controller_app;

import cn.hutool.core.util.StrUtil;
import com.project.mapapp.common.BaseResponse;
import com.project.mapapp.common.ErrorCode;
import com.project.mapapp.common.ResultUtils;
import com.project.mapapp.exception.BusinessException;
import com.project.mapapp.model.dto.user.EmailUpdateRequest;
import com.project.mapapp.model.dto.user.ProfileDTO;
import com.project.mapapp.service.UserService;
import com.project.mapapp.utils.JwtTokenUtil;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 个人信息
 */

@Api("个人信息维护")
@RestController
@RequestMapping("/app/profile")
public class AppProfileController {
    @Resource
    private UserService userService;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 获取用户资料
     * @param token 通过拦截器从请求头获取的JWT
     * @return 用户资料DTO
     */
    @GetMapping("/getProfile")
    public BaseResponse<ProfileDTO> getProfile(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtTokenUtil.getUserIdFromToken(token);
        ProfileDTO profile = userService.getUserProfile(userId);
        return ResultUtils.success(profile);
    }

//    /**
//     * 更新用户资料
//     * @param token 通过拦截器从请求头获取的JWT
//     * @param profileDTO 用户资料DTO
//     * @return 更新后的用户资料DTO
//     */
//    @GetMapping("/updateProfile")
//    public BaseResponse<ProfileDTO> updateProfile(
//            @RequestHeader("Authorization") String token,
//            ProfileDTO profileDTO) {
//        Long userId = jwtTokenUtil.getUserIdFromToken(token);
//        ProfileDTO updatedProfile = userService.updateUserProfile(userId, profileDTO);
//        return ResultUtils.success(updatedProfile);
//    }

    /**
     * 换绑邮箱
     * @param token
     * @param
     * @return
     */
    @PostMapping("/updateEmail")
    public BaseResponse<ProfileDTO> updateEmail(
            @RequestHeader("Authorization") String token,
            @RequestBody EmailUpdateRequest emailUpdateRequest) {
        // 1. 参数校验
        if (emailUpdateRequest == null || StrUtil.isBlank(emailUpdateRequest.getEmail())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        }

        // 2. 从token获取用户ID
        Long userId = jwtTokenUtil.getUserIdFromToken(token);

        // 3. 调用服务层更新邮箱
        ProfileDTO updatedProfile = userService.updateUserEmail(userId, emailUpdateRequest);

        return ResultUtils.success(updatedProfile);
    }
}
