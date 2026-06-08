package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "管理员登录数据传输对象")
public class AdminDTO implements Serializable {
  private static final long serialVersionUID = 1L;

  @ApiModelProperty("用户名")
  private String username;

  @ApiModelProperty("密码")
  private String password;
}