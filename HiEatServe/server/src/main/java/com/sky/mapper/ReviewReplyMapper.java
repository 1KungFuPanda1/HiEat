package com.sky.mapper;

import com.sky.entity.ReviewReply;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewReplyMapper {

  /**
   * 新增评论回复
   * 
   * @param reviewReply
   */
  @Insert("insert into review_replie (review_id, user_id, content, create_time) " +
      "values (#{reviewId}, #{userId}, #{content}, #{createTime})")
  void insert(ReviewReply reviewReply);

  /**
   * 根据评论ID查询回复
   * 
   * @param reviewId
   * @return
   */
  @Select("select * from review_replie where review_id = #{reviewId} order by create_time asc")
  List<ReviewReply> getByReviewId(Long reviewId);
}