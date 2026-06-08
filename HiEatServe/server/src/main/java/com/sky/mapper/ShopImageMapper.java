package com.sky.mapper;

import com.sky.entity.ShopImage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface ShopImageMapper {

    void insertBatch(ArrayList<ShopImage> shopImages);

    /**
     * 根据店铺ID查询店铺图片
     * 
     * @param shopId
     * @return
     */
    @Select("select image_url from shop_image where shop_id = #{shopId}")
    List<String> getImagesByShopId(Long shopId);


    @Delete("delete from shop_image where shop_id = #{shopId}")
    void deleteByShopId(Long shopId);

}
