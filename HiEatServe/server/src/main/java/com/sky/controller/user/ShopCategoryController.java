package com.sky.controller.user;

import com.sky.entity.ShopCategory;
import com.sky.result.Result;
import com.sky.service.ShopCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 店铺分类(用户端)
 */
@RestController("userShopCategoryController")
@RequestMapping("/user/shopCategory")
@Api(tags = "店铺分类接口")
@Slf4j
public class ShopCategoryController {

    @Autowired
    private ShopCategoryService shopCategoryService;

    /**
     * 查询所有启用的店铺分类(上面的小图标）
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询所有启用的店铺分类")
    public Result<List<ShopCategory>> list() {
        log.info("查询所有启用的店铺分类");
        List<ShopCategory> list = shopCategoryService.listByStatus(1);
        return Result.success(list);
    }
} 