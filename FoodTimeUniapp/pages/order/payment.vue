<template>
  <view class="payment-container">
    <!-- ======================  订单金额区域（截图上方白色卡片） ====================== -->
    <view class="amount-section">
      <text class="amount-label">订单金额</text>
	  <!-- 动态显示订单总价，从上个页面（提交订单页）传过来 -->
      <text class="amount-value">¥{{ amount }}</text>
    </view>

    <!-- ====================== 支付方式选择区域 ====================== -->
    <view class="payment-methods">
      <view class="methods-title">支付方式</view>

      <!-- 模拟支付 -->
	  <!-- @click="selectPayMethod(2)"：点击选中这个支付方式 -->
	  <!-- :class="{ active: payMethod === 2 }"：选中后高亮 -->
      <view class="payment-method-item" @click="selectPayMethod(2)" :class="{ active: payMethod === 2 }">
        <view class="method-info">
			<!-- 支付方式图标 -->
          <image class="method-icon" src="/static/image/payment/simulate.png" mode="aspectFit"></image>
           <!-- 支付方式名称 -->
		  <text class="method-name">模拟支付</text>
        </view>
		   <!-- 选中后显示对勾 -->
        <view class="method-selected" v-if="payMethod === 2">
          <uni-icons type="checkmarkempty" size="20" color="#2588FF"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 支付按钮 -->
    <view class="pay-button" @click="confirmPayment">
      立即支付
    </view>
  </view>
</template>

<script>
import { orderApi } from '@/api/order'

export default {
  data() {
    return {
      orderNumber: '', // 订单号
      amount: 0, // 订单金额
      payMethod: 2, // 支付方式：1-微信支付 2-模拟支付（默认选中模拟支付）
      loading: false
    }
  },
  onLoad(options) {
    if (options.orderNumber) {
      this.orderNumber = options.orderNumber
    }

    if (options.amount) {
      this.amount = Number(options.amount)
    }
  },
  methods: {
    // 选择支付方式
    selectPayMethod(method) {
      this.payMethod = method
    },

    // 确认支付
    async confirmPayment() {
      if (this.loading) return

      if (!this.orderNumber) {
        uni.showToast({
          title: '订单号不能为空',
          icon: 'none'
        })
        return
      }

      this.loading = true

      try {
        const paymentData = {
          orderNumber: this.orderNumber,
          payMethod: this.payMethod
        }

        // 调用支付接口
        const res = await orderApi.simulatePayment(paymentData)

        if (res.code === 1) {
          // 支付成功
          uni.showToast({
            title: '支付成功',
            icon: 'success'
          })

          // 延迟跳转到订单列表页
          setTimeout(() => {
            uni.switchTab({
              url: '/pages/orders/orders'
            })
          }, 1500)
        } else {
          uni.showToast({
            title: res.msg || '支付失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('支付请求失败:', error)
        uni.showToast({
          title: '支付失败，请重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss">
.payment-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 30rpx;

  .amount-section {
    background-color: #fff;
    border-radius: 12rpx;
    padding: 40rpx;
    margin-bottom: 30rpx;
    text-align: center;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

    .amount-label {
      font-size: 28rpx;
      color: #666;
      margin-bottom: 20rpx;
      display: block;
    }

    .amount-value {
      font-size: 60rpx;
      font-weight: bold;
      color: #ff5722;
    }
  }

  .payment-methods {
    background-color: #fff;
    border-radius: 12rpx;
    padding: 30rpx;
    margin-bottom: 40rpx;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

    .methods-title {
      font-size: 30rpx;
      color: #333;
      margin-bottom: 30rpx;
      font-weight: 500;
    }

    .payment-method-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 30rpx 0;
      border-bottom: 1px solid #f5f5f5;

      &:last-child {
        border-bottom: none;
      }

      &.active {
        background-color: rgba(37, 136, 255, 0.05);
      }

      .method-info {
        display: flex;
        align-items: center;

        .method-icon {
          width: 60rpx;
          height: 60rpx;
          margin-right: 20rpx;
        }

        .method-name {
          font-size: 30rpx;
          color: #333;
        }
      }

      .method-selected {
        width: 40rpx;
        height: 40rpx;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }

  .pay-button {
    background-color: #2588FF;
    color: #fff;
    font-size: 32rpx;
    height: 90rpx;
    line-height: 90rpx;
    border-radius: 45rpx;
    text-align: center;
    margin: 60rpx 30rpx;
    font-weight: 500;
    box-shadow: 0 6rpx 15rpx rgba(37, 136, 255, 0.3);
  }
}
</style>