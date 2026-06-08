package com.sky.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 轮播图DTO
 */
@Data
public class CarouselDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 轮播图ID
     */
    private Long id;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 关联店铺ID(可为空)
     */
    private Long shopId;
} 