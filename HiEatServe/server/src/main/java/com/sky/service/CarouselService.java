package com.sky.service;

import com.sky.dto.CarouselDTO;
import com.sky.entity.Carousel;
import com.sky.result.PageResult;

import java.util.List;

/**
 * 轮播图服务
 */
public interface CarouselService {

    /**
     * 新增轮播图
     * @param carouselDTO
     */
    void save(CarouselDTO carouselDTO);

    /**
     * 删除轮播图
     * @param id
     */
    void deleteById(Long id);

    /**
     * 更新轮播图
     * @param carouselDTO
     */
    void update(CarouselDTO carouselDTO);

    /**
     * 根据ID查询轮播图
     * @param id
     * @return
     */
    Carousel getById(Long id);

    /**
     * 分页查询轮播图
     * @param page
     * @param pageSize
     * @return
     */
    PageResult pageQuery(int page, int pageSize);

    /**
     * 查询所有轮播图
     * @return
     */
    List<Carousel> list();
} 