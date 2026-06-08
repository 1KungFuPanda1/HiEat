package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.constant.RedisConstant;
import com.sky.context.BaseContext;
import com.sky.dto.UserLoginAccountDTO;
import com.sky.dto.UserRegisterDTO;
import com.sky.dto.UserResetPasswordDTO;
import com.sky.dto.UserUpdateDTO;
import com.sky.entity.User;
import com.sky.exception.*;
import com.sky.mapper.UserMapper;
import com.sky.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 账号密码登录
     * 
     * @param userLoginAccountDTO
     * @return
     */
    public User accountLogin(UserLoginAccountDTO userLoginAccountDTO) {
        String phone = userLoginAccountDTO.getPhone();
        String password = userLoginAccountDTO.getPassword();

        // 根据手机号查询用户
        User user = userMapper.getByPhone(phone);

        // 判断用户是否存在
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 密码比对
        // 对前端传过来的明文密码进行md5加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(user.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        // 判断账号是否被锁定
        if (Objects.equals(user.getStatus(), StatusConstant.DISABLE)) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.update(user);

        return user;
    }

    /**
     * 更新用户信息
     * 
     * @param userUpdateDTO
     */
    @Override
    public void update(UserUpdateDTO userUpdateDTO) {
        // 获取当前登录用户ID
        Long userId = BaseContext.getCurrentId();
        User user;
        // 查询用户名与手机号是否重复
        if (userUpdateDTO.getUsername() != null || userUpdateDTO.getPhone() != null) {
            user = userMapper.getByUsernameOrPhone(userUpdateDTO.getUsername(), userUpdateDTO.getPhone());
            if (user != null) {
                throw new UsernameOrPhoneExists("用户名或手机号已注册");
            }
        }

        // 创建用户对象并设置属性
        user = new User();
        BeanUtils.copyProperties(userUpdateDTO, user);
        user.setId(userId); // 确保使用当前登录用户的ID

        // 设置新密码
        // 两次密码一致
        if (userUpdateDTO.getNewPassword() != null
                && userUpdateDTO.getNewPassword().equals(userUpdateDTO.getConfirmPassword())) {
            user.setPassword(DigestUtils.md5DigestAsHex(userUpdateDTO.getNewPassword().getBytes()));
        }

        // 更新用户信息
        userMapper.update(user);
    }

    /**
     * 获取当前登录用户信息
     * 
     * @return
     */
    @Override
    public User getCurrentUserInfo() {
        // 获取当前登录用户ID
        Long userId = BaseContext.getCurrentId();

        // 查询用户信息
        User user = userMapper.getById(userId);

        // 出于安全考虑，将敏感信息设为null
        if (user != null) {
            user.setPassword(null);
            user.setOpenid(null);
        }

        return user;
    }

    @Override
    public User register(UserRegisterDTO userRegisterDTO) {
        String defaultAvatar = "http://localhost:8080/files/87c668d5-6582-4e7c-b271-affc6f003a06.jpg";
        // 判断用户名是否重复
        User user = userMapper.getByUsername(userRegisterDTO.getUsername());
        if (user != null) {
            throw new UsernameOrPhoneExists("用户名已存在");
        }

        // 判断手机号是否重复
        user = userMapper.getByPhone(userRegisterDTO.getPhone());
        if (user != null) {
            throw new UsernameOrPhoneExists("手机号已存在");
        }

        // 创建用户对象
        user = new User();
        BeanUtils.copyProperties(userRegisterDTO, user);
        // 设置密码
        user.setPassword(DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes()));
        user.setAvatar(defaultAvatar);
        user.setCreateTime(LocalDateTime.now());
        user.setStatus(StatusConstant.ENABLE);

        // 保存用户
        userMapper.insert(user);

        return user;
    }

    @Override
    public void resetPassword(UserResetPasswordDTO userResetPasswordDTO) {
        // 1. 验证手机号是否存在
        User user = userMapper.getByPhone(userResetPasswordDTO.getPhone());
        if (user == null) {
            throw new BusinessException("手机号不存在");
        }

        // 2. 验证验证码是否正确
        String code = redisTemplate.opsForValue()
                .get(RedisConstant.VERIFICATION_CODE_PREFIX + userResetPasswordDTO.getPhone());
        if (code == null || !code.equals(userResetPasswordDTO.getCode())) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 3. 更新密码
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setPassword(DigestUtils.md5DigestAsHex(userResetPasswordDTO.getNewPassword().getBytes()));
        userMapper.update(updateUser);

        // 4. 删除验证码
        redisTemplate.delete(RedisConstant.VERIFICATION_CODE_PREFIX + userResetPasswordDTO.getPhone());
    }

    @Override
    public String sendVerificationCode(String phone) {
        // 1. 验证手机号格式
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }

        // 2. 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 3. 将验证码保存到Redis，设置5分钟过期
        redisTemplate.opsForValue().set(
                RedisConstant.VERIFICATION_CODE_PREFIX + phone,
                code,
                5,
                TimeUnit.MINUTES);

        // 4. 模拟发送验证码
        log.info("向手机号 {} 发送验证码: {}", phone, code);

        return code;
    }
}
