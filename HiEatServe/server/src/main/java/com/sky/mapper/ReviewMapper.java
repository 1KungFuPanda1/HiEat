package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.ReviewPageQueryDTO;
import com.sky.entity.Review;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewMapper {

  /**
   * 新增评论
   * 
   * @param review
   */
  @Insert("insert into review (user_id, shop_id, rating, title, content, create_time, update_time, status) " +
      "values (#{userId}, #{shopId}, #{rating}, #{title}, #{content}, #{createTime}, #{updateTime}, #{status})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insert(Review review);

  /**
   * 分页查询评论
   * 
   * @param reviewPageQueryDTO
   * @return
   */
  Page<Review> pageQuery(ReviewPageQueryDTO reviewPageQueryDTO);

  /**
   * 根据ID查询评论
   * 
   * @param id
   * @return
   */
  @Select("select * from review where id = #{id}")
  Review getById(Long id);

  /**
   * 根据店铺ID查询评论
   * 
   * @param shopId
   * @return
   */
  @Select("select * from review where shop_id = #{shopId} and status = 'active' order by create_time desc")
  List<Review> getByShopId(Long shopId);

  /**
   * 根据店铺ID和评分查询评论
   * 
   * @param shopId
   * @param rating
   * @return
   */
  @Select("select * from review where shop_id = #{shopId} and rating = #{rating} and status = 'active' order by create_time desc")
  List<Review> getByShopIdAndRating(Long shopId, Integer rating);

  /**
   * 更新评论状态
   * 
   * @param id
   * @param status
   */
  @Select("update review set status = #{status} where id = #{id}")
  void updateStatus(Long id, String status);

  /**
   * 根据店铺id查询评论数量
   * 
   * @param shopId 店铺id
   * @return 评论数量
   */
  @Select("select count(id) from review where shop_id = #{shopId}")
  Integer countReviewsByShopId(Long shopId);
}