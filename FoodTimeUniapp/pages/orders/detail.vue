<template>
  <view class="order-detail">
    <!-- 订单状态 -->
    <view class="status-section">
      <view class="status-text">{{ getStatusText(orderDetail.status) }}</view>
      <view class="status-desc" v-if="orderDetail.status === 4">
        正在配送中，请耐心等待
      </view>
      <view class="status-desc" v-else-if="orderDetail.status === 1">
        请尽快完成支付
      </view>
      <view class="status-desc" v-else-if="orderDetail.status === 2">
        商家正在接单中
      </view>
      <view class="status-desc" v-else-if="orderDetail.status === 3">
        商家已接单，正在准备中
      </view>
      <view class="status-desc" v-else-if="orderDetail.status === 5">
        订单已完成，欢迎再次光临
      </view>
      <view class="status-desc" v-else-if="orderDetail.status === 6">
        订单已取消
      </view>
    </view>

    <!-- 店铺信息 -->
    <view class="shop-section" v-if="shopInfo.id" @click="goToShop">
      <image class="shop-avatar" :src="shopInfo.image || '/static/image/shop/default-shop.png'" mode="aspectFill">
      </image>
      <view class="shop-info">
        <text class="shop-name">{{ shopInfo.shopName || shopInfo.name || '加载中...' }}</text>
        <text class="shop-desc">{{ getFullAddress(shopInfo) }}</text>
      </view>
      <text class="arrow">></text>
    </view>

    <!-- 配送信息 -->
    <view class="delivery-section" v-if="orderDetail.id">
      <view class="section-title">配送信息</view>
      <view class="delivery-info">
        <view class="info-item">
          <text class="label">收货人:</text>
          <text class="value">{{ orderDetail.consignee }} {{ orderDetail.phone }}</text>
        </view>
        <view class="info-item">
          <text class="label">收货地址:</text>
          <text class="value">{{ orderDetail.address }}</text>
        </view>
        <view class="info-item">
          <text class="label">配送时间:</text>
          <text class="value">{{ orderDetail.estimatedDeliveryTime }}</text>
        </view>
      </view>
    </view>

    <!-- 订单信息 -->
    <view class="order-info-section" v-if="orderDetail.id">
      <view class="section-title">订单信息</view>
      <view class="order-info">
        <view class="info-item">
          <text class="label">订单编号:</text>
          <text class="value">{{ orderDetail.number }}</text>
          <text class="copy-btn" @click="copyOrderNumber">复制</text>
        </view>
        <view class="info-item">
          <text class="label">下单时间:</text>
          <text class="value">{{ orderDetail.orderTime }}</text>
        </view>
        <view class="info-item">
          <text class="label">支付方式:</text>
          <text class="value">{{ getPayMethodText(orderDetail.payMethod) }}</text>
        </view>
        <view class="info-item">
          <text class="label">支付状态:</text>
          <text class="value">{{ getPayStatusText(orderDetail.payStatus) }}</text>
        </view>
        <view class="info-item">
          <text class="label">支付时间:</text>
          <text class="value">{{ orderDetail.checkoutTime }}</text>
        </view>
        <view class="info-item" v-if="orderDetail.remark">
          <text class="label">备注信息:</text>
          <text class="value">{{ orderDetail.remark }}</text>
        </view>
      </view>
    </view>

    <!-- 商品信息 -->
    <view class="goods-section" v-if="orderDetail.orderDetailList && orderDetail.orderDetailList.length > 0">
      <view class="section-title">商品信息</view>
      <view class="goods-list">
        <view class="goods-item" v-for="(good, index) in orderDetail.orderDetailList" :key="index">
          <image class="goods-image" :src="good.image" mode="aspectFill"></image>
          <view class="goods-info">
            <text class="goods-name">{{ good.name }}</text>
            <view class="goods-flavor" v-if="good.dishFlavor">{{ formatDishFlavor(good.dishFlavor) }}</view>
            <view class="goods-price-wrap">
              <text class="goods-price">¥{{ good.amount }}</text>
              <text class="goods-count">x{{ good.number }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 价格明细 -->
      <view class="price-detail">
        <view class="price-item">
          <text>商品金额</text>
          <text>¥{{ orderDetail.amount - orderDetail.packAmount - orderDetail.deliverFee }}</text>
        </view>
        <view class="price-item" v-if="orderDetail.packAmount > 0">
          <text>包装费</text>
          <text>¥{{ orderDetail.packAmount }}</text>
        </view>
        <view class="price-item" v-if="orderDetail.deliverFee > 0">
          <text>配送费</text>
          <text>¥{{ orderDetail.deliverFee }}</text>
        </view>
        <view class="price-item total">
          <text>实付金额</text>
          <text>¥{{ orderDetail.amount }}</text>
        </view>
      </view>
    </view>

    <!-- 底部操作区 -->
    <view class="bottom-actions" v-if="orderDetail.status">
      <!-- 待支付状态 -->
      <template v-if="orderDetail.status === 1">
       <!-- <view class="action-btn cancel" @click="handleCancelOrder">取消订单</view> -->
        <view class="action-btn pay" @click="goToPay">去支付</view>
      </template>

      <!-- 已接单或派送中状态，可以催单 -->
      <template v-if="orderDetail.status === 3 || orderDetail.status === 4">
        <view class="action-btn reminder" @click="handleReminderOrder">催单</view>
      </template>

      <!-- 已完成状态，可以再来一单 -->
      <template v-if="orderDetail.status === 5">
        <view class="action-btn repeat" @click="handleRepeatOrder">再来一单</view>
      </template>
    </view>
  </view>
</template>

<script>
import { orderApi } from '@/api/order'
import { shopApi } from '@/api/shop'

export default {
  data() {
    return {
      orderId: null,
      orderDetail: {},
      shopInfo: {},
      shopCache: {}
    }
  },
  onLoad(options) {
    if (options.orderId) {
      this.orderId = options.orderId
      this.loadOrderDetail()
    }

    // 从本地存储中加载店铺缓存
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

    // 加载订单详情
    async loadOrderDetail() {
      try {
        uni.showLoading({
          title: '加载中...'
        })

        const res = await orderApi.getOrderDetail(this.orderId)

        if (res.code === 1 && res.data) {
          this.orderDetail = res.data

          // 如果有店铺ID，先尝试从缓存加载，再获取店铺信息
          if (this.orderDetail.shopId) {
            // 先尝试从缓存获取
            if (this.shopCache && this.shopCache[this.orderDetail.shopId]) {
              const cachedShop = this.shopCache[this.orderDetail.shopId]
              this.shopInfo = {
                id: this.orderDetail.shopId,
                name: cachedShop.name || '未知店铺', // 确保有有效的名称
                image: cachedShop.image || '',
                address: cachedShop.address || '',
                provinceName: cachedShop.provinceName || '',
                cityName: cachedShop.cityName || '',
                districtName: cachedShop.districtName || ''
              }
            } else {
              this.shopInfo = {
                id: this.orderDetail.shopId,
                name: '加载中...'
              }
            }
            // 无论是否从缓存读取到，都请求最新数据
            this.loadShopInfo(this.orderDetail.shopId)
          }
        } else {
          uni.showToast({
            title: res.msg || '获取订单详情失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('获取订单详情失败:', error)
        uni.showToast({
          title: '网络异常，请重试',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    },

    // 加载店铺信息
    async loadShopInfo(shopId) {
      try {
        console.log('开始获取店铺信息, shopId:', shopId)
        uni.showLoading({
          title: '加载店铺信息...'
        })

        const shopRes = await shopApi.getShopDetail(shopId)
        console.log('获取到店铺信息:', shopRes)

        if (shopRes.code === 1 && shopRes.data) {
          // 保存完整的店铺数据
          this.shopInfo = {
            ...shopRes.data,
            // 确保使用shopName作为name字段
            name: shopRes.data.shopName || '未知店铺'
          }

          console.log('处理后的店铺信息:', this.shopInfo)

          // 更新缓存
          if (!this.shopCache) this.shopCache = {}
          this.shopCache[shopId] = {
            name: shopRes.data.shopName || '未知店铺', // 使用shopName字段
            image: shopRes.data.image || '',
            address: shopRes.data.address || '',
            provinceName: shopRes.data.provinceName || '',
            cityName: shopRes.data.cityName || '',
            districtName: shopRes.data.districtName || ''
          }

          // 保存到本地存储
          this.saveShopCache()
        } else {
          console.error('获取店铺信息返回错误:', shopRes)
          uni.showToast({
            title: '获取店铺信息失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('获取店铺信息异常:', error)
        uni.showToast({
          title: '网络异常，请重试',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    },

    // 获取完整地址
    getFullAddress(shop) {
      if (!shop) return '';

      let address = '';

      // 添加省市区
      if (shop.provinceName) {
        address += shop.provinceName;
      }
      if (shop.cityName) {
        address += shop.cityName;
      }
      if (shop.districtName) {
        address += shop.districtName;
      }

      // 添加详细地址
      if (shop.address) {
        address += shop.address;
      }

      return address || '暂无地址信息';
    },

    // 获取订单状态文本
    getStatusText(status) {
      switch (status) {
        case 1: return '等待支付'
        case 2: return '等待商家接单'
        case 3: return '商家已接单'
        case 4: return '派送中'
        case 5: return '订单已完成'
        case 6: return '订单已取消'
        case 7: return '退款'
        default: return '未知状态'
      }
    },

    // 获取支付方式文本
    getPayMethodText(payMethod) {
      switch (payMethod) {
        case 1: return '微信支付'
        case 2: return '模拟支付'
        default: return '其他方式'
      }
    },

    // 获取支付状态文本
    getPayStatusText(payStatus) {
      switch (payStatus) {
        case 0: return '未支付'
        case 1: return '已支付'
        case 2: return '退款中'
        case 3: return '已退款'
        default: return '未知状态'
      }
    },

    // 格式化口味信息
    formatDishFlavor(flavorStr) {
      try {
        const flavorObj = JSON.parse(flavorStr)
        return Object.entries(flavorObj)
          .map(([key, value]) => `${key}: ${value}`)
          .join('、')
      } catch (error) {
        return ''
      }
    },

    // 复制订单号
    copyOrderNumber() {
      uni.setClipboardData({
        data: this.orderDetail.number,
        success: () => {
          uni.showToast({
            title: '订单号已复制',
            icon: 'success'
          })
        }
      })
    },

    // 跳转到店铺
    goToShop() {
      if (!this.orderDetail.shopId) return

      uni.navigateTo({
        url: `/pages/shop/detail?id=${this.orderDetail.shopId}`
      })
    },

    // 去支付
    goToPay() {
      if (this.orderDetail.status !== 1) return

      uni.navigateTo({
        url: `/pages/order/payment?orderNumber=${this.orderDetail.number}&amount=${this.orderDetail.amount}`
      })
    },

    // 取消订单
    async handleCancelOrder() {
      try {
        uni.showModal({
          title: '提示',
          content: '确定要取消该订单吗？',
          success: async (res) => {
            if (res.confirm) {
              uni.showLoading({
                title: '取消中...'
              })

              const result = await orderApi.cancelOrder(this.orderId)

              uni.hideLoading()

              if (result.code === 1) {
                uni.showToast({
                  title: '订单已取消',
                  icon: 'success'
                })

                // 重新加载订单详情
                this.loadOrderDetail()
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

    // 再来一单
    async handleRepeatOrder() {
      try {
        uni.showLoading({
          title: '处理中...'
        })

        const result = await orderApi.repeatOrder(this.orderId)

        uni.hideLoading()

        if (result.code === 1) {
          uni.showToast({
            title: '已添加到购物车',
            icon: 'success'
          })

          // 跳转到购物车页面
          setTimeout(() => {
            uni.switchTab({
              url: '/pages/cart/cart'
            })
          }, 1500)
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

    // 催单
    async handleReminderOrder() {
      try {
        uni.showLoading({
          title: '处理中...'
        })

        const result = await orderApi.reminderOrder(this.orderId)

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
    }
  }
}
</script>

<style lang="scss">
.order-detail {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;

  .status-section {
    background-color: #2588FF;
    padding: 40rpx 30rpx;
    color: #fff;

    .status-text {
      font-size: 36rpx;
      font-weight: 500;
      margin-bottom: 10rpx;
    }

    .status-desc {
      font-size: 28rpx;
      opacity: 0.8;
    }
  }

  .shop-section {
    display: flex;
    align-items: center;
    background-color: #fff;
    padding: 30rpx;
    margin-top: 20rpx;

    .shop-avatar {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      margin-right: 20rpx;
    }

    .shop-info {
      flex: 1;
      display: flex;
      flex-direction: column;

      .shop-name {
        font-size: 30rpx;
        color: #333;
        font-weight: 500;
        margin-bottom: 8rpx;
      }

      .shop-desc {
        font-size: 26rpx;
        color: #999;
      }
    }

    .arrow {
      font-size: 32rpx;
      color: #999;
    }
  }

  .section-title {
    font-size: 30rpx;
    color: #333;
    font-weight: 500;
    margin-bottom: 20rpx;
  }

  .delivery-section,
  .order-info-section,
  .goods-section {
    background-color: #fff;
    padding: 30rpx;
    margin-top: 20rpx;

    .info-item {
      display: flex;
      margin-bottom: 16rpx;
      font-size: 28rpx;

      .label {
        color: #666;
        width: 160rpx;
      }

      .value {
        flex: 1;
        color: #333;
      }

      .copy-btn {
        font-size: 24rpx;
        color: #2588FF;
        padding: 0 10rpx;
      }
    }
  }

  .goods-section {
    .goods-list {
      .goods-item {
        display: flex;
        padding: 20rpx 0;
        border-bottom: 1px solid #f5f5f5;

        &:last-child {
          border-bottom: none;
        }

        .goods-image {
          width: 140rpx;
          height: 140rpx;
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

          .goods-flavor {
            font-size: 24rpx;
            color: #999;
            margin-bottom: 10rpx;
          }

          .goods-price-wrap {
            display: flex;
            justify-content: space-between;

            .goods-price {
              font-size: 28rpx;
              color: #ff5722;
            }

            .goods-count {
              font-size: 26rpx;
              color: #999;
            }
          }
        }
      }
    }

    .price-detail {
      margin-top: 30rpx;
      padding-top: 20rpx;
      border-top: 1px solid #f5f5f5;

      .price-item {
        display: flex;
        justify-content: space-between;
        margin-bottom: 16rpx;
        font-size: 28rpx;
        color: #666;

        &.total {
          margin-top: 20rpx;
          padding-top: 16rpx;
          border-top: 1px dashed #eee;
          font-size: 32rpx;
          color: #ff5722;
          font-weight: 500;
        }
      }
    }
  }

  .bottom-actions {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    height: 100rpx;
    background-color: #fff;
    box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);

    .action-btn {
      flex: 1;
      display: flex;
      justify-content: center;
      align-items: center;
      font-size: 30rpx;

      &.cancel {
        background-color: #fff;
        color: #666;
      }

      &.pay {
        background-color: #ff5722;
        color: #fff;
      }

      &.reminder {
        background-color: #ff9800;
        color: #fff;
      }

      &.repeat {
        background-color: #2588FF;
        color: #fff;
      }
    }
  }
}
</style>