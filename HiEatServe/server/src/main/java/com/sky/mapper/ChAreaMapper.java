package com.sky.mapper;

import com.sky.entity.ChArea;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 区域信息Mapper
 */
@Mapper
public interface ChAreaMapper {

  /**
   * 查询所有区域信息
   * 
   * @return 区域列表
   */
  @Select("select * from ch_area")
  List<ChArea> selectAll();

  /**
   * 根据父ID查询区域
   * 
   * @param parentId 父ID
   * @return 区域列表
   */
  @Select("select * from ch_area where parent_id = #{parentId}")
  List<ChArea> selectByParentId(Long parentId);

  /**
   * 查询所有省级区域
   * 
   * @return 省级区域列表
   */
  @Select("select * from ch_area where level = 1")
  List<ChArea> selectProvinces();
}