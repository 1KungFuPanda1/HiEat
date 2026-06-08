package com.sky.vo;

import com.sky.entity.ReviewPhoto;
import com.sky.entity.ReviewReply;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewVO {
  private Long id;
  private Long userId;
  private Long shopId;
  private Integer rating;
  private String title;
  private String content;
  private LocalDateTime createTime;
  private String status;

  private String userName;
  private String userAvatar;
  private List<String> photoUrls;
  private List<ReviewReply> replies;
}