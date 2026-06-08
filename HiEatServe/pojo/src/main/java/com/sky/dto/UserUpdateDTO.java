package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户信息更新数据传输对象")
public class UserUpdateDTO {

  @ApiModelProperty("用户ID")
  private Long id;

  @ApiModelProperty("用户名")
  private String username;

  @ApiModelProperty("手机号")
  private String phone;

  @ApiModelProperty("性别")
  private String sex;

  @ApiModelProperty("头像")
  private String avatar;

  @ApiModelProperty("新密码")
  private String newPassword;

  @ApiModelProperty("重复密码")
  private String confirmPassword;
}