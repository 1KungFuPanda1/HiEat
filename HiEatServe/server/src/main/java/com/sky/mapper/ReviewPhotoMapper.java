package com.sky.mapper;

import com.sky.entity.ReviewPhoto;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewPhotoMapper {

  /**
   * 批量插入评论图片
   * 
   * @param reviewPhotos
   */
  void insertBatch(List<ReviewPhoto> reviewPhotos);

  /**
   * 根据评论ID查询图片
   * 
   * @param reviewId
   * @return
   */
  @Select("select * from review_photo where review_id = #{reviewId}")
  List<ReviewPhoto> getByReviewId(Long reviewId);

  /**
   * 根据评论ID删除图片
   * 
   * @param reviewId
   */
  @Delete("delete from review_photo where review_id = #{reviewId}")
  void deleteByReviewId(Long reviewId);
}