package com.sky.service.impl;

import com.sky.entity.ChArea;
import com.sky.mapper.ChAreaMapper;
import com.sky.service.ChAreaService;
import com.sky.vo.AreaVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 区域服务实现类
 */
@Service
@Slf4j
public class ChAreaServiceImpl implements ChAreaService {

    @Autowired
    private ChAreaMapper chAreaMapper;

    /**
     * 获取省市区树形结构
     * @return 省市区树形结构
     */
    @Override
    public List<AreaVO> getAreaTree() {
        // 获取所有区域数据
        List<ChArea> allAreas = chAreaMapper.selectAll();
        
        // 转换为VO对象
        List<AreaVO> areaVOList = allAreas.stream().map(area -> {
            AreaVO areaVO = new AreaVO();
            BeanUtils.copyProperties(area, areaVO);
            return areaVO;
        }).collect(Collectors.toList());
        
        // 把所有地区，按它们的父 ID 分组，生成一个 Map <父 ID, 子地区列表>
        //  父ID=0 → [省1, 省2, 省3]
        //  父ID=1 → [市1, 市2]
        //  父ID=10 → [区1, 区2]
        Map<Long, List<AreaVO>> parentMap = areaVOList.stream()
                .collect(Collectors.groupingBy(AreaVO::getParentId));
        
        // 构建树形结构
        /**遍历每一个地区：
           用这个地区的ID → 去分组Map里找它的子地区
           把子地区设置进去
         */
        areaVOList.forEach(areaVO -> {
            List<AreaVO> children = parentMap.get(areaVO.getAreaId());
            areaVO.setChildren(children);
        });
        
        // 返回顶级节点（省级）
        return areaVOList.stream()
                .filter(areaVO -> areaVO.getLevel() == 1)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定省的所有市
     * @param provinceId 省ID
     * @return 市列表
     */
    @Override
    public List<AreaVO> getCitiesByProvince(Long provinceId) {
        List<ChArea> cities = chAreaMapper.selectByParentId(provinceId);
        return convertToVOList(cities);
    }

    /**
     * 获取指定市的所有区/县
     * @param cityId 市ID
     * @return 区/县列表
     */
    @Override
    public List<AreaVO> getDistrictsByCity(Long cityId) {
        List<ChArea> districts = chAreaMapper.selectByParentId(cityId);
        return convertToVOList(districts);
    }
    
    /**
     * 将实体列表转换为VO列表
     * @param areaList 实体列表
     * @return VO列表
     */
    private List<AreaVO> convertToVOList(List<ChArea> areaList) {
        if (areaList == null || areaList.isEmpty()) {
            return new ArrayList<>();
        }
        
        return areaList.stream().map(area -> {
            AreaVO areaVO = new AreaVO();
            BeanUtils.copyProperties(area, areaVO);
            return areaVO;
        }).collect(Collectors.toList());
    }
}