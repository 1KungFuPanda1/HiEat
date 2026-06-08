package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.entity.Shop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShopMapper {

        /**
         * 根据ID查询店铺信息
         * 
         * @param id 店铺ID
         * @return 店铺信息
         */
        @Select("select * from shop where id = #{id}")
        Shop getById(Long id);

        /**
         * 更新店铺信息
         * 
         * @param shop 店铺信息
         * @return 影响的行数
         */
        int update(Shop shop);

        /**
         * 分页查询店铺信息
         * 
         * @param auditStatus 审核状态
         * @return 店铺分页结果
         */
        Page<Shop> pageQueryByAudit(int page, int pageSize, Integer auditStatus);

        /**
         * 根据审核状态统计店铺数量
         * 
         * @param auditStatus 审核状态
         * @return 店铺数量
         */
        @Select("select count(id) from shop where audit_status = #{auditStatus}")
        Integer countByAuditStatus(Integer auditStatus);

        /**
         * 插入店铺信息
         * 
         * @param shop 店铺信息
         */
        void insert(Shop shop);

        /**
         * 根据平均送达时间范围查询店铺
         * 
         * @param minTime 最小时间
         * @param maxTime 最大时间
         * @return 店铺列表
         */
        @Select("select * from shop where average_send_time between #{minTime} and #{maxTime} and status = 1 and audit_status = 1")
        Page<Shop> queryByDeliveryTime(Integer minTime, Integer maxTime);

        /**
         * 根据评分范围查询店铺
         * 
         * @param minScore 最小评分
         * @param maxScore 最大评分
         * @return 店铺列表
         */
        @Select("select * from shop where score between #{minScore} and #{maxScore} and status = 1 and audit_status = 1")
        Page<Shop> queryByScore(Double minScore, Double maxScore);

        /**
         * 更新店铺评分
         * 
         * @param shopId 店铺ID
         * @param score  新评分
         * @return 影响的行数
         */
        @Update("update shop set score = #{score} where id = #{shopId}")
        int updateScore(Long shopId, Double score);

        /**
         * 更新店铺订单量
         * 
         * @param shopId 店铺ID
         * @return 影响的行数
         */
        @Update("update shop set order_quantity = order_quantity + 1 where id = #{shopId}")
        int incrementOrderQuantity(Long shopId);

        /**
         * 分页查询用户端可见的店铺
         *
         * @param status      店铺状态
         * @param auditStatus 审核状态
         * @return 店铺分页结果
         */
        Page<Shop> pageQueryForUser(Integer status, Integer auditStatus);

        Page<Shop> pageQueryByRule(Integer status, Integer auditStatus, String rule);

        /**
         * 根据分类id分页查询店铺信息
         * 
         * @param categoryId 分类id
         * @return 店铺分页结果
         */
        @Select("select * from shop where category_id = #{categoryId} and status = 1 and audit_status = 1")
        Page<Shop> pageQueryByCategoryId(Long categoryId);

        /**
         * 根据分类id和排序规则查询店铺
         * 
         * @param categoryId 分类id
         * @param rule       排序规则
         * @return 店铺分页结果
         */
        Page<Shop> pageQueryByCategoryIdWithRule(@Param("categoryId") Long categoryId, @Param("rule") String rule);

        /**
         * 根据店铺名称模糊搜索店铺
         * 
         * @param shopName 店铺名称关键词
         * @return 店铺分页结果
         */
        @Select("select * from shop where shop_name like concat('%', #{shopName}, '%') " +
                        "and status = 1 and audit_status = 1 order by create_time desc")
        Page<Shop> pageQueryByName(String shopName);

        /**
         * 根据店铺名称和排序规则模糊搜索店铺
         * 
         * @param shopName 店铺名称关键词
         * @param rule     排序规则
         * @return 店铺分页结果
         */
        Page<Shop> pageQueryByNameWithRule(@Param("shopName") String shopName, @Param("rule") String rule);
}
