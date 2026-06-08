package com.sky.vo;

import com.sky.entity.Shop;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ShopVO extends Shop {
  // 店铺照片列表
  private List<String> shopImages;

  private String owner;
}