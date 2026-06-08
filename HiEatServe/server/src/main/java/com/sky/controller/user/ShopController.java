package com.sky.controller.user;
import com.sky.entity.Shop;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "C端-店铺相关接口")
@Slf4j
public class ShopController {

    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ShopService shopService;

    @GetMapping("/status/{shopId}")
    @ApiOperation("获取店铺的营业状态")
    public Result<Integer> getStatus(@PathVariable Long shopId) {
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY + "-" + shopId);
        // redis中无商家营业状态，默认为营业中
        if(status == null) {
            status = 1;
        }
        log.info("获取到店铺{}的营业状态为：{}", shopId, status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }

    @GetMapping ("/{shopId}")
    @ApiOperation("根据店铺id获取店铺信息")
    public Result<Shop> getShopById(@PathVariable Long shopId) {
        Shop shop = shopService.getShopInfo(shopId);
        return Result.success(shop);
    }

    /**
     * 首页分页查询店铺信息
     * 
     * @param page     页码
     * @param pageSize 每页记录数
     * @return 店铺分页结果
     */
    @GetMapping("/page")
    @ApiOperation("分页查询店铺信息")
    public Result<PageResult> page(int page, int pageSize) {
        log.info("分页查询店铺信息：page={}, pageSize={}", page, pageSize);
        PageResult pageResult = shopService.pageQuery(page, pageSize);
        return Result.success(pageResult);
    }

    /**
     * 获取 所有店铺 ，按销量优先排序
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/count")
    @ApiOperation("销量优先获取店铺列表")
    public Result<PageResult> count(int page, int pageSize) {
        log.info("销量优先获取店铺列表：page={}, pageSize={}", page, pageSize);
        PageResult pageResult = shopService.pageQueryByRule(page, pageSize, "order_quantity");
        return Result.success(pageResult);
    }

    /**
     * 获取 所有店铺 ，按评分排序
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/score")
    @ApiOperation("评分优先获取店铺列表")
    public Result<PageResult> score(int page, int pageSize) {
        log.info("评分优先获取店铺列表：page={}, pageSize={}", page, pageSize);
        PageResult pageResult = shopService.pageQueryByRule(page, pageSize, "score");
        return Result.success(pageResult);
    }

    /**
     *获取 指定分类下 的店铺，按指定规则排序
     * @param page
     * @param pageSize
     * @param categoryId
     * @param rule
     * @return
     */
    @GetMapping("/category/page")
    @ApiOperation("通过分类id获取店铺列表")
    public Result<PageResult> category(int page, int pageSize, Long categoryId, String rule) {
        log.info("通过分类id获取店铺列表：page={}, pageSize={}, categoryId={}, rule={}", page, pageSize, categoryId, rule);
        
        // 如果rule参数为空，默认使用"default"排序规则
        if (rule == null || rule.isEmpty()) {
            rule = "default";
        }
        
        PageResult pageResult = shopService.pageQueryByCategoryIdWithRule(page, pageSize, categoryId, rule);
        return Result.success(pageResult);
    }

    @GetMapping("/search")
    @ApiOperation("通过店铺名称搜索店铺")
    public Result<PageResult> search(int page, int pageSize, String keyword, String rule) {
        log.info("搜索店铺：page={}, pageSize={}, keyword={}, rule={}", page, pageSize, keyword, rule);
        
        // 如果关键词为空，返回空结果
        if (keyword == null || keyword.isEmpty()) {
            return Result.success(new PageResult(0, new ArrayList<>()));
        }
        
        // 如果rule参数为空，默认使用"default"排序规则
        if (rule == null || rule.isEmpty()) {
            rule = "default";
        }
        
        PageResult pageResult = shopService.pageQueryByNameWithRule(page, pageSize, keyword, rule);
        return Result.success(pageResult);
    }

}
