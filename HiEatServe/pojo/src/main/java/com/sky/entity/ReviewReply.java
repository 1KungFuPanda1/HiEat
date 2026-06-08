package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReply implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long id;
  private Long reviewId;
  private Long userId;
  private String content;
  private LocalDateTime createTime;

  // 非数据库字段
  private String userName;
  private String userAvatar;
}