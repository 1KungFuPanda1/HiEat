package com.sky.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户重置密码DTO
 */
@Data
public class UserResetPasswordDTO implements Serializable {
  // 手机号
  private String phone;
  // 新密码
  private String newPassword;
  // 验证码
  private String code;
}