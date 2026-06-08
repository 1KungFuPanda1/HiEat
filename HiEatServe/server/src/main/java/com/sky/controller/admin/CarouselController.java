package com.sky.controller.admin;

import com.sky.dto.CarouselDTO;
import com.sky.entity.Carousel;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CarouselService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 轮播图管理
 */
@RestController
@RequestMapping("/admin/carousel")
@Api(tags = "轮播图管理接口")
@Slf4j
public class CarouselController {

    @Autowired
    private CarouselService carouselService;

    /**
     * 新增轮播图
     * @param carouselDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增轮播图")
    public Result<String> save(@RequestBody CarouselDTO carouselDTO) {
        log.info("新增轮播图：{}", carouselDTO);
        carouselService.save(carouselDTO);
        return Result.success();
    }

    /**
     * 删除轮播图
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除轮播图")
    public Result<String> delete(@PathVariable Long id) {
        log.info("删除轮播图：{}", id);
        carouselService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改轮播图
     * @param carouselDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改轮播图")
    public Result<String> update(@RequestBody CarouselDTO carouselDTO) {
        log.info("修改轮播图：{}", carouselDTO);
        carouselService.update(carouselDTO);
        return Result.success();
    }

    /**
     * 根据id查询轮播图
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询轮播图")
    public Result<Carousel> getById(@PathVariable Long id) {
        log.info("根据id查询轮播图：{}", id);
        Carousel carousel = carouselService.getById(id);
        return Result.success(carousel);
    }

    /**
     * 轮播图分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("轮播图分页查询")
    public Result<PageResult> page(int page, int pageSize) {
        log.info("轮播图分页查询：page={}, pageSize={}", page, pageSize);
        PageResult pageResult = carouselService.pageQuery(page, pageSize);
        return Result.success(pageResult);
    }
} 