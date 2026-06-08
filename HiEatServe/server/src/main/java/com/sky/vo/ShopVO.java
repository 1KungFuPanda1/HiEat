package com.sky.vo;

import com.sky.entity.Shop;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder // 替换原来的@Builder注解
@NoArgsConstructor
@AllArgsConstructor
public class ShopVO extends Shop { //继承了shop的所有字段
  // 店铺图片列表
  private List<String> shopImages;
  //店长姓名
  private String owner;
}