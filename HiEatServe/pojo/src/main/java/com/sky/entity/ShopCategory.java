package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 店铺分类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类图片
     */
    private String image;

    /**
     * 状态 0:禁用 1:启用
     */
    private Integer status;
} 