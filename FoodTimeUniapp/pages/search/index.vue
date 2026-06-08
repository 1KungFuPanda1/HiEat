<template>
  <view class="search-container">
    <!-- 搜索头部 -->
    <view class="search-header">
      <view class="search-input-box">
        <input class="search-input" type="text" v-model="keyword" placeholder="搜索店铺" confirm-type="search"
          @confirm="handleSearch" focus />
        <view class="clear-icon" v-if="keyword" @click="clearKeyword">
          <uni-icons type="clear" size="14" color="#999"></uni-icons>
        </view>
      </view>
      <view class="search-btn" @click="goBack">取消</view>
    </view>

    <!-- 搜索内容区域 -->
    <view class="search-content">
      <!-- 搜索历史 -->
      <view class="search-history" v-if="!showResult">
        <view class="history-header">
          <text class="history-title">搜索历史</text>
          <view class="clear-history" @click="clearHistory">
            <uni-icons type="trash" size="16" color="#999"></uni-icons>
          </view>
        </view>
        <view class="history-list">
          <view class="history-item" v-for="(item, index) in searchHistory" :key="index"
            @click="useHistoryKeyword(item)">
            {{ item }}
          </view>
          <view class="empty-history" v-if="searchHistory.length === 0">暂无搜索记录</view>
        </view>
      </view>

      <!-- 搜索结果 -->
      <view class="search-result" v-if="showResult">
        <!-- 筛选条件 -->
        <view class="filter-section">
          <view class="filter-tab" :class="{ active: currentTab === 'default' }" @click="changeTab('default')">综合排序
          </view>
          <view class="filter-tab" :class="{ active: currentTab === 'sales' }" @click="changeTab('sales')">销量优先</view>
          <view class="filter-tab" :class="{ active: currentTab === 'score' }" @click="changeTab('score')">评分优先</view>
        </view>

        <!-- 结果列表 -->
        <scroll-view class="result-list" scroll-y @scrolltolower="loadMore" refresher-enabled
          @refresherrefresh="refreshData" :refresher-triggered="isRefreshing">
          <view v-if="loading && resultList.length === 0" class="loading-box">
            <uni-load-more status="loading" :contentText="loadMoreText"></uni-load-more>
          </view>
          <view v-else-if="resultList.length === 0" class="empty-box">
            <image src="/static/image/common/empty.png" mode="aspectFit"></image>
            <text>未找到相关店铺</text>
          </view>
          <view v-else class="shop-items">
            <view class="shop-item" v-for="(item, index) in resultList" :key="index" @click="goToShopDetail(item)">
              <view class="shop-img">
                <image :src="item.image" mode="aspectFill"></image>
              </view>
              <view class="shop-info">
                <view class="shop-name">{{ item.shopName }}</view>
                <view class="shop-desc">
                  <text class="score">{{ item.score }}分</text>
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
          <uni-load-more v-if="resultList.length > 0" :status="loadMoreStatus"
            :contentText="loadMoreText"></uni-load-more>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { shopApi } from '@/api';

// 搜索关键词
const keyword = ref('');
// 是否显示搜索结果
const showResult = ref(false);
// 搜索历史记录
const searchHistory = ref([]);
// 搜索结果列表
const resultList = ref([]);
// 当前页码
const currentPage = ref(1);
// 每页数量
const pageSize = ref(10);
// 是否有更多数据
const hasMore = ref(true);
// 加载状态
const loading = ref(false);
// 当前排序选项卡
const currentTab = ref('default'); // default, sales, score
// 是否正在刷新
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
 * 清空搜索关键词
 */
const clearKeyword = () => {
  keyword.value = '';
  showResult.value = false;
};

/**
 * 搜索操作
 */
const handleSearch = () => {
  if (!keyword.value.trim()) {
    uni.showToast({
      title: '请输入搜索关键词',
      icon: 'none'
    });
    return;
  }

  // 添加到搜索历史
  addToHistory(keyword.value.trim());

  // 显示搜索结果
  showResult.value = true;

  // 搜索店铺
  searchShops(true);
};

/**
 * 添加搜索关键词到历史记录
 * @param {string} kw - 搜索关键词
 */
const addToHistory = (kw) => {
  // 如果已存在相同的关键词，先删除
  const index = searchHistory.value.findIndex(item => item === kw);
  if (index !== -1) {
    searchHistory.value.splice(index, 1);
  }

  // 添加到数组开头
  searchHistory.value.unshift(kw);

  // 最多保留10条记录
  if (searchHistory.value.length > 10) {
    searchHistory.value = searchHistory.value.slice(0, 10);
  }

  // 保存到本地存储
  saveHistoryToStorage();
};

/**
 * 保存搜索历史到本地存储
 */
const saveHistoryToStorage = () => {
  uni.setStorageSync('searchHistory', JSON.stringify(searchHistory.value));
};

/**
 * 加载本地存储的搜索历史
 */
const loadHistoryFromStorage = () => {
  const history = uni.getStorageSync('searchHistory');
  if (history) {
    try {
      searchHistory.value = JSON.parse(history);
    } catch (e) {
      console.error('解析搜索历史失败:', e);
      searchHistory.value = [];
    }
  }
};

/**
 * 清空搜索历史
 */
const clearHistory = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清空搜索历史吗？',
    success: (res) => {
      if (res.confirm) {
        searchHistory.value = [];
        uni.removeStorageSync('searchHistory');
      }
    }
  });
};

/**
 * 使用历史关键词进行搜索
 * @param {string} kw - 搜索关键词
 */
const useHistoryKeyword = (kw) => {
  keyword.value = kw;
  handleSearch();
};

/**
 * 搜索店铺
 * @param {boolean} [refresh=false] - 是否刷新数据
 */
const searchShops = async (refresh = false) => {
  if (refresh) {
    currentPage.value = 1;
    hasMore.value = true;
    resultList.value = [];
  }

  if (!hasMore.value || !keyword.value.trim()) return;

  loading.value = true;

  try {
    // 准备请求参数
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: keyword.value.trim(),
      rule: currentTab.value
    };

    const res = await shopApi.searchShops(params);

    if (res && res.code === 1 && res.data) {
      const { records, total } = res.data;

      if (records && records.length > 0) {
        resultList.value = [...resultList.value, ...records];
        currentPage.value++;
        hasMore.value = resultList.value.length < total;
      } else {
        hasMore.value = false;
      }
    } else {
      console.error('搜索店铺失败:', res);
      uni.showToast({
        title: '搜索失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('搜索店铺异常:', error);
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
  searchShops(true);
};

/**
 * 加载更多数据
 */
const loadMore = () => {
  if (!loading.value && hasMore.value) {
    searchShops();
  }
};

/**
 * 刷新数据
 */
const refreshData = () => {
  isRefreshing.value = true;
  searchShops(true);
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

// 页面加载时获取搜索历史
onMounted(() => {
  loadHistoryFromStorage();
});
</script>

<style lang="scss">
.search-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f5;

  .search-header {
    display: flex;
    align-items: center;
    padding: 20rpx 30rpx;
    background-color: #fff;
    border-bottom: 1rpx solid #f0f0f0;

    .search-input-box {
      flex: 1;
      height: 70rpx;
      background-color: #f5f5f5;
      border-radius: 35rpx;
      display: flex;
      align-items: center;
      padding: 0 30rpx;
      position: relative;

      .search-input {
        flex: 1;
        height: 100%;
        font-size: 28rpx;
      }

      .clear-icon {
        width: 40rpx;
        height: 40rpx;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }

    .search-btn {
      padding: 0 20rpx;
      font-size: 30rpx;
      color: #333;
    }
  }

  .search-content {
    flex: 1;
    overflow: hidden;

    .search-history {
      padding: 30rpx;
      background-color: #fff;

      .history-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20rpx;

        .history-title {
          font-size: 30rpx;
          font-weight: 500;
          color: #333;
        }

        .clear-history {
          padding: 10rpx;
        }
      }

      .history-list {
        display: flex;
        flex-wrap: wrap;

        .history-item {
          padding: 10rpx 30rpx;
          background-color: #f5f5f5;
          border-radius: 30rpx;
          margin-right: 20rpx;
          margin-bottom: 20rpx;
          font-size: 26rpx;
          color: #666;
        }

        .empty-history {
          width: 100%;
          height: 100rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #999;
          font-size: 28rpx;
        }
      }
    }

    .search-result {
      flex: 1;
      display: flex;
      flex-direction: column;

      .filter-section {
        display: flex;
        height: 80rpx;
        background-color: #fff;

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

      .result-list {
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
  }
}
</style>