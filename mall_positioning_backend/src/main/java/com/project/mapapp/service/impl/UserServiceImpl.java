package com.project.mapapp.service.impl;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.mapapp.common.ErrorCode;
import com.project.mapapp.exception.BusinessException;
import com.project.mapapp.mapper.UserMapper;
import com.project.mapapp.mapper.WardMapper;
import com.project.mapapp.model.dto.user.EmailUpdateRequest;
import com.project.mapapp.model.dto.user.ProfileDTO;
import com.project.mapapp.model.dto.user.UserRegisterDTO;
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

    @Resource
    private VerificationCodeServiceImpl verificationCodeService;

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

    @Override
    public ProfileDTO getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        ProfileDTO profileDTO=new ProfileDTO();
        BeanUtils.copyProperties(user, profileDTO);
        return profileDTO;
    }

    @Override
    public ProfileDTO updateUserEmail(Long userId, EmailUpdateRequest emailUpdateRequest) {
        // 1. 参数校验
        if (userId == null || emailUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 2. 获取当前用户
        User currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        String newEmail = emailUpdateRequest.getEmail();
        String code = emailUpdateRequest.getCode();

        // 3. 检查邮箱是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", newEmail);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已被注册");
        }

        // 4. 验证码校验
        String redisKey = "verificationCode:" + newEmail;
        String redisCode = stringRedisTemplate.opsForValue().get(redisKey);
        if (StrUtil.isEmpty(redisCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码已过期");
        }
        if (!redisCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }

        // 5. 更新邮箱
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setEmail(newEmail);
        boolean success = this.updateById(updateUser);

        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "邮箱更新失败");
        }

        // 6. 删除已使用的验证码
        stringRedisTemplate.delete(redisKey);

        // 7. 返回更新后的用户信息
        User updatedUser = this.getById(userId);
        ProfileDTO profileDTO = new ProfileDTO();
        BeanUtils.copyProperties(updatedUser, profileDTO);

        return profileDTO;
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


    /**
     * 用户注册—app端
     * @param userRegisterDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long userRegister(UserRegisterDTO userRegisterDTO) {
        // 1. 参数校验
        validateRegisterParams(userRegisterDTO);

        // 2. 校验验证码
        verificationCodeService.validateCode(userRegisterDTO.getEmail(), userRegisterDTO.getCode());

        // 3. 加密密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userRegisterDTO.getUserPassword()).getBytes());

        // 4. 创建用户实体
        User user = User.builder()
                .userAccount(userRegisterDTO.getUserAccount())
                .userPassword(encryptPassword)
                .email(userRegisterDTO.getEmail())
                .userAvatar(userRegisterDTO.getAvatarUrl())
                .userName(userRegisterDTO.getUsername())
                .userRole(userRegisterDTO.getUserRole())
                .build();

        // 5. 保存用户
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }

        // 6. 如果是被监护人角色，创建对应记录
        if (WARD_ROLE.equals(userRegisterDTO.getUserRole())) {
            createWardRecord(user.getId());
        }

        return user.getId();
    }

    private void validateRegisterParams(UserRegisterDTO dto) {
        // 参数非空校验
        if (StringUtils.isAnyBlank(
                dto.getUserAccount(),
                dto.getUserPassword(),
                dto.getCheckPassword(),
                dto.getEmail(),
                dto.getCode()
        )) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }

        // 账号长度校验
        if (dto.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }

        // 密码长度校验
        if (dto.getUserPassword().length() < 8 || dto.getCheckPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        // 密码一致性校验
        if (!dto.getUserPassword().equals(dto.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 邮箱格式校验
        if (!ReUtil.isMatch("^[A-Za-z0-9+_.-]+@(.+)$", dto.getEmail())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }

        // 检查账户和邮箱是否已存在
        checkAccountAndEmailDuplicate(dto.getUserAccount(), dto.getEmail());
    }

    private void checkAccountAndEmailDuplicate(String userAccount, String email) {
        // 账户不能重复
        if (lambdaQuery().eq(User::getUserAccount, userAccount).exists()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该账号已存在");
        }

        // 邮箱不能重复
        if (lambdaQuery().eq(User::getEmail, email).exists()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已注册");
        }
    }

    private void createWardRecord(Long userId) {
        Ward ward = new Ward();
        ward.setId(userId);
        int result = wardMapper.insert(ward);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建被监护人记录失败");
        }
    }
}