package com.sky.controller.admin;

import com.sky.dto.SetShopStatusDTO;
import com.sky.dto.ShopAuditDTO;
import com.sky.entity.Shop;
import com.sky.dto.admin.shop.ShopUpdateDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.ShopService;
import com.sky.utils.JwtUtil;
import com.sky.vo.ShopVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {

    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ShopService shopService;

    /**
     * 设置店铺的营业状态
     */
    @PutMapping("/set")
    @ApiOperation("设置店铺的营业状态")
    public Result setStatus(@RequestBody SetShopStatusDTO setShopStatusDTO) {

        log.info("设置店铺{}的营业状态为：{}", setShopStatusDTO.getShopId(), setShopStatusDTO.getStatus() == 1 ? "营业中" : "打烊中");
        redisTemplate.opsForValue().set(KEY + "-" + setShopStatusDTO.getShopId(), setShopStatusDTO.getStatus());
        return Result.success();
    }

    /**
     * 获取店铺的营业状态
     * 
     * @return
     */
    @GetMapping("/status/{shopId}")
    @ApiOperation("获取店铺的营业状态")
    public Result<Integer> getStatus(@PathVariable Long shopId) {
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY + "-" + shopId);
        // redis中无商家营业状态
        if(status == null) {
            // 设置为营业中，返回营业状态
            redisTemplate.opsForValue().set(KEY + "-" + shopId, 1);
            status = 1;
        }
        log.info("获取到店铺的营业状态为：{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }

    @GetMapping("/getInfo/{id}")
    @ApiOperation("获取店铺信息")
    public Result<ShopVO> getShopInfo(@PathVariable Long id) {
        log.info("获取店铺信息：{}", id);
        ShopVO shopVO = shopService.getShopInfoWithImages(id);
        return Result.success(shopVO);
    }

    @PostMapping("/update")
    public Result<String> saveShop(@RequestBody ShopUpdateDTO shopUpdateDTO) {
        log.info("保存店铺信息{}", shopUpdateDTO);
        shopService.updateShop(shopUpdateDTO);
        return Result.success("保存成功");
    }

    /**
     * 店铺审核列表
     * 
     * @param page
     * @param pageSize
     * @param auditStatus
     * @return
     */
    @GetMapping("/audit/page")
    @ApiOperation("店铺审核列表")
    public Result<PageResult> auditPage(int page, int pageSize, Integer auditStatus) {
        log.info("店铺审核列表：page={}, pageSize={}, auditStatus={}", page, pageSize, auditStatus);
        PageResult pageResult = shopService.auditPage(page, pageSize, auditStatus);
        return Result.success(pageResult);
    }

    /**
     * 店铺审核
     * 
     * @param shopAuditDTO
     * @return
     */
    @PostMapping("/audit")
    @ApiOperation("店铺审核")
    public Result audit(@RequestBody ShopAuditDTO shopAuditDTO) {
        log.info("店铺审核：{}", shopAuditDTO);
        shopService.audit(shopAuditDTO);
        return Result.success();
    }

    /**
     * 获取不同审核状态的店铺数量
     * 
     * @return 包含不同状态店铺数量的Map
     */
    @GetMapping("/audit/counts")
    @ApiOperation("获取不同审核状态的店铺数量")
    public Result<Map<String, Integer>> getShopCounts() {
        log.info("获取不同审核状态的店铺数量");
        Map<String, Integer> countMap = shopService.getShopCounts();
        return Result.success(countMap);
    }
}
