package com.sky.service;

import com.sky.dto.ReviewDTO;
import com.sky.dto.ReviewPageQueryDTO;
import com.sky.dto.ReviewReplyDTO;
import com.sky.result.PageResult;
import com.sky.vo.ReviewVO;

import java.util.List;

public interface ReviewService {

  /**
   * 新增评论
   * 
   * @param reviewDTO
   * @return
   */
  void addReview(ReviewDTO reviewDTO);

  /**
   * 分页查询评论
   * 
   * @param reviewPageQueryDTO
   * @return
   */
  PageResult pageQuery(ReviewPageQueryDTO reviewPageQueryDTO);

  /**
   * 根据店铺ID查询评论
   * 
   * @param shopId
   * @return
   */
  List<ReviewVO> getByShopId(Long shopId);

  /**
   * 根据店铺ID和评分查询评论
   * 
   * @param shopId
   * @param rating
   * @return
   */
  List<ReviewVO> getByShopIdAndRating(Long shopId, Integer rating);

  /**
   * 回复评论
   * 
   * @param reviewReplyDTO
   */
  void replyReview(ReviewReplyDTO reviewReplyDTO);

  /**
   * 更新评论状态
   * 
   * @param id
   * @param status
   */
  void updateStatus(Long id, String status);
}