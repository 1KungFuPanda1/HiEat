package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 省市区区域实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChArea implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 父级ID
     */
    private Long parentId;

    /**
     * 级别 (1:省, 2:市, 3:区/县)
     */
    private Integer level;
}