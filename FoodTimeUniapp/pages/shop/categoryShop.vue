<template>
  <view class="category-shop-container">
    <!-- 顶部筛选栏排序 -->
    <view class="filter-section">
      <view class="filter-tab" :class="{ active: currentTab === 'default' }" @click="changeTab('default')">综合排序</view>
      <view class="filter-tab" :class="{ active: currentTab === 'sales' }" @click="changeTab('sales')">销量优先</view>
      <view class="filter-tab" :class="{ active: currentTab === 'score' }" @click="changeTab('score')">评分优先</view>
    </view>
      <!-- 2. 店铺列表滚动区域 -->
    <scroll-view class="shop-list" scroll-y @scrolltolower="loadMore" refresher-enabled @refresherrefresh="refreshData"
      :refresher-triggered="isRefreshing">
      <view v-if="loading && shopList.length === 0" class="loading-box">
        <uni-load-more status="loading" :contentText="loadMoreText"></uni-load-more>
      </view>
      <view v-else-if="shopList.length === 0" class="empty-box">
        <image src="/static/image/common/empty.png" mode="aspectFit"></image>
        <text>暂无店铺</text>
      </view>
      <view v-else class="shop-items">
          <!-- 循环展示店铺 -->
        <view class="shop-item" v-for="(item, index) in shopList" :key="index" @click="goToShopDetail(item)">
          <view class="shop-img">
            <image :src="item.image" mode="aspectFill"></image>
          </view>
          <view class="shop-info">
            <view class="shop-name">{{ item.shopName }}</view>
            <view class="shop-desc">
              <text class="score">{{ item.score.toFixed(1) }}分</text>
              <text class="sale">月售{{ item.orderQuantity || 0 }}</text>
            </view>
            <view class="shop-address">{{ item.address }}</view>
            <view class="shop-extra">
              <text class="fee">起送¥{{ (item.minFee / 100).toFixed(2) }}</text>
              <text class="delivery">配送¥{{ (item.deliverFee / 100).toFixed(2) }}</text>
              <text class="time">{{ item.averageSendTime }}分钟</text>
            </view>
          </view>
        </view>
      </view>
      <uni-load-more v-if="shopList.length > 0" :status="loadMoreStatus" :contentText="loadMoreText"></uni-load-more>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { shopApi } from '@/api/index';

// 页面参数
const categoryId = ref(null);
const categoryName = ref('分类店铺');

// 店铺列表数据
const shopList = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const hasMore = ref(true);
const loading = ref(false);
const currentTab = ref('default'); // default, sales, score
const isRefreshing = ref(false);

// 加载状态文本
const loadMoreText = {
  contentdown: '上拉显示更多',
  contentrefresh: '正在加载...',
  contentnomore: '没有更多数据了'
};

// 计算加载状态
const loadMoreStatus = computed(() => {
  if (loading.value) return 'loading';
  if (!hasMore.value) return 'noMore';
  return 'more';
});

/**
 * 获取店铺列表
 * @param {boolean} [refresh=false] - 是否刷新数据
 */
const getShopList = async (refresh = false) => {
  if (refresh) {
    currentPage.value = 1;
    hasMore.value = true;
    shopList.value = [];
  }

  if (!hasMore.value || !categoryId.value) return;

  loading.value = true;

  try {
    // 准备请求参数
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      categoryId: categoryId.value
    };

    // 根据当前选择的排序规则获取数据
    if (currentTab.value === 'sales') {
      params.rule = 'sales';
    } else if (currentTab.value === 'score') {
      params.rule = 'score';
    } else {
      params.rule = 'default';
    }

    const res = await shopApi.getShopsByCategory(params);

    if (res && res.code === 1 && res.data) {
      const { records, total } = res.data;

      if (records && records.length > 0) {
        shopList.value = [...shopList.value, ...records];
        currentPage.value++;
        hasMore.value = shopList.value.length < total;
      } else {
        hasMore.value = false;
      }
    } else {
      console.error('获取店铺列表失败:', res);
      uni.showToast({
        title: '获取店铺列表失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('获取店铺列表异常:', error);
    uni.showToast({
      title: '网络异常，请稍后重试',
      icon: 'none'
    });
  } finally {
    loading.value = false;
    if (isRefreshing.value) {
      isRefreshing.value = false;
      uni.stopPullDownRefresh();
    }
  }
};

/**
 * 切换排序选项卡
 * @param {string} tab - 选项卡标识
 */
const changeTab = (tab) => {
  if (currentTab.value === tab) return;
  currentTab.value = tab;
  getShopList(true);
};

/**
 * 加载更多数据
 */
const loadMore = () => {
  if (!loading.value && hasMore.value) {
    getShopList();
  }
};

/**
 * 刷新数据
 */
const refreshData = () => {
  isRefreshing.value = true;
  getShopList(true);
};

/**
 * 跳转到店铺详情
 * @param {Object} shop - 店铺信息
 */
const goToShopDetail = (shop) => {
  uni.navigateTo({
    url: `/pages/shop/detail?id=${shop.id}`
  });
};

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack();
};

// 页面加载
onMounted(() => {
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  const options = currentPage.options || {};

  if (options.categoryId) {
    categoryId.value = options.categoryId;
  }

  if (options.categoryName) {
    categoryName.value = decodeURIComponent(options.categoryName);
  }

  // 获取数据
  getShopList(true);
});
</script>

<style lang="scss">
.category-shop-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f5;

  .filter-section {
    display: flex;
    height: 80rpx;
    background-color: #fff;
    margin-top: 2rpx;

    .filter-tab {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28rpx;
      color: #666;
      position: relative;

      &.active {
        color: #2588FF;
        font-weight: 500;

        &::after {
          content: '';
          position: absolute;
          bottom: 0;
          left: 50%;
          transform: translateX(-50%);
          width: 60rpx;
          height: 4rpx;
          background-color: #2588FF;
          border-radius: 2rpx;
        }
      }
    }
  }

  .shop-list {
    flex: 1;
    padding: 20rpx;

    .loading-box,
    .empty-box {
      height: 300rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;

      image {
        width: 200rpx;
        height: 200rpx;
        margin-bottom: 20rpx;
      }

      text {
        color: #999;
        font-size: 28rpx;
      }
    }

    .shop-items {
      .shop-item {
        display: flex;
        padding: 30rpx;
        margin-bottom: 20rpx;
        background-color: #fff;
        border-radius: 12rpx;

        .shop-img {
          width: 160rpx;
          height: 160rpx;
          margin-right: 24rpx;
          border-radius: 8rpx;
          overflow: hidden;

          image {
            width: 100%;
            height: 100%;
          }
        }

        .shop-info {
          flex: 1;
          display: flex;
          flex-direction: column;

          .shop-name {
            font-size: 32rpx;
            font-weight: 500;
            color: #333;
            margin-bottom: 10rpx;
          }

          .shop-desc {
            display: flex;
            align-items: center;
            margin-bottom: 10rpx;

            .score {
              font-size: 26rpx;
              color: #FF9500;
              margin-right: 20rpx;
            }

            .sale {
              font-size: 24rpx;
              color: #999;
            }
          }

          .shop-address {
            font-size: 24rpx;
            color: #666;
            margin-bottom: 10rpx;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .shop-extra {
            display: flex;
            align-items: center;

            text {
              font-size: 22rpx;
              color: #999;
              margin-right: 20rpx;
            }

            .fee {
              color: #FF6A00;
            }
          }
        }
      }
    }
  }
}
</style>