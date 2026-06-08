package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long id;
  private Long userId;
  private Long shopId;
  private Integer rating;
  private String title;
  private String content;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  private String status;

  // 非数据库字段
  private String userName;
  private String userAvatar;
  private List<ReviewPhoto> photos;
  private List<ReviewReply> replies;
}