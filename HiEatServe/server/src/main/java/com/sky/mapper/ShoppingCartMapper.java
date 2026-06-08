package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 动态条件查询
     * 
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据id修改商品数量
     * 
     * @param shoppingCart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 插入购物车数据
     * 
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name, user_id, shop_id,dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) "
            +
            " values (#{name},#{userId},#{shopId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{image},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据用户id删除购物车数据
     * 
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

    /**
     * 根据id删除购物车数据
     * 
     * @param id
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);

    /**
     * 批量插入购物车数据
     *
     * @param shoppingCartList
     */
    void insertBatch(List<ShoppingCart> shoppingCartList);

    /**
     * 根据用户id和店铺id查询购物车数据
     * 
     * @param shoppingCart
     * @return
     */
    @Select("select * from shopping_cart where user_id = #{userId} and shop_id = #{shopId}")
    List<ShoppingCart> listByUserIdAndShopId(ShoppingCart shoppingCart);

    /**
     * 根据用户id和店铺id删除购物车数据
     * 
     * @param userId
     * @param shopId
     */
    @Delete("delete from shopping_cart where user_id = #{userId} and shop_id = #{shopId}")
    void deleteByUserIdAndShopId(Long userId, Long shopId);

}
