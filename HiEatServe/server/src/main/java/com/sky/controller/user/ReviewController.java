package com.sky.controller.user;

import com.sky.dto.ReviewDTO;
import com.sky.dto.ReviewPageQueryDTO;
import com.sky.dto.ReviewReplyDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.ReviewService;
import com.sky.vo.ReviewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/review")
@Api(tags = "C端-评论相关接口")
@Slf4j
public class ReviewController {

  @Autowired
  private ReviewService reviewService;

  /**
   * 新增评论
   * 
   * @param reviewDTO
   * @return
   */
  @PostMapping
  @ApiOperation("新增评论")
  public Result<String> add(@RequestBody ReviewDTO reviewDTO) {
    log.info("新增评论：{}", reviewDTO);
    reviewService.addReview(reviewDTO);
    return Result.success("评论成功");
  }

  /**
   * 分页查询评论
   * 
   * @param reviewPageQueryDTO
   * @return
   */
  @GetMapping("/page")
  @ApiOperation("分页查询评论")
  public Result<PageResult> page(ReviewPageQueryDTO reviewPageQueryDTO) {
    log.info("分页查询评论：{}", reviewPageQueryDTO);
    PageResult pageResult = reviewService.pageQuery(reviewPageQueryDTO);
    return Result.success(pageResult);
  }

  /**
   * 根据店铺ID查询评论
   * 
   * @param shopId
   * @return
   */
  @GetMapping("/shop/{shopId}")
  @ApiOperation("根据店铺ID查询评论")
  public Result<List<ReviewVO>> getByShopId(@PathVariable Long shopId) {
    log.info("根据店铺ID查询评论：{}", shopId);
    List<ReviewVO> list = reviewService.getByShopId(shopId);
    return Result.success(list);
  }

  /**
   * 根据店铺ID和评分查询评论
   * 
   * @param shopId
   * @param rating
   * @return
   */
  @GetMapping("/shop/{shopId}/rating/{rating}")
  @ApiOperation("根据店铺ID和评分查询评论")
  public Result<List<ReviewVO>> getByShopIdAndRating(@PathVariable Long shopId, @PathVariable Integer rating) {
    log.info("根据店铺ID和评分查询评论：shopId={}, rating={}", shopId, rating);
    List<ReviewVO> list = reviewService.getByShopIdAndRating(shopId, rating);
    return Result.success(list);
  }
}