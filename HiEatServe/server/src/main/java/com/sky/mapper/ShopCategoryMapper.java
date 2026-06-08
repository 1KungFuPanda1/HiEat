package com.sky.mapper;

import com.sky.entity.ShopCategory;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 店铺分类Mapper
 */
@Mapper
public interface ShopCategoryMapper {

    /**
     * 新增店铺分类
     * @param shopCategory
     */
    @Insert("insert into shop_category (id, name, image, status) values (#{id}, #{name}, #{image}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ShopCategory shopCategory);

    /**
     * 删除店铺分类
     * @param id
     */
    @Delete("delete from shop_category where id = #{id}")
    void deleteById(Long id);

    /**
     * 更新店铺分类
     * @param shopCategory
     */
    @Update("update shop_category set name = #{name}, image = #{image}, status = #{status} where id = #{id}")
    void update(ShopCategory shopCategory);

    /**
     * 根据id查询店铺分类
     * @param id
     * @return
     */
    @Select("select * from shop_category where id = #{id}")
    ShopCategory getById(Long id);

    /**
     * 查询所有店铺分类
     * @return
     */
    @Select("select * from shop_category order by id")
    List<ShopCategory> list();

    /**
     * 根据状态查询店铺分类
     * @param status
     * @return
     */
    @Select("select * from shop_category where status = #{status} order by id")
    List<ShopCategory> listByStatus(Integer status);
} 