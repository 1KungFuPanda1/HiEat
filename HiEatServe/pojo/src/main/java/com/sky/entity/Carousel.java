package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 轮播图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Carousel implements Serializable {

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
    
    /**
     * 关联店铺信息(展示用)
     */
    private Shop shop;
} 