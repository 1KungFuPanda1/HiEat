package com.sky.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReviewDTO {
  private Long shopId;
  //评分
  private Integer rating;
  private String title;
  private String content;
  private List<String> photoUrls;
}