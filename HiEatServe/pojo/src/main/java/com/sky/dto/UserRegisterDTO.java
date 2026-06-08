package com.sky.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户注册DTO
 */
@Data
public class UserRegisterDTO implements Serializable {
  // 用户名
  private String username;
  // 手机号
  private String phone;
  // 密码
  private String password;
  // 验证码
  private String code;
}