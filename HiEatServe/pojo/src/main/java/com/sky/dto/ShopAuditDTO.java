package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "店铺审核数据传输对象")
public class ShopAuditDTO implements Serializable {
  private static final long serialVersionUID = 1L;

  @ApiModelProperty("店铺ID")
  private Long shopId;

  @ApiModelProperty("审核状态 1通过 2拒绝")
  private Integer auditStatus;

  @ApiModelProperty("审核意见")
  private String auditOpinion;
}