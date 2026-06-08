package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "用户账号密码登录数据传输对象")
public class UserLoginAccountDTO implements Serializable {
  private static final long serialVersionUID = 1L;

  @ApiModelProperty("手机号码")
  private String phone;

  @ApiModelProperty("密码")
  private String password;
}