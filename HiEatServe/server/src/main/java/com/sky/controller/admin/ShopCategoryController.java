package com.sky.controller.admin;

import com.sky.dto.ShopCategoryDTO;
import com.sky.entity.ShopCategory;
import com.sky.result.Result;
import com.sky.service.ShopCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 店铺分类管理
 */
@RestController
@RequestMapping("/admin/shopCategory")
@Api(tags = "店铺分类管理接口")
@Slf4j
public class ShopCategoryController {

    @Autowired
    private ShopCategoryService shopCategoryService;

    /**
     * 新增店铺分类
     * @param shopCategoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增店铺分类")
    public Result<String> save(@RequestBody ShopCategoryDTO shopCategoryDTO) {
        log.info("新增店铺分类：{}", shopCategoryDTO);
        
        ShopCategory shopCategory = new ShopCategory();
        BeanUtils.copyProperties(shopCategoryDTO, shopCategory);
        
        shopCategoryService.save(shopCategory);
        return Result.success();
    }

    /**
     * 删除店铺分类
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除店铺分类")
    public Result<String> delete(@PathVariable Long id) {
        log.info("删除店铺分类：{}", id);
        shopCategoryService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改店铺分类
     * @param shopCategoryDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改店铺分类")
    public Result<String> update(@RequestBody ShopCategoryDTO shopCategoryDTO) {
        log.info("修改店铺分类：{}", shopCategoryDTO);
        
        ShopCategory shopCategory = new ShopCategory();
        BeanUtils.copyProperties(shopCategoryDTO, shopCategory);
        
        shopCategoryService.update(shopCategory);
        return Result.success();
    }

    /**
     * 根据id查询店铺分类
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询店铺分类")
    public Result<ShopCategory> getById(@PathVariable Long id) {
        log.info("根据id查询店铺分类：{}", id);
        ShopCategory shopCategory = shopCategoryService.getById(id);
        return Result.success(shopCategory);
    }

    /**
     * 查询所有店铺分类
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询所有店铺分类")
    public Result<List<ShopCategory>> list() {
        log.info("查询所有店铺分类");
        List<ShopCategory> list = shopCategoryService.list();
        return Result.success(list);
    }
    
    /**
     * 启用或禁用店铺分类
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用或禁用店铺分类")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用或禁用店铺分类：{},{}", status, id);
        shopCategoryService.startOrStop(status, id);
        return Result.success();
    }
} 