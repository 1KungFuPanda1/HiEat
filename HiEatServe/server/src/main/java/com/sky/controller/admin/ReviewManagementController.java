package com.sky.controller.admin;

import com.sky.dto.ReviewPageQueryDTO;
import com.sky.dto.ReviewReplyDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.ReviewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/review")
@Api(tags = "管理端-评论管理相关接口")
@Slf4j
public class ReviewManagementController {

  @Autowired
  private ReviewService reviewService;

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
   * 回复评论
   * 
   * @param reviewReplyDTO
   * @return
   */
  @PostMapping("/reply")
  @ApiOperation("回复评论")
  public Result<String> reply(@RequestBody ReviewReplyDTO reviewReplyDTO) {
    log.info("回复评论：{}", reviewReplyDTO);
    reviewService.replyReview(reviewReplyDTO);
    return Result.success("回复成功");
  }

  /**
   * 更新评论状态
   * 
   * @param id
   * @param status
   * @return
   */
  @PutMapping("/status")
  @ApiOperation("更新评论状态")
  public Result<String> updateStatus(Long id, String status) {
    log.info("更新评论状态：id={}, status={}", id, status);
    reviewService.updateStatus(id, status);
    return Result.success("操作成功");
  }
}