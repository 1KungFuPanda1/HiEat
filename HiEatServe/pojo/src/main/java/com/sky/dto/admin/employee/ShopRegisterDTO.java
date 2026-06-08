package com.sky.dto.admin.employee;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "商家注册数据传输对象")
public class ShopRegisterDTO implements Serializable {
  private static final long serialVersionUID = 1L;

  @ApiModelProperty("用户名")
  private String username;

  @ApiModelProperty("密码")
  private String password;

  @ApiModelProperty("责任人姓名")
  private String name;

  @ApiModelProperty("手机号")
  private String phone;

  @ApiModelProperty("责任人身份证号")
  private String idNumber;

  @ApiModelProperty("店铺名称")
  private String shopName;

  @ApiModelProperty("店铺分类")
  private Long categoryId;

  @ApiModelProperty("店铺地址")
  private String shopAddress;

  @ApiModelProperty("店铺Logo")
  private String shopLogo;

  @ApiModelProperty("营业执照")
  private String businessLicense;

  @ApiModelProperty("店铺照片")
  private List<String> shopPhotos;

  @ApiModelProperty("验证码")
  private String code;
}