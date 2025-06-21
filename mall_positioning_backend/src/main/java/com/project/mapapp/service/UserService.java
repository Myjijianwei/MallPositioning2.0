package com.project.mapapp.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.project.mapapp.model.dto.user.UserRegisterDTO;
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
     * 用户注册—app端
     * @param userRegisterDTO
     * @return
     */
    long userRegister(UserRegisterDTO userRegisterDTO);
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
