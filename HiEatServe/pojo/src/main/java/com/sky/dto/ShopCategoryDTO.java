package com.sky.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 店铺分类DTO
 */
@Data
public class ShopCategoryDTO implements Serializable {

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