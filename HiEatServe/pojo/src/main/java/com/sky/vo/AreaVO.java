package com.sky.vo;

import com.sky.entity.ChArea;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 区域数据VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaVO implements Serializable {
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

    /**
     * 子区域列表
     */
    private List<AreaVO> children;
}