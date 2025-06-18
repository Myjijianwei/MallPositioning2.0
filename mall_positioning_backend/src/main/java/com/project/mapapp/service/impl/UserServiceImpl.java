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
            log.info("登录失败, 用户不存在或密码错误");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // TODO JWT完全跑通之后可以删除此步骤  3. 记录用户的登录态
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
}