package com.sky.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShopImage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long shopId;

    private String imageUrl;
}
