package com.sky.service.impl;

import com.sky.entity.ShopCategory;
import com.sky.mapper.ShopCategoryMapper;
import com.sky.service.ShopCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 店铺分类服务实现
 */
@Service
@Slf4j
public class ShopCategoryServiceImpl implements ShopCategoryService {

    @Autowired
    private ShopCategoryMapper shopCategoryMapper;

    /**
     * 新增店铺分类
     * @param shopCategory
     */
    @Override
    public void save(ShopCategory shopCategory) {
        shopCategoryMapper.insert(shopCategory);
    }

    /**
     * 删除店铺分类
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        shopCategoryMapper.deleteById(id);
    }

    /**
     * 更新店铺分类
     * @param shopCategory
     */
    @Override
    public void update(ShopCategory shopCategory) {
        shopCategoryMapper.update(shopCategory);
    }

    /**
     * 根据id查询店铺分类
     * @param id
     * @return
     */
    @Override
    public ShopCategory getById(Long id) {
        return shopCategoryMapper.getById(id);
    }

    /**
     * 查询所有店铺分类
     * @return
     */
    @Override
    public List<ShopCategory> list() {
        return shopCategoryMapper.list();
    }

    /**
     * 根据状态查询店铺分类
     * @param status
     * @return
     */
    @Override
    public List<ShopCategory> listByStatus(Integer status) {
        return shopCategoryMapper.listByStatus(status);
    }

    /**
     * 启用或禁用店铺分类
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        ShopCategory shopCategory = ShopCategory.builder()
                .id(id)
                .status(status)
                .build();
        shopCategoryMapper.update(shopCategory);
    }
} 