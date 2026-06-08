package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.CarouselDTO;
import com.sky.entity.Carousel;
import com.sky.entity.Shop;
import com.sky.exception.CarouselException;
import com.sky.mapper.CarouselMapper;
import com.sky.mapper.ShopMapper;
import com.sky.result.PageResult;
import com.sky.service.CarouselService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 轮播图服务实现
 */
@Service
@Slf4j
public class CarouselServiceImpl implements CarouselService {

    @Autowired
    private CarouselMapper carouselMapper;
    @Autowired
    private ShopMapper shopMapper;

    /**
     * 新增轮播图
     * @param carouselDTO
     */
    @Override
    public void save(CarouselDTO carouselDTO) {
        Long shopID = carouselDTO.getShopId();
        if (shopID != null) {
            Shop shop = shopMapper.getById(shopID);
            if (shop == null) {
                throw new CarouselException("关联的店铺不存在");
            }
        }
        Carousel carousel = new Carousel();
        BeanUtils.copyProperties(carouselDTO, carousel);
        carouselMapper.insert(carousel);
    }

    /**
     * 删除轮播图
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        carouselMapper.deleteById(id);
    }

    /**
     * 更新轮播图
     * @param carouselDTO
     */
    @Override
    public void update(CarouselDTO carouselDTO) {
        Carousel carousel = new Carousel();
        BeanUtils.copyProperties(carouselDTO, carousel);
        carouselMapper.update(carousel);
    }

    /**
     * 根据ID查询轮播图
     * @param id
     * @return
     */
    @Override
    public Carousel getById(Long id) {
        return carouselMapper.getById(id);
    }

    /**
     * 分页查询轮播图
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult pageQuery(int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        Page<Carousel> carousels = carouselMapper.pageQuery();
        return new PageResult(carousels.getTotal(), carousels.getResult());
    }

    /**
     * 查询所有轮播图
     * @return
     */
    @Override
    public List<Carousel> list() {
        return carouselMapper.list();
    }
} 