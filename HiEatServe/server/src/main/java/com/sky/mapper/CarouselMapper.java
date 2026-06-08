package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.entity.Carousel;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 轮播图Mapper
 */
@Mapper
public interface CarouselMapper {

    /**
     * 新增轮播图
     * @param carousel
     */
    @Insert("insert into carousel (image_url, shop_id) values (#{imageUrl}, #{shopId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Carousel carousel);

    /**
     * 删除轮播图
     * @param id
     */
    @Delete("delete from carousel where id = #{id}")
    void deleteById(Long id);

    /**
     * 更新轮播图
     * @param carousel
     */
    @Update("update carousel set image_url = #{imageUrl}, shop_id = #{shopId} where id = #{id}")
    void update(Carousel carousel);

    /**
     * 根据ID查询轮播图
     * @param id
     * @return
     */
    @Select("select * from carousel where id = #{id}")
    Carousel getById(Long id);

    /**
     * 分页查询轮播图
     * @return
     */
    Page<Carousel> pageQuery();

    /**
     * 查询所有轮播图
     * @return
     */
    @Select("select * from carousel")
    List<Carousel> list();
} 