package com.sky.controller.user;

import com.sky.entity.Carousel;
import com.sky.result.Result;
import com.sky.service.CarouselService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 轮播图(用户端)
 */
@RestController("userCarouselController")
@RequestMapping("/user/carousel")
@Api(tags = "轮播图接口")
@Slf4j
public class CarouselController {

    @Autowired
    private CarouselService carouselService;

    /**
     * 获取所有轮播图
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("获取所有轮播图")
    public Result<List<Carousel>> list() {
        log.info("获取所有轮播图");
        List<Carousel> list = carouselService.list();
        return Result.success(list);
    }
} 