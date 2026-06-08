package com.sky.service;

import com.sky.dto.UserLoginAccountDTO;
import com.sky.dto.UserLoginCodeDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.dto.UserRegisterDTO;
import com.sky.dto.UserUpdateDTO;
import com.sky.dto.UserResetPasswordDTO;
import com.sky.entity.User;
import com.sky.vo.UserInfoVO;

public interface UserService {



    /**
     * 手机号密码登录
     * 
     * @param userLoginAccountDTO
     * @return
     */
    User accountLogin(UserLoginAccountDTO userLoginAccountDTO);

    /**
     * 更新用户信息
     * 
     * @param userUpdateDTO
     */
    void update(UserUpdateDTO userUpdateDTO);

    /**
     * 获取当前登录用户信息
     * 
     * @return
     */
    User getCurrentUserInfo();

    User register(UserRegisterDTO userRegisterDTO);

    /**
     * 重置密码
     * 
     * @param userResetPasswordDTO
     */
    void resetPassword(UserResetPasswordDTO userResetPasswordDTO);

    /**
     * 发送验证码
     *
     * @param phone 手机号
     * @return 验证码
     */
    String sendVerificationCode(String phone);

    /**
     * 验证码登录
     *
     * @param userLoginCodeDTO
     * @return
     */
    User codeLogin(UserLoginCodeDTO userLoginCodeDTO);
}
