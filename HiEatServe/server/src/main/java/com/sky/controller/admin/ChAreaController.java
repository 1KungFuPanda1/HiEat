package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ChAreaService;
import com.sky.vo.AreaVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 区域管理控制器
 */
@RestController
@RequestMapping("/admin/area")
@Api(tags = "区域管理相关接口")
@Slf4j
public class ChAreaController {

    @Autowired
    private ChAreaService chAreaService;

    /**
     * 获取省市区树形结构
     * @return 省市区树形结构
     */
    @GetMapping("/tree")
    @ApiOperation("获取省市区树形结构")
    public Result<List<AreaVO>> getAreaTree() {
        log.info("获取省市区树形结构");
        List<AreaVO> areaTree = chAreaService.getAreaTree();
        return Result.success(areaTree);
    }

    /**
     * 获取指定省的所有市
     * @param provinceId 省ID
     * @return 市列表
     */
    @GetMapping("/cities/{provinceId}")
    @ApiOperation("获取指定省的所有市")
    public Result<List<AreaVO>> getCitiesByProvince(@PathVariable Long provinceId) {
        log.info("获取指定省的所有市：{}", provinceId);
        List<AreaVO> cities = chAreaService.getCitiesByProvince(provinceId);
        return Result.success(cities);
    }

    /**
     * 获取指定市的所有区/县
     * @param cityId 市ID
     * @return 区/县列表
     */
    @GetMapping("/districts/{cityId}")
    @ApiOperation("获取指定市的所有区/县")
    public Result<List<AreaVO>> getDistrictsByCity(@PathVariable Long cityId) {
        log.info("获取指定市的所有区/县：{}", cityId);
        List<AreaVO> districts = chAreaService.getDistrictsByCity(cityId);
        return Result.success(districts);
    }
}