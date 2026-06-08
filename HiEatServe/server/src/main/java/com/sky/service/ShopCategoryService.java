package com.sky.service;

import com.sky.entity.ShopCategory;
import java.util.List;

/**
 * 店铺分类服务
 */
public interface ShopCategoryService {

    /**
     * 新增店铺分类
     * @param shopCategory
     */
    void save(ShopCategory shopCategory);

    /**
     * 删除店铺分类
     * @param id
     */
    void deleteById(Long id);

    /**
     * 更新店铺分类
     * @param shopCategory
     */
    void update(ShopCategory shopCategory);

    /**
     * 根据id查询店铺分类
     * @param id
     * @return
     */
    ShopCategory getById(Long id);

    /**
     * 查询所有店铺分类
     * @return
     */
    List<ShopCategory> list();

    /**
     * 根据状态查询店铺分类
     * @param status
     * @return
     */
    List<ShopCategory> listByStatus(Integer status);
    
    /**
     * 启用或禁用店铺分类
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);
} 