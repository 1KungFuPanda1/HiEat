package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginAccountDTO;
import com.sky.dto.UserLoginCodeDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.dto.UserUpdateDTO;
import com.sky.dto.UserRegisterDTO;
import com.sky.dto.UserResetPasswordDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserInfoVO;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
@Api(tags = "C端用户相关接口")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;



    /**
     * 手机号密码登录
     * 
     * @param userLoginAccountDTO
     * @return
     */
    @PostMapping("/login/account")
    @ApiOperation("手机号密码登录")
    public Result<UserLoginVO> loginByAccount(@RequestBody UserLoginAccountDTO userLoginAccountDTO) {
        log.info("用户手机号密码登录：{}", userLoginAccountDTO.getPhone());

        // 手机号密码登录
        User user = userService.accountLogin(userLoginAccountDTO);

        // 为用户生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .username(user.getUsername())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }

    /**
     * 发送验证码
     *
     * @param userLoginAccountDTO
     * @return
     */
    @PostMapping("/sendCode")
    @ApiOperation("发送验证码")
    public Result<String> sendCode(@RequestBody UserLoginAccountDTO userLoginAccountDTO) {
        String phone = userLoginAccountDTO.getPhone();
        log.info("发送验证码：手机号={}", phone);

        userService.sendVerificationCode(phone);
        return Result.success("验证码已发送");
    }

    /**
     * 手机号验证码登录
     *
     * @param userLoginCodeDTO
     * @return
     */
    @PostMapping("/login/sms")
    @ApiOperation("手机号验证码登录")
    public Result<UserLoginVO> loginByCode(@RequestBody UserLoginCodeDTO userLoginCodeDTO) {
        log.info("用户验证码登录：手机号={}", userLoginCodeDTO.getPhone());

        // 验证码登录
        User user = userService.codeLogin(userLoginCodeDTO);

        // 为用户生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .username(user.getUsername())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }

    /**
     * 更新用户信息
     *
     * @param userUpdateDTO
     * @return
     */
    @PutMapping("/update")
    @ApiOperation("更新用户信息")
    public Result<String> update(@RequestBody UserUpdateDTO userUpdateDTO) {
        log.info("更新用户信息：{}", userUpdateDTO);
        userService.update(userUpdateDTO);
        return Result.success("用户信息更新成功");
    }

    /**
     * 获取用户信息
     * 
     * @return
     */
    @GetMapping("/info")
    @ApiOperation("获取用户信息")
    public Result<User> getUserInfo() {
        User user = userService.getCurrentUserInfo();
        return Result.success(user);
    }

    /**
     * 获取脱敏用户信息
     */
    @GetMapping("/get/userInfo")
    @ApiOperation("获取脱敏用户信息")
    public Result<User> getUserInfoNoPassword() {
        User user = userService.getCurrentUserInfo();
        return Result.success(user);
    }

    /**
     * 用户注册
     * 
     * @param userRegisterDTO
     * @return
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<UserLoginVO> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册：{}", userRegisterDTO);
        User user = userService.register(userRegisterDTO);

        // 为用户生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }

    /**
     * 忘记密码
     * 
     * @param userResetPasswordDTO
     * @return
     */
    @PutMapping("/resetPassword")
    @ApiOperation("忘记密码")
    public Result<String> resetPassword(@RequestBody UserResetPasswordDTO userResetPasswordDTO) {
        log.info("用户重置密码：{}", userResetPasswordDTO);
        userService.resetPassword(userResetPasswordDTO);
        return Result.success("密码重置成功");
    }
}
