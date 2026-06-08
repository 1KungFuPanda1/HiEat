<template>
  <view class="orders-container">
    <!-- 顶部标签栏 全部 / 待支付 / 派送中 / 已完成-->
    <view class="tabs">
      <view v-for="(tab, index) in tabs" :key="index" class="tab-item" :class="{ active: currentTab === index }"
        @click="switchTab(index)">
        {{ tab.name }}
      </view>
    </view>

    <!-- 订单列表容器 order-list(滚动区域) -->
    <scroll-view scroll-y class="order-list" @scrolltolower="loadMore" refresher-enabled
      :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
      <template v-if="orderList.length > 0">
        <!--单个订单卡片 order-item-->
		<view class="order-item" v-for="order in orderList" :key="order.id" @click="goToOrderDetail(order.id)">
          <!-- 订单头部 order-header （店铺名 + 订单状态） -->
          <view class="order-header">
            <view class="shop-info" @click.stop="goToShop(order.shopId)">
              <image class="shop-avatar" :src="order.shopImage || '/static/image/shop/default-shop.png'"
                mode="aspectFill"></image>
              <text class="shop-name">{{ order.shopName || '加载中...' }}</text>
            </view>
            <view class="order-status">
              <text :class="getStatusClass(order.status)">{{ getStatusText(order.status) }}</text>
            </view>
          </view>
          <!-- 订单商品 order-goods（菜品图片 + 名称 + 价格 + 数量）-->
          <view class="order-goods">
            <!-- 有详情时显示商品列表 -->
            <template v-if="order.orderDetailList && order.orderDetailList.length > 0">
              <view class="goods-item" v-for="(good, gIndex) in order.orderDetailList.slice(0, 3)" :key="gIndex">
                <image class="goods-image" :src="good.image" mode="aspectFill"></image>
                <view class="goods-info">
                  <text class="goods-name">{{ good.name }}</text>
                  <view class="goods-price">
                    <text class="price">¥{{ good.amount }}</text>
                    <text class="count">x{{ good.number }}</text>
                  </view>
                </view>
              </view>
              <!-- 显示更多商品提示 -->
              <view class="more-goods" v-if="order.orderDetailList.length > 3">
                <text>查看全部{{ order.orderDetailList.length }}个商品</text>
              </view>
            </template>
            <!-- 无详情时显示地址 -->
            <template v-else>
              <view class="order-address">
                <text>配送至: {{ order.address }}</text>
                <text>联系人: {{ order.consignee }} {{ order.phone }}</text>
              </view>
            </template>
          </view>

          <!-- 订单底部 order-footer（时间 + 实付金额） -->
          <view class="order-footer">
            <view class="order-time">{{ order.orderTime }}</view>
            <view class="order-amount">
              <text>实付:</text>
              <text class="amount">¥{{ order.amount }}</text>
            </view>
          </view>

          <!-- 订单操作区 order-actions（去评价 / 再来一单） -->
          <view class="order-actions">
            <!-- 待支付状态 -->
            <template v-if="order.status === 1">
            <!--  <view class="action-btn cancel" @click.stop="handleCancelOrder(order.id)">取消订单</view> -->
              <view class="action-btn pay" @click.stop="goToPay(order)">去支付</view>
            </template>

            <!-- 已完成状态 -->
            <template v-if="order.status === 5">
              <view class="action-btn review" @click.stop="goToReview(order)" v-if="!order.reviewed">去评价</view>
              <view class="action-btn repeat" @click.stop="handleRepeatOrder(order.id)">再来一单</view>
            </template>

            <!-- 已接单状态或配送中状态可以催单 -->
            <template v-if="order.status === 3 || order.status === 4">
              <view class="action-btn reminder" @click.stop="handleReminderOrder(order.id)">催单</view>
            </template>
          </view>
        </view>
      </template>

      <!-- 空状态 -->
      <view class="empty-state" v-if="orderList.length === 0 && !loading">
        <image src="/static/image/orders/empty-orders.png" mode="aspectFit" class="empty-image"></image>
        <text class="empty-text">暂无相关订单</text>
      </view>

      <!-- 加载状态 -->
      <view class="loading-state" v-if="loading && !refreshing">
        <text>加载中...</text>
      </view>

      <!-- 加载完成提示 -->
      <view class="load-all" v-if="!hasMore && orderList.length > 0 && !loading">
        <text>已加载全部订单</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { orderApi } from '@/api/order'
import { shopApi } from '@/api/shop'

export default {
  data() {
    return {
      // 标签页配置
      tabs: [
        { name: '全部', status: null },
        { name: '待支付', status: 1 },
        { name: '派送中', status: 4 },
        { name: '已完成', status: 5 }
      ],
      currentTab: 0, // 当前选中的标签页索引
      orderList: [], // 订单列表
      page: 1, // 当前页码
      pageSize: 10, // 每页数量
      total: 0, // 总记录数
      loading: false, // 加载状态
      hasMore: true, // 是否有更多数据
      refreshing: false, // 刷新状态
      shopCache: {} // 店铺信息缓存
    }
  },
  onShow() {
    // 每次显示页面时刷新数据
    this.refreshData()
  },
  onLoad() {
    // 从本地存储加载店铺缓存
    this.loadShopCache()
  },
  methods: {
    // 加载店铺缓存
    loadShopCache() {
      try {
        const shopCacheStr = uni.getStorageSync('shopCache')
        if (shopCacheStr) {
          this.shopCache = JSON.parse(shopCacheStr)
        }
      } catch (error) {
        console.error('加载店铺缓存失败:', error)
      }
    },

    // 保存店铺缓存
    saveShopCache() {
      try {
        uni.setStorageSync('shopCache', JSON.stringify(this.shopCache))
      } catch (error) {
        console.error('保存店铺缓存失败:', error)
      }
    },

    // 切换标签页
	/* 点「全部 / 待支付 / 派送中 / 已完成」
	切换标签
	重新加载对应状态的订单 */
    switchTab(index) {
      if (this.currentTab === index) return
      this.currentTab = index
      this.refreshData()
    },

    // 刷新数据
    refreshData() {
      this.page = 1
      this.hasMore = true
      this.orderList = []
      this.loadOrders()
    },

    // 下拉刷新
    onRefresh() {
      this.refreshing = true
      this.refreshData()
      setTimeout(() => {
        this.refreshing = false
      }, 1000)
    },

    // 加载更多
    loadMore() {
      if (this.loading || !this.hasMore) return
      this.page++
      this.loadOrders()
    },

    // 加载订单数据
    async loadOrders() {
      if (this.loading) return

		  // 1. 向后端请求订单
		  // 2. 如果是特定标签，带上 status 参数
		  // 3. 把订单放进 orderList
		  // 4. 加载店铺信息（头像、名称）

      this.loading = true

      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize
        }

        // 如果不是"全部"标签，添加状态过滤
        if (this.tabs[this.currentTab].status !== null) {
          params.status = this.tabs[this.currentTab].status
        }

        const res = await orderApi.getHistoryOrders(params)

        if (res.code === 1 && res.data) {
          // 第一页直接赋值，其他页追加
          const orders = res.data.records || []

          if (this.page === 1) {
            this.orderList = orders
          } else {
            this.orderList = this.orderList.concat(orders)
          }

          this.total = res.data.total || 0

          // 判断是否还有更多数据
          this.hasMore = this.orderList.length < this.total

          // 应用已缓存的店铺信息
          this.applyShopCache()

          // 获取订单中的店铺信息
          this.loadShopInfo(orders)
        } else {
          uni.showToast({
            title: res.msg || '获取订单列表失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('获取订单列表失败:', error)
        uni.showToast({
          title: '网络异常，请重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },

    // 获取订单关联的店铺信息
    async loadShopInfo(orders) {
      // 提取所有需要获取信息的店铺ID
      const shopIds = orders
        .filter(order => order.shopId && !this.shopCache[order.shopId])
        .map(order => order.shopId)

      // 去重
      const uniqueShopIds = [...new Set(shopIds)]

      if (uniqueShopIds.length === 0) {
        console.log('没有需要获取的店铺信息')
        return
      }

      console.log('需要获取的店铺ID:', uniqueShopIds)

      uni.showLoading({
        title: '加载店铺信息...',
        mask: false
      })

      // 批量获取店铺信息
      for (const shopId of uniqueShopIds) {
        try {
          console.log('开始获取店铺信息, shopId:', shopId)
          const shopRes = await shopApi.getShopDetail(shopId)
          console.log('获取到店铺信息:', shopRes)

          if (shopRes.code === 1 && shopRes.data) {
            const shopData = shopRes.data
            console.log('店铺数据:', shopData)

            // 缓存店铺信息
            this.shopCache[shopId] = {
              name: shopData.shopName || '未知店铺', // 使用shopName字段
              image: shopData.image || '',
              address: shopData.address || '',
              provinceName: shopData.provinceName || '',
              cityName: shopData.cityName || '',
              districtName: shopData.districtName || ''
            }

            // 更新订单列表中的店铺信息
            this.orderList = this.orderList.map(order => {
              if (order.shopId === shopId) {
                return {
                  ...order,
                  shopName: shopData.shopName || '未知店铺', // 使用shopName字段
                  shopImage: shopData.image
                }
              }
              return order
            })
          } else {
            console.error('获取店铺信息返回错误:', shopRes)
          }
        } catch (error) {
          console.error(`获取店铺(${shopId})信息失败:`, error)
        }
      }

      // 保存更新后的店铺缓存
      this.saveShopCache()

      uni.hideLoading()
    },

    // 应用缓存的店铺信息到当前订单列表
    applyShopCache() {
      if (Object.keys(this.shopCache).length === 0) {
        console.log('店铺缓存为空')
        return
      }

      console.log('应用店铺缓存:', this.shopCache)

      this.orderList = this.orderList.map(order => {
        if (order.shopId && this.shopCache[order.shopId]) {
          const shopInfo = this.shopCache[order.shopId]
          console.log(`应用店铺(${order.shopId})缓存:`, shopInfo)
          return {
            ...order,
            shopName: shopInfo.name || '未知店铺',
            shopImage: shopInfo.image || '/static/image/shop/default-shop.png'
          }
        }
        return order
      })
    },

    // 获取订单状态文本
    getStatusText(status) {
      switch (status) {
        case 1: return '待支付'
        case 2: return '待接单'
        case 3: return '已接单'
        case 4: return '派送中'
        case 5: return '已完成'
        case 6: return '已取消'
        case 7: return '退款'
        default: return '未知状态'
      }
    },

    // 获取订单状态样式类
    getStatusClass(status) {
      switch (status) {
        case 1: return 'status-unpaid'
        case 2: return 'status-processing'
        case 3: return 'status-processing'
        case 4: return 'status-delivering'
        case 5: return 'status-completed'
        case 6: return 'status-canceled'
        case 7: return 'status-refund'
        default: return ''
      }
    },

    // 前往订单详情
    goToOrderDetail(orderId) {
      uni.navigateTo({
        url: `/pages/orders/detail?orderId=${orderId}`
      })
    },

    // 前往支付页面
    goToPay(order) {
      uni.navigateTo({
        url: `/pages/order/payment?orderNumber=${order.number}&amount=${order.amount}`
      })
    },

    // 前往店铺页面
    goToShop(shopId) {
      if (!shopId) return

      uni.navigateTo({
        url: `/pages/shop/detail?id=${shopId}`
      })
    },

    // 处理取消订单
    async handleCancelOrder(orderId) {
      try {
        uni.showModal({
          title: '提示',
          content: '确定要取消该订单吗？',
          success: async (res) => {
            if (res.confirm) {
              uni.showLoading({
                title: '取消中...'
              })

              const result = await orderApi.cancelOrder(orderId)

              uni.hideLoading()

              if (result.code === 1) {
                uni.showToast({
                  title: '订单已取消',
                  icon: 'success'
                })

                // 刷新订单列表
                this.refreshData()
              } else {
                uni.showToast({
                  title: result.msg || '取消订单失败',
                  icon: 'none'
                })
              }
            }
          }
        })
      } catch (error) {
        uni.hideLoading()
        console.error('取消订单失败:', error)
        uni.showToast({
          title: '网络异常，请重试',
          icon: 'none'
        })
      }
    },

    // 处理再来一单
    async handleRepeatOrder(orderId) {
      try {
        uni.showLoading({
          title: '处理中...'
        })

        const result = await orderApi.repeatOrder(orderId)

        uni.hideLoading()

        if (result.code === 1) {
          // 获取订单详情以获取店铺ID
          const orderDetail = await orderApi.getOrderDetail(orderId)

          if (orderDetail.code === 1 && orderDetail.data) {
            // 跳转到提交订单页面
            uni.navigateTo({
              url: `/pages/order/submit?shopId=${orderDetail.data.shopId}`
            })
          } else {
            uni.showToast({
              title: '获取订单信息失败',
              icon: 'none'
            })
          }
        } else {
          uni.showToast({
            title: result.msg || '操作失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('再来一单失败:', error)
        uni.showToast({
          title: '网络异常，请重试',
          icon: 'none'
        })
      }
    },

    // 处理催单
    async handleReminderOrder(orderId) {
      try {
        uni.showLoading({
          title: '处理中...'
        })

        const result = await orderApi.reminderOrder(orderId)

        uni.hideLoading()

        if (result.code === 1) {
          uni.showToast({
            title: '已催单，请耐心等待',
            icon: 'success'
          })
        } else {
          uni.showToast({
            title: result.msg || '催单失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('催单失败:', error)
        uni.showToast({
          title: '网络异常，请重试',
          icon: 'none'
        })
      }
    },

    // 前往评价页面
    goToReview(order) {
      uni.navigateTo({
        url: `/pages/orders/review?orderId=${order.id}`
      })
    }
  }
}
</script>

<style lang="scss">
.orders-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;

  // 标签栏
  .tabs {
    display: flex;
    background-color: #fff;
    padding: 0 20rpx;
    height: 90rpx;
    position: sticky;
    top: 0;
    z-index: 10;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

    .tab-item {
      flex: 1;
      display: flex;
      justify-content: center;
      align-items: center;
      font-size: 28rpx;
      color: #666;
      position: relative;

      &.active {
        color: #2588FF;
        font-weight: 500;

        &:after {
          content: '';
          position: absolute;
          bottom: 0;
          left: 50%;
          transform: translateX(-50%);
          width: 40rpx;
          height: 4rpx;
          background-color: #2588FF;
          border-radius: 2rpx;
        }
      }
    }
  }

  // 订单列表
  .order-list {
    flex: 1;
    padding: 20rpx;
    height: calc(100vh - 90rpx);
    box-sizing: border-box;

    .order-item {
      background-color: #fff;
      border-radius: 12rpx;
      margin-bottom: 20rpx;
      padding: 20rpx;
      box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

      .order-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-bottom: 20rpx;
        border-bottom: 1px solid #f5f5f5;

        .shop-info {
          display: flex;
          align-items: center;

          .shop-avatar {
            width: 60rpx;
            height: 60rpx;
            border-radius: 50%;
            margin-right: 16rpx;
          }

          .shop-name {
            font-size: 28rpx;
            color: #333;
            font-weight: 500;
          }
        }

        .order-status {
          font-size: 26rpx;

          .status-unpaid {
            color: #ff5722;
          }

          .status-processing {
            color: #2588FF;
          }

          .status-delivering {
            color: #ff9800;
          }

          .status-completed {
            color: #4caf50;
          }

          .status-canceled {
            color: #999;
          }

          .status-refund {
            color: #9c27b0;
          }
        }
      }

      .order-goods {
        padding: 20rpx 0;

        .goods-item {
          display: flex;
          margin-bottom: 20rpx;

          &:last-child {
            margin-bottom: 10rpx;
          }

          .goods-image {
            width: 120rpx;
            height: 120rpx;
            border-radius: 8rpx;
            margin-right: 20rpx;
          }

          .goods-info {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: space-between;

            .goods-name {
              font-size: 28rpx;
              color: #333;
              margin-bottom: 10rpx;
            }

            .goods-price {
              display: flex;
              justify-content: space-between;

              .price {
                font-size: 26rpx;
                color: #ff5722;
              }

              .count {
                font-size: 26rpx;
                color: #999;
              }
            }
          }
        }

        .more-goods {
          text-align: center;
          font-size: 24rpx;
          color: #999;
          padding: 10rpx 0;
        }

        .order-address {
          font-size: 26rpx;
          color: #666;
          line-height: 1.6;
          display: flex;
          flex-direction: column;
        }
      }

      .order-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: 20rpx;
        border-top: 1px solid #f5f5f5;

        .order-time {
          font-size: 24rpx;
          color: #999;
        }

        .order-amount {
          font-size: 26rpx;
          color: #666;

          .amount {
            font-size: 30rpx;
            color: #ff5722;
            font-weight: 500;
            margin-left: 10rpx;
          }
        }
      }

      .order-actions {
        display: flex;
        justify-content: flex-end;
        margin-top: 20rpx;

        .action-btn {
          padding: 8rpx 20rpx;
          font-size: 26rpx;
          border-radius: 30rpx;
          margin-left: 20rpx;

          &.cancel {
            color: #666;
            border: 1px solid #ddd;
          }

          &.pay {
            color: #fff;
            background-color: #ff5722;
          }

          &.repeat {
            color: #fff;
            background-color: #2588FF;
          }

          &.reminder {
            color: #fff;
            background-color: #ff9800;
          }

          &.review {
            color: #fff;
            background-color: #4caf50;
          }
        }
      }
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding-top: 200rpx;

      .empty-image {
        width: 200rpx;
        height: 200rpx;
        margin-bottom: 30rpx;
      }

      .empty-text {
        font-size: 28rpx;
        color: #999;
      }
    }

    .loading-state,
    .load-all {
      text-align: center;
      padding: 30rpx 0;
      font-size: 24rpx;
      color: #999;
    }
  }
}
</style>