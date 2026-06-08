<template>
  <view class="shop-list">
    <!-- 排序标签区域 - 更紧凑的布局，靠左显示 -->
    <view class="sort-tags">
      <view class="sort-tag" :class="{ active: sortType === 'default' }" @click="changeSort('default')">
        综合排序
      </view>
      <view class="sort-tag" :class="{ active: sortType === 'sales' }" @click="changeSort('sales')">
        销量
      </view>
      <view class="sort-tag" :class="{ active: sortType === 'score' }" @click="changeSort('score')">
        评分
      </view>
    </view>

    <view class="shop-list-content">
      <view class="shop-item" v-for="(item, index) in shopList" :key="index" @click="goToShopDetail(item)">
        <view class="shop-logo">
          <image :src="item.image" mode="aspectFill"></image>
        </view>
        <view class="shop-info">
          <view class="shop-name">{{ item.shopName }}</view>
          <view class="shop-rating">
            <text class="rating-score">{{ item.score }}分</text>
            <text class="monthly-sales">月售{{ item.orderQuantity || 0 }}单</text>
          </view>
          <view class="shop-fee">
            <text class="min-fee">起送¥{{ (item.minFee / 100).toFixed(2) }}</text>
            <text class="delivery-fee">配送¥{{ (item.deliverFee / 100).toFixed(2) }}</text>
          </view>
          <view class="shop-delivery-time">
            <text class="delivery-time">{{ item.averageSendTime }}分钟</text>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="hasMore" @click="loadMore">
        <text>加载更多</text>
      </view>
      <view class="no-more" v-else>
        <text>没有更多了</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { shopApi } from '@/api/index';

// 店铺列表数据
const shopList = ref([]);
// 分页参数
const page = ref(1);
const pageSize = ref(10);
// 是否有更多数据
const hasMore = ref(true);
// 是否正在加载
const loading = ref(false);

// 排序类型：default-综合排序，sales-销量优先，score-评分优先
const sortType = ref('default');

// 切换排序方式
const changeSort = (type) => {
  if (sortType.value === type) return;

  sortType.value = type;
  page.value = 1;
  shopList.value = [];
  hasMore.value = true;
  getShopList();
};

// 获取店铺列表
const getShopList = async () => {
  if (loading.value) return;

  loading.value = true;
  try {
    // 构建请求参数
    const params = {
      page: page.value,
      pageSize: pageSize.value
    };

    let res;
    // 根据排序类型选择不同的接口
    switch (sortType.value) {
      case 'sales':
        // 销量优先使用 /user/shop/count 接口
        res = await shopApi.getShopListByCount(params);
        break;
      case 'score':
        // 评分优先使用 /user/shop/score 接口
        res = await shopApi.getShopListByScore(params);
        break;
      default:
        // 综合排序使用默认接口 /user/shop/page
        res = await shopApi.getShopList(params);
        break;
    }

    // 如果是第一页，直接赋值；否则追加数据
    if (page.value === 1) {
      shopList.value = res.data.records;
      // 打印店铺数据，查看是否包含 status 字段
      console.log('店铺列表数据:', res.data.records);
      if (res.data.records.length > 0) {
        console.log('第一个店铺的status:', res.data.records[0].status);
      }
    } else {
      shopList.value = [...shopList.value, ...res.data.records];
    }

    // 判断是否还有更多数据
    hasMore.value = shopList.value.length < res.data.total;

    // 页码加1，为下次加载做准备
    if (hasMore.value) {
      page.value++;
    }
  } catch (error) {
    console.error('获取店铺列表失败:', error);
    uni.showToast({
      title: '获取店铺列表失败',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
};

// 加载更多
const loadMore = () => {
  if (!hasMore.value || loading.value) return;
  getShopList();
};

// 在 methods 部分添加或修改以下方法
const goToShopDetail = (shop) => {
  // 跳转到店铺详情页，并传递店铺ID
  uni.navigateTo({
    url: `/pages/shop/detail?id=${shop.id}`
  });
};

// 刷新店铺列表
const refreshShopList = () => {
  page.value = 1;
  hasMore.value = true;
  getShopList();
};

// 暴露方法给父组件调用
defineExpose({
  refreshShopList
});

// 页面加载时获取店铺列表
onMounted(() => {
  getShopList();
});
</script>

<style lang="scss">
.shop-list {
  margin-top: 20rpx;

  .sort-tags {
    display: flex;
    justify-content: flex-start;
    /* 改为靠左对齐 */
    margin-bottom: 20rpx;
    background-color: #fff;
    padding: 15rpx 20rpx;
    /* 减小内边距使其更紧凑 */
    border-radius: 8rpx;
    box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);

    .sort-tag {
      font-size: 26rpx;
      /* 减小字体大小 */
      color: #666;
      position: relative;
      padding: 0 10rpx;
      margin-right: 30rpx;
      /* 添加右边距，使标签之间有间隔 */

      &.active {
        color: #2588FF;
        font-weight: 500;

        &::after {
          content: '';
          position: absolute;
          bottom: -8rpx;
          /* 调整下划线位置 */
          left: 50%;
          transform: translateX(-50%);
          width: 40rpx;
          height: 4rpx;
          background-color: #2588FF;
          border-radius: 2rpx;
        }
      }

      &:last-child {
        margin-right: 0;
        /* 最后一个标签不需要右边距 */
      }
    }
  }

  .shop-list-content {
    .shop-item {
      display: flex;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f5f5f5;

      .shop-logo {
        width: 120rpx;
        height: 120rpx;
        margin-right: 20rpx;

        image {
          width: 100%;
          height: 100%;
          border-radius: 8rpx;
        }
      }

      .shop-info {
        flex: 1;
        position: relative;

        .shop-name {
          font-size: 30rpx;
          font-weight: 500;
          margin-bottom: 10rpx;
        }

        .shop-rating {
          font-size: 24rpx;
          color: #666;
          margin-bottom: 10rpx;

          .rating-score {
            color: #ff9500;
            margin-right: 20rpx;
          }
        }

        .shop-fee {
          font-size: 24rpx;
          color: #999;
          margin-bottom: 10rpx;

          .min-fee {
            margin-right: 20rpx;
          }

          .delivery-fee {
            margin-right: 20rpx;
          }
        }

        .shop-delivery-time {
          font-size: 24rpx;
          color: #999;
          position: absolute;
          right: 0;
          bottom: 0;

          .delivery-time {
            color: #2588FF;
          }
        }
      }
    }

    .load-more,
    .no-more {
      text-align: center;
      padding: 20rpx 0;
      font-size: 26rpx;
      color: #999;
    }

    .load-more {
      color: #2588FF;
    }
  }
}
</style>