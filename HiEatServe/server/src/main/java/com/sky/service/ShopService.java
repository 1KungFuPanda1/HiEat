package com.sky.service;

import com.sky.dto.ShopAuditDTO;
import com.sky.entity.Shop;
import com.sky.dto.admin.shop.ShopUpdateDTO;
import com.sky.result.PageResult;
import com.sky.vo.ShopVO;

import java.util.Map;

public interface ShopService {
    /**
     * 获取店铺信息
     * 
     * @param shopId
     * @return
     */
    Shop getShopInfo(Long shopId);

    /**
     * 获取店铺信息（包含照片）
     * 
     * @param shopId
     * @return
     */
    ShopVO getShopInfoWithImages(Long shopId);

    /**
     * 店铺审核列表
     *
     * @param page
     * @param pageSize
     * @param auditStatus
     * @return
     */
    PageResult auditPage(int page, int pageSize, Integer auditStatus);

    /**
     * 店铺审核
     *
     * @param shopAuditDTO
     */
    void audit(ShopAuditDTO shopAuditDTO);

    void updateShop(ShopUpdateDTO shopUpdateDTO);

    /**
     * 获取不同审核状态的店铺数量
     * 
     * @return 包含不同状态店铺数量的Map
     */
    Map<String, Integer> getShopCounts();

    /**
     * 分页查询店铺信息
     * 
     * @param page     页码
     * @param pageSize 每页记录数
     * @return 店铺分页结果
     */
    PageResult pageQuery(int page, int pageSize);

    PageResult pageQueryByRule(int page, int pageSize, String rule);

    /**
     * 根据分类id分页查询店铺信息
     *
     * @param page
     * @param pageSize
     * @param categoryId
     * @return
     */
    PageResult pageQueryByCategoryId(int page, int pageSize, Long categoryId);

    /**
     * 根据分类id分页查询店铺，支持排序规则
     * @param page 页码
     * @param pageSize 每页记录数
     * @param categoryId 分类id
     * @param rule 排序规则："default"(默认), "score"(评分), "order_quantity"(销量)
     * @return 分页结果
     */
    PageResult pageQueryByCategoryIdWithRule(int page, int pageSize, Long categoryId, String rule);

    /**
     * 根据店铺名称模糊搜索店铺
     * @param page 页码
     * @param pageSize 每页记录数
     * @param shopName 店铺名称关键词
     * @return 分页结果
     */
    PageResult pageQueryByName(int page, int pageSize, String shopName);
    
    /**
     * 根据店铺名称和排序规则模糊搜索店铺
     * @param page 页码
     * @param pageSize 每页记录数
     * @param shopName 店铺名称关键词
     * @param rule 排序规则："default"(默认), "score"(评分), "sales"(销量)
     * @return 分页结果
     */
    PageResult pageQueryByNameWithRule(int page, int pageSize, String shopName, String rule);
}
