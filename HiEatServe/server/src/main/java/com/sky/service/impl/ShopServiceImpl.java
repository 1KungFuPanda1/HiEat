package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.ShopAuditDTO;
import com.sky.entity.Employee;
import com.sky.entity.Shop;
import com.sky.entity.ShopImage;
import com.sky.dto.admin.shop.ShopUpdateDTO;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.ShopImageMapper;
import com.sky.mapper.ShopMapper;
import com.sky.result.PageResult;
import com.sky.service.ShopService;
import com.sky.vo.ShopVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShopServiceImpl implements ShopService {

    public static final String SHOP_STATUS_KEY = "SHOP_STATUS";

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private ShopImageMapper shopImageMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取店铺信息
     * 
     * @param shopId
     * @return
     */
    @Override
    public Shop getShopInfo(Long shopId) {
        return shopMapper.getById(shopId);
    }

    /**
     * 获取店铺信息（包含照片）
     * 
     * @param shopId
     * @return
     */
    @Override
    public ShopVO getShopInfoWithImages(Long shopId) {
        // 获取店铺基本信息
        Shop shop = shopMapper.getById(shopId);

        if (shop == null) {
            return null;
        }

        // 获取店铺照片
        List<String> shopImages = shopImageMapper.getImagesByShopId(shopId);

        Employee manager = employeeMapper.getManager(shopId);

        // 构建ShopVO对象
        ShopVO shopVO = new ShopVO();
        BeanUtils.copyProperties(shop, shopVO);
        shopVO.setShopImages(shopImages);
        shopVO.setOwner(manager.getName());

        return shopVO;
    }

    /**
     * 店铺审核列表
     * 
     * @param page
     * @param pageSize
     * @param auditStatus
     * @return
     */
    @Override
    public PageResult auditPage(int page, int pageSize, Integer auditStatus) {
        PageHelper.startPage(page, pageSize);
        Page<Shop> shops = shopMapper.pageQueryByAudit(page, pageSize, auditStatus);

        // 将Shop转换为ShopVO
        List<ShopVO> shopVOList = shops.getResult().stream().map(shop -> {
            ShopVO shopVO = new ShopVO();
            BeanUtils.copyProperties(shop, shopVO);

            // 查询店铺图片
            shopVO.setOwner(employeeMapper.getManager(shop.getId()).getName());
            List<String> shopImages = shopImageMapper.getImagesByShopId(shop.getId());
            shopVO.setShopImages(shopImages);

            return shopVO;
        }).collect(Collectors.toList());

        return new PageResult(shops.getTotal(), shopVOList);
    }

    /**
     * 店铺审核
     * 
     * @param shopAuditDTO
     */
    @Override
    @Transactional
    public void audit(ShopAuditDTO shopAuditDTO) {
        // 获取店铺信息
        Shop shop = shopMapper.getById(shopAuditDTO.getShopId());
        if (shop == null) {
            throw new RuntimeException("店铺不存在");
        }

        // 更新店铺审核状态
        shop.setAuditStatus(shopAuditDTO.getAuditStatus());
        shop.setAuditOpinion(shopAuditDTO.getAuditOpinion());
        shop.setUpdateTime(LocalDateTime.now());

        // 如果审核通过，则启用店铺
        if (shopAuditDTO.getAuditStatus() == 1) {
            shop.setStatus(StatusConstant.ENABLE);

            // 同时启用店长账号
            Employee employee = employeeMapper.getByShopId(shop.getId());
            if (employee != null) {
                employee.setStatus(StatusConstant.ENABLE);
                employee.setUpdateTime(LocalDateTime.now());
                employeeMapper.update(employee);
            }
        }

        shopMapper.update(shop);
    }

    @Override
    @Transactional
    public void updateShop(ShopUpdateDTO shopUpdateDTO) {

        Shop shop = new Shop();
        BeanUtils.copyProperties(shopUpdateDTO, shop);

        // 更新店铺信息
        shopMapper.update(shop);

        // 修改店铺照片
        // 删除旧的
        shopImageMapper.deleteByShopId(shop.getId());
        // 直接插入新的
        ArrayList<ShopImage> shopImages = new ArrayList<>();
        shopUpdateDTO.getShopImages().forEach(shopImage -> {
            ShopImage image = new ShopImage();
            image.setShopId(shop.getId());
            image.setImageUrl(shopImage);
            shopImages.add(image);
        });
        shopImageMapper.insertBatch(shopImages);
    }

    /**
     * 获取不同审核状态的店铺数量
     * 
     * @return 包含不同状态店铺数量的Map
     */
    @Override
    public Map<String, Integer> getShopCounts() {
        Map<String, Integer> countMap = new HashMap<>();

        // 获取待审核店铺数量（auditStatus = 0）
        Integer pendingCount = shopMapper.countByAuditStatus(0);
        countMap.put("pending", pendingCount);

        // 获取审核通过店铺数量（auditStatus = 1）
        Integer approvedCount = shopMapper.countByAuditStatus(1);
        countMap.put("approved", approvedCount);

        // 获取审核拒绝店铺数量（auditStatus = 2）
        Integer rejectedCount = shopMapper.countByAuditStatus(2);
        countMap.put("rejected", rejectedCount);

        // 获取全部店铺数量
        Integer totalCount = pendingCount + approvedCount + rejectedCount;
        countMap.put("total", totalCount);

        return countMap;
    }

    /**
     * 判断店铺是否处于营业状态（未打烊）
     * Redis 中 SHOP_STATUS-{shopId} 值为 0 表示打烊中，null 或 1 表示营业中
     * 如果 Redis 读取异常，默认返回 true（营业中），避免 Redis 故障导致所有店铺不可见
     * @param shopId 店铺ID
     * @return true=营业中，false=打烊中
     */
    private boolean isShopOpen(Long shopId) {
        try {
            String redisKey = SHOP_STATUS_KEY + "-" + shopId;
            Object value = redisTemplate.opsForValue().get(redisKey);
            log.info("从 Redis 读取营业状态: key={}, value={}, type={}", redisKey, value,
                    value != null ? value.getClass().getName() : "null");

            // 未设置过状态默认为营业中(1)，只有明确设置为0才是打烊中
            if (value == null) {
                return true;
            }

            // 处理可能的类型：Integer, Long, String 等
            int status;
            if (value instanceof Number) {
                status = ((Number) value).intValue();
            } else if (value instanceof String) {
                status = Integer.parseInt((String) value);
            } else {
                log.warn("Redis 中营业状态的类型不符合预期: {}", value.getClass().getName());
                return true;
            }

            boolean isOpen = status == 1;
            log.info("店铺{} 营业状态: {}", shopId, isOpen ? "营业中" : "打烊中");
            return isOpen;
        } catch (Exception e) {
            log.error("读取 Redis 营业状态失败, shopId={}, 默认返回营业中", shopId, e);
            return true; // Redis 故障时默认营业中，避免全部店铺不可见
        }
    }

    /**
     * 过滤掉打烊中的店铺，只保留营业中的店铺
     */
    private List<ShopVO> filterClosedShops(List<ShopVO> shopVOList) {
        int beforeCount = shopVOList.size();
        List<ShopVO> filtered = shopVOList.stream()
                .filter(shop -> isShopOpen(shop.getId()))
                .collect(Collectors.toList());
        int removedCount = beforeCount - filtered.size();
        if (removedCount > 0) {
            log.info("过滤掉 {} 个打烊中的店铺，剩余 {} 个营业中的店铺", removedCount, filtered.size());
        }
        return filtered;
    }

    /**
     * 分页查询店铺信息
     *
     * @param page     页码
     * @param pageSize 每页记录数
     * @return 店铺分页结果
     */
    @Override
    public PageResult pageQuery(int page, int pageSize) {
        PageHelper.startPage(page, pageSize);

        //直接设置审核状态
        // 只查询审核通过且状态为启用的店铺
        Page<Shop> shops = shopMapper.pageQueryForUser(StatusConstant.ENABLE, 1);

        // 将Shop转换为ShopVO
        List<ShopVO> shopVOList = shops.getResult().stream().map(shop -> {
            ShopVO shopVO = new ShopVO();
            BeanUtils.copyProperties(shop, shopVO);
            return shopVO;
        }).collect(Collectors.toList());

        // 过滤掉打烊中的店铺
        shopVOList = filterClosedShops(shopVOList);

        return new PageResult(shopVOList.size(), shopVOList);
    }

    @Override
    public PageResult pageQueryByRule(int page, int pageSize, String rule) {

        PageHelper.startPage(page, pageSize);

        // 只查询审核通过且状态为启用的店铺
        Page<Shop> shops = shopMapper.pageQueryByRule(StatusConstant.ENABLE, 1, rule);

        // 将Shop转换为ShopVO
        List<ShopVO> shopVOList = shops.getResult().stream().map(shop -> {
            ShopVO shopVO = new ShopVO();
            BeanUtils.copyProperties(shop, shopVO);
            return shopVO;
        }).collect(Collectors.toList());

        // 过滤掉打烊中的店铺
        shopVOList = filterClosedShops(shopVOList);

        return new PageResult(shopVOList.size(), shopVOList);
    }

    @Override
    public PageResult pageQueryByCategoryId(int page, int pageSize, Long categoryId) {
        PageHelper.startPage(page, pageSize);
        Page<Shop> shops = shopMapper.pageQueryByCategoryId(categoryId);

        // 转换为 ShopVO 并过滤打烊店铺
        List<ShopVO> shopVOList = shops.getResult().stream().map(shop -> {
            ShopVO shopVO = new ShopVO();
            BeanUtils.copyProperties(shop, shopVO);
            return shopVO;
        }).collect(Collectors.toList());
        shopVOList = filterClosedShops(shopVOList);

        return new PageResult(shopVOList.size(), shopVOList);
    }

    /**
     * 根据分类id分页查询店铺，支持排序规则
     * @param page 页码
     * @param pageSize 每页记录数
     * @param categoryId 分类id
     * @param rule 排序规则
     * @return 分页结果
     */
    @Override
    public PageResult pageQueryByCategoryIdWithRule(int page, int pageSize, Long categoryId, String rule) {
        PageHelper.startPage(page, pageSize);

        // 根据不同的排序规则选择不同的查询方法
        Page<Shop> shops;
        if ("default".equals(rule)) {
            // 默认排序（创建时间降序）
            shops = shopMapper.pageQueryByCategoryId(categoryId);
        } else {
            // 自定义排序（如score或order_quantity）
            shops = shopMapper.pageQueryByCategoryIdWithRule(categoryId, rule);
        }

        // 将Shop转换为ShopVO
        List<ShopVO> shopVOList = shops.getResult().stream().map(shop -> {
            ShopVO shopVO = new ShopVO();
            BeanUtils.copyProperties(shop, shopVO);
            return shopVO;
        }).collect(Collectors.toList());

        // 过滤掉打烊中的店铺
        shopVOList = filterClosedShops(shopVOList);

        return new PageResult(shopVOList.size(), shopVOList);
    }

    /**
     * 根据店铺名称模糊搜索店铺
     * @param page 页码
     * @param pageSize 每页记录数
     * @param shopName 店铺名称关键词
     * @return 分页结果
     */
    @Override
    public PageResult pageQueryByName(int page, int pageSize, String shopName) {
        PageHelper.startPage(page, pageSize);
        Page<Shop> shops = shopMapper.pageQueryByName(shopName);

        // 将Shop转换为ShopVO
        List<ShopVO> shopVOList = shops.getResult().stream().map(shop -> {
            ShopVO shopVO = new ShopVO();
            BeanUtils.copyProperties(shop, shopVO);
            return shopVO;
        }).collect(Collectors.toList());

        // 过滤掉打烊中的店铺
        shopVOList = filterClosedShops(shopVOList);

        return new PageResult(shopVOList.size(), shopVOList);
    }

    /**
     * 根据店铺名称和排序规则模糊搜索店铺
     * @param page 页码
     * @param pageSize 每页记录数
     * @param shopName 店铺名称关键词
     * @param rule 排序规则
     * @return 分页结果
     */
    @Override
    public PageResult pageQueryByNameWithRule(int page, int pageSize, String shopName, String rule) {
        PageHelper.startPage(page, pageSize);

        // 根据不同的排序规则选择不同的查询方法
        Page<Shop> shops;
        if ("default".equals(rule)) {
            // 默认排序（创建时间降序）
            shops = shopMapper.pageQueryByName(shopName);
        } else {
            // 自定义排序（如score或sales）
            shops = shopMapper.pageQueryByNameWithRule(shopName, rule);
        }

        // 将Shop转换为ShopVO
        List<ShopVO> shopVOList = shops.getResult().stream().map(shop -> {
            ShopVO shopVO = new ShopVO();
            BeanUtils.copyProperties(shop, shopVO);
            return shopVO;
        }).collect(Collectors.toList());

        // 过滤掉打烊中的店铺
        shopVOList = filterClosedShops(shopVOList);

        return new PageResult(shopVOList.size(), shopVOList);
    }
}
