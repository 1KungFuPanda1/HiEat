package com.sky.service;

import com.sky.vo.AreaVO;

import java.util.List;

/**
 * 区域服务接口
 */
public interface ChAreaService {

  /**
   * 获取省市区树形结构
   * 
   * @return 省市区树形结构
   */
  List<AreaVO> getAreaTree();

  /**
   * 获取指定省的所有市
   * 
   * @param provinceId 省ID
   * @return 市列表
   */
  List<AreaVO> getCitiesByProvince(Long provinceId);

  /**
   * 获取指定市的所有区/县
   * 
   * @param cityId 市ID
   * @return 区/县列表
   */
  List<AreaVO> getDistrictsByCity(Long cityId);
}