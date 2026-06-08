package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "手机号验证码登录数据传输对象")
public class UserLoginCodeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("手机号码")
    private String phone;

    @ApiModelProperty("验证码")
    private String code;
}
