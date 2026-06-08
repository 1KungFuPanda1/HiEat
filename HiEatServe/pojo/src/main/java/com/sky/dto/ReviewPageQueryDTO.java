package com.sky.dto;

import lombok.Data;

@Data
public class ReviewPageQueryDTO {
  private Long shopId;
  //评分
  private Integer rating;
  private String status;
  private int page;
  private int pageSize;
}