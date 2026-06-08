package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@SuperBuilder // 替换原来的@Builder注解
@NoArgsConstructor
@AllArgsConstructor
public class Shop implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String shopName;

    private String address;

    private String phone;

    private String image;

    private String des;

    private Long categoryId;

    private Double score;

    // 店铺状态 1表示正常 0表示禁用
    private Integer status;

    // 审核状态 0待审核 1审核通过 2审核拒绝
    private Integer auditStatus;

    // 审核意见
    private String auditOpinion;

    // 营业执照
    private String businessLicense;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // 平均送达时间
    private Integer averageSendTime;

    // 起送费用
    private Integer minFee;

    // 配送费
    private Integer deliverFee;

    // 订单量
    private Integer orderQuantity;
}
