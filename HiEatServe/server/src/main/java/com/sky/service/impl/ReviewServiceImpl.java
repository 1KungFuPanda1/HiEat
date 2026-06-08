package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.ReviewDTO;
import com.sky.dto.ReviewPageQueryDTO;
import com.sky.dto.ReviewReplyDTO;
import com.sky.entity.*;
import com.sky.mapper.ReviewMapper;
import com.sky.mapper.ReviewPhotoMapper;
import com.sky.mapper.ReviewReplyMapper;
import com.sky.mapper.ShopMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.PageResult;
import com.sky.service.ReviewService;
import com.sky.vo.ReviewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReviewServiceImpl implements ReviewService {

  @Autowired
  private ReviewMapper reviewMapper;

  @Autowired
  private ReviewPhotoMapper reviewPhotoMapper;

  @Autowired
  private ReviewReplyMapper reviewReplyMapper;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private ShopMapper shopMapper;

  /**
   * 新增评论
   * 
   * @param reviewDTO
   */
  @Override
  @Transactional
  public void addReview(ReviewDTO reviewDTO) {
    // 获取当前登录用户ID
    Long userId = BaseContext.getCurrentId();

    // 创建评论对象
    Review review = new Review();
    BeanUtils.copyProperties(reviewDTO, review);
    review.setUserId(userId);
    review.setCreateTime(LocalDateTime.now());
    review.setUpdateTime(LocalDateTime.now());
    review.setStatus("active");

    // 保存评论
    reviewMapper.insert(review);

    log.info("review add id:{}", review);

    // 保存评论图片
    if (reviewDTO.getPhotoUrls() != null && !reviewDTO.getPhotoUrls().isEmpty()) {
      List<ReviewPhoto> photoList = reviewDTO.getPhotoUrls().stream()
          .map(url -> {
            ReviewPhoto photo = new ReviewPhoto();
            photo.setReviewId(review.getId());
            photo.setPhotoUrl(url);
            return photo;
          })
          .collect(Collectors.toList());
      reviewPhotoMapper.insertBatch(photoList);
    }

    // 更新店铺评分
    Integer reviewCount = reviewMapper.countReviewsByShopId(review.getShopId());
    Shop shop = shopMapper.getById(review.getShopId());
    // 使用 BigDecimal 精确计算，保留一位小数
    BigDecimal newScore = BigDecimal.valueOf(review.getRating())
            .add(BigDecimal.valueOf(shop.getScore()).multiply(BigDecimal.valueOf(reviewCount)))
            .divide(BigDecimal.valueOf(reviewCount + 1), 1, RoundingMode.HALF_UP);
    shop.setScore(newScore.doubleValue());
    shopMapper.update(shop);
  }

  /**
   * 分页查询评论
   * 
   * @param reviewPageQueryDTO
   * @return
   */
  @Override
  public PageResult pageQuery(ReviewPageQueryDTO reviewPageQueryDTO) {
    PageHelper.startPage(reviewPageQueryDTO.getPage(), reviewPageQueryDTO.getPageSize());
    Page<Review> page = reviewMapper.pageQuery(reviewPageQueryDTO);

    // 查询评论的用户信息、图片和回复
    List<ReviewVO> reviewVOList = page.getResult().stream()
        .map(this::convertToReviewVO)
        .collect(Collectors.toList());

    return new PageResult(page.getTotal(), reviewVOList);
  }

  /**
   * 根据店铺ID查询评论
   * 
   * @param shopId
   * @return
   */
  @Override
  public List<ReviewVO> getByShopId(Long shopId) {
    List<Review> reviews = reviewMapper.getByShopId(shopId);
    return reviews.stream()
        .map(this::convertToReviewVO)
        .collect(Collectors.toList());
  }

  /**
   * 根据店铺ID和评分查询评论
   * 
   * @param shopId
   * @param rating
   * @return
   */
  @Override
  public List<ReviewVO> getByShopIdAndRating(Long shopId, Integer rating) {
    List<Review> reviews = reviewMapper.getByShopIdAndRating(shopId, rating);
    return reviews.stream()
        .map(this::convertToReviewVO)
        .collect(Collectors.toList());
  }

  /**
   * 回复评论
   * 
   * @param reviewReplyDTO
   */
  @Override
  @Transactional
  public void replyReview(ReviewReplyDTO reviewReplyDTO) {
    // 获取当前登录用户ID
    Long userId = BaseContext.getCurrentId();

    // 创建回复对象
    ReviewReply reviewReply = new ReviewReply();
    reviewReply.setReviewId(reviewReplyDTO.getReviewId());
    reviewReply.setUserId(userId);
    reviewReply.setContent(reviewReplyDTO.getContent());
    reviewReply.setCreateTime(LocalDateTime.now());

    // 保存回复
    reviewReplyMapper.insert(reviewReply);
  }

  /**
   * 更新评论状态
   * 
   * @param id
   * @param status
   */
  @Override
  public void updateStatus(Long id, String status) {
    reviewMapper.updateStatus(id, status);
  }

  /**
   * 将Review转换为ReviewVO
   * 
   * @param review
   * @return
   */
  private ReviewVO convertToReviewVO(Review review) {
    ReviewVO reviewVO = new ReviewVO();
    BeanUtils.copyProperties(review, reviewVO);

    // 查询用户信息
    User user = userMapper.getById(review.getUserId());
    if (user != null) {
      reviewVO.setUserName(user.getName());
      reviewVO.setUserAvatar(user.getAvatar());
    }

    // 查询评论图片
    List<ReviewPhoto> photos = reviewPhotoMapper.getByReviewId(review.getId());
    List<String> photoUrls = photos.stream()
        .map(ReviewPhoto::getPhotoUrl)
        .collect(Collectors.toList());
    reviewVO.setPhotoUrls(photoUrls);

    // 查询评论回复
    List<ReviewReply> replies = reviewReplyMapper.getByReviewId(review.getId());

    // 查询回复用户信息
    for (ReviewReply reply : replies) {
      User replyUser = userMapper.getById(reply.getUserId());
      if (replyUser != null) {
        reply.setUserName(replyUser.getName());
        reply.setUserAvatar(replyUser.getAvatar());
      }
    }

    reviewVO.setReplies(replies);

    return reviewVO;
  }
}