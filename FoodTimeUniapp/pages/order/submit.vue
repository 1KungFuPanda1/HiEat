<template>
  <view class="submit-order">
   <!-- ======================  收货地址模块 ====================== -->
       <!-- 点击整个区域 → 跳转到选择地址页面 -->
    <view class="address-section" @click="goToAddress">
        <!-- 如果已经选择了地址 → 显示地址信息 -->
	  <template v-if="address">
        <view class="address-info">
			 <!-- 收货人 + 电话 -->
          <view class="user-info">
            <text class="name">{{ address.consignee }}</text>
            <text class="phone">{{ address.phone }}</text>
          </view>
		    <!-- 详细地址：省+市+区+详细地址 -->
          <view class="address-detail">
            <text>{{ address.provinceName }}{{ address.cityName }}{{ address.districtName }}{{ address.detail }}</text>
          </view>
        </view>
      </template>
	   <!-- 如果没有选择地址 → 显示提示文字 -->
      <template v-else>
        <view class="no-address">
          请选择收货地址
        </view>
      </template>
	     <!-- 右侧箭头图标 -->
      <view class="arrow">></view>
    </view>

     <!-- ======================  配送信息模块 ====================== -->
    <view class="delivery-section">
	  <!-- 配送方式：固定为立即配送 -->
      <view class="delivery-type">
        <text class="label">配送方式</text>
        <text class="value">立即配送</text>
      </view>
	   <!-- 预计送达时间：JS动态计算出来 -->
      <view class="delivery-time">
        <text class="label">预计送达</text>
        <text class="value">{{ expectedTime }}</text>
      </view>
    </view>

     <!-- ====================== 商品列表（购物车商品） ====================== -->
    <view class="goods-list">
		  <!-- 循环渲染购物车里的每一个商品 -->
      <view class="goods-item" v-for="item in cartList" :key="item.id">
         <!-- 商品图片：属性绑定 -->
		<image class="goods-image" :src="item.image" mode="aspectFill"></image>
        <view class="goods-info">
           <!-- 商品名称 -->
		  <view class="goods-name">{{ item.name }}</view>
           <!-- 商品口味/备注（如：不要辣、不要葱），有值才显示 -->
		  <view class="goods-flavor" v-if="item.dishFlavor">
            <text>{{ formatDishFlavor(item.dishFlavor) }}</text>
          </view>
		  <!-- 商品价格 × 数量 -->
          <view class="goods-price">
            <text class="price">¥{{ item.amount }}</text>
            <text class="count">x{{ item.number }}</text>
          </view>
        </view>
      </view>
    </view>

  
    <!-- ======================  餐具数量选择 ====================== -->
    <view class="tableware-section">
      <view class="section-title">餐具数量</view>
	    <!-- 两个选项：按餐量提供 / 选择数量 -->
      <view class="tableware-options">
		  <!-- 选中时动态添加 active 类（高亮） -->
        <view class="option" :class="{ 'active': tablewareStatus === 1 }" @click="tablewareStatus = 1">按餐量提供</view>
        <view class="option" :class="{ 'active': tablewareStatus === 0 }" @click="tablewareStatus = 0">选择数量</view>
      </view>
	   <!-- 如果选择【选择数量】→ 显示加减控件 -->
      <view class="tableware-number" v-if="tablewareStatus === 0">
        <view class="number-control">
          <text class="minus" @click="updateTablewareNumber(-1)">-</text>
          <text class="number">{{ tablewareNumber }}</text>
          <text class="plus" @click="updateTablewareNumber(1)">+</text>
        </view>
      </view>
    </view>

    
    <!-- ======================  订单备注 ====================== -->
    <view class="remark-section">
      <view class="section-title">备注</view>
      <input type="text" v-model="remark" placeholder="请输入备注信息" class="remark-input" />
    </view>

       <!-- ====================== 支付方式 ====================== -->
    <view class="payment-section">
      <text class="section-title">支付方式</text>
      <view class="payment-list">
		  <!-- 目前只支持在线支付 -->
        <view class="payment-item" :class="{ 'active': payMethod === 1 }" @click="payMethod = 1">
          <text class="payment-name">在线支付</text>
          <text class="payment-selected" v-if="payMethod === 1">✓</text>
        </view>
      </view>
    </view>

    <!-- ====================== 金额汇总 ====================== -->
    <view class="amount-section">
		 <!-- 商品总价 -->
      <view class="amount-item">
        <text class="label">商品金额</text>
        <text class="value">¥{{ totalAmount }}</text>
      </view>
	  <!-- 配送费 -->
      <view class="amount-item">
        <text class="label">配送费</text>
        <text class="value">¥{{ deliveryFee }}</text>
      </view>
	    <!-- 打包费 -->
      <view class="amount-item">
        <text class="label">打包费</text>
        <text class="value">¥{{ packAmount }}</text>
      </view>
	   <!-- 实付金额（最终要支付的钱） -->
      <view class="amount-item total">
        <text class="label">实付金额</text>
        <text class="value">¥{{ finalAmount }}</text>
      </view>
    </view>

     <!-- ====================== 提交订单按钮 ====================== -->
       <!-- 点击 → 提交订单到后端 -->
    <view class="submit-btn" @click="submitOrder">
      提交订单
    </view>
  </view>
</template>

<script>
import { orderApi } from '@/api/order'
import { shoppingCartApi } from '@/api/shoppingCart'
import { addressApi } from '@/api/address'
import { shopApi } from '@/api/shop'
import { formatDate } from '@/utils/date'

export default {
  data() {
    return {
      shopId: '',
      shopInfo: null,
      address: null,
      selectedAddressId: null, // 记录选中的地址ID
      cartList: [],
      totalAmount: 0,
      deliveryFee: 0,
      packAmount: 5, // 打包费，默认5元（菜品数*1）
      finalAmount: 0,
      expectedTime: '',
      averageDeliveryTime: 30, // 平均配送时间（分钟）
      payMethod: 1, // 支付方式：1-在线支付
      remark: '', // 备注
      tablewareStatus: 1, // 餐具数量状态：1-按餐量提供 0-选择具体数量
      tablewareNumber: 1, // 餐具数量
      deliveryStatus: 1, // 配送状态：1-立即送出
    }
  },
  onLoad(options) {
    this.shopId = options.shopId
    this.loadData()

    // 监听地址选择事件
    uni.$on('addressSelected', this.handleAddressSelected);
  },
  onUnload() {
    // 解除监听
    uni.$off('addressSelected', this.handleAddressSelected);
  },
  onShow() {
    // 页面显示时，如果有选中的地址ID，则重新获取该地址信息
    if (this.selectedAddressId) {
      this.loadSelectedAddress()
    }
  },
  methods: {
    async loadData() {
      try {
        // 修改加载顺序，不再让后续数据依赖于地址加载结果
        // 并行加载数据以提高效率
        const promises = [
          this.loadAddress().catch(err => {
            console.error('加载地址信息失败:', err)
            // 地址加载失败不影响其他数据加载
          }),
          this.loadShopInfo(),
          this.loadCart()
        ]
        
        await Promise.all(promises)
      } catch (error) {
        console.error('加载数据失败:', error)
        uni.showToast({
          title: '部分数据加载失败',
          icon: 'none'
        })
      }
    },
    // 加载地址信息
    async loadAddress() {
      try {
        // 如果有选中的地址ID，获取该地址信息
        if (this.selectedAddressId) {
          await this.loadSelectedAddress()
        } else {
          // 否则获取默认地址
          const addressRes = await addressApi.getDefaultAddress()
          if (addressRes.code === 1 && addressRes.data) {
            this.address = addressRes.data
            this.selectedAddressId = addressRes.data.id
          }
          // 如果没有默认地址，不做任何处理，地址保持为null
        }
      } catch (error) {
        console.error('获取地址信息失败:', error)
        // 不抛出异常，允许继续加载其他数据
      }
    },
    // 加载选中的地址
    async loadSelectedAddress() {
      try {
        const addressRes = await addressApi.getAddressById(this.selectedAddressId)
        if (addressRes.code === 1 && addressRes.data) {
          this.address = addressRes.data
        }
      } catch (error) {
        console.error('获取选中地址失败:', error)
      }
    },
    // 加载店铺信息
    async loadShopInfo() {
      try {
        // 修改为使用正确的API方法名 - getShopDetail
        const shopRes = await shopApi.getShopDetail(this.shopId)
        if (shopRes.code === 1 && shopRes.data) {
          this.shopInfo = shopRes.data
          // 设置配送费，后端返回的是分，需要转换为元
          this.deliveryFee = this.shopInfo.deliverFee ? this.shopInfo.deliverFee / 100 : 0
          console.log("配送费：" + this.deliveryFee)
          // 设置平均配送时间
          this.averageDeliveryTime = this.shopInfo.averageSendTime || 30
          this.calculateExpectedTime()

          // 如果已加载购物车数据，重新计算总金额
          if (this.cartList.length > 0) {
            this.calculateAmount()
          }
        }
      } catch (error) {
        console.error('获取店铺信息失败:', error)
        // 显示错误但不阻止其他加载
        uni.showToast({
          title: '获取店铺信息失败',
          icon: 'none'
        })
      }
    },
    // 加载购物车
    async loadCart() {
      const cartRes = await shoppingCartApi.listShoppingCartByShopId(this.shopId)
      if (cartRes.code === 1 && cartRes.data) {
        this.cartList = cartRes.data
        this.packAmount = this.cartList.length * 1
        this.calculateAmount()
      }
    },
    calculateAmount() {
      // 计算商品总金额
      this.totalAmount = this.cartList.reduce((sum, item) => {
        return sum + item.amount * item.number
      }, 0)

      // 计算最终金额
      console.log("总金额：" + this.totalAmount + " 配送费：" + this.deliveryFee + " 打包费：" + this.packAmount)

      // 确保配送费不为空，如果还没有加载店铺信息，使用0作为默认值
      const deliveryFee = typeof this.deliveryFee === 'number' ? this.deliveryFee : 0

      this.finalAmount = this.totalAmount + deliveryFee + this.packAmount
    },
    calculateExpectedTime() {
      const now = new Date()
      const expectedDate = new Date(now.getTime() + this.averageDeliveryTime * 60000)
      // 格式化为 yyyy-MM-dd HH:mm:ss
      const year = expectedDate.getFullYear()
      const month = String(expectedDate.getMonth() + 1).padStart(2, '0')
      const day = String(expectedDate.getDate()).padStart(2, '0')
      const hours = String(expectedDate.getHours()).padStart(2, '0')
      const minutes = String(expectedDate.getMinutes()).padStart(2, '0')
      const seconds = String(expectedDate.getSeconds()).padStart(2, '0')
      this.expectedTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    },
    updateTablewareNumber(step) {
      const newNumber = this.tablewareNumber + step
      if (newNumber >= 1) {
        this.tablewareNumber = newNumber
      }
    },
    goToAddress() {
      // 跳转到地址列表页面，并传递当前选中的地址ID
      uni.navigateTo({
        url: `/pages/address/index?selected=${this.selectedAddressId || ''}`
      })
    },
    async submitOrder() {
      if (!this.address) {
        uni.showToast({
          title: '请选择收货地址',
          icon: 'none'
        })
        return
      }

      try {
        const orderData = {
          shopId: this.shopId,
          addressBookId: this.address.id,
          payMethod: this.payMethod,
          remark: this.remark,
          estimatedDeliveryTime: this.expectedTime,
          deliveryStatus: this.deliveryStatus,
          tablewareNumber: this.tablewareNumber,
          tablewareStatus: this.tablewareStatus,
          packAmount: this.packAmount,
          amount: this.finalAmount,
          deliverFee: this.deliveryFee
        }

        const res = await orderApi.submitOrder(orderData)

        if (res.code === 1) {
          // 提交成功后跳转到支付页面
          uni.redirectTo({
            url: `/pages/order/payment?orderNumber=${res.data.orderNumber}&amount=${res.data.orderAmount}`
          })
        } else {
          uni.showToast({
            title: res.msg || '提交订单失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.showToast({
          title: '提交订单失败',
          icon: 'none'
        })
      }
    },
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
    // 处理地址选择
    handleAddressSelected(address) {
      // 更新为使用地址对象的ID，而非单独传递ID
      this.selectedAddressId = address.id
      // 直接更新地址对象，避免再次请求
      this.address = address
    }
  }
}
</script>

<style lang="scss">
.submit-order {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;

  .section-title {
    font-size: 28rpx;
    color: #333;
    margin-bottom: 20rpx;
  }

  .address-section {
    background-color: #fff;
    padding: 30rpx;
    margin-bottom: 20rpx;
    display: flex;
    align-items: center;

    .address-info {
      flex: 1;

      .user-info {
        margin-bottom: 10rpx;

        .name {
          font-size: 32rpx;
          margin-right: 20rpx;
        }

        .phone {
          font-size: 28rpx;
          color: #666;
        }
      }

      .address-detail {
        font-size: 28rpx;
        color: #333;
      }
    }

    .no-address {
      font-size: 32rpx;
      color: #666;
    }

    .arrow {
      font-size: 32rpx;
      color: #999;
    }
  }

  .delivery-section {
    background-color: #fff;
    padding: 30rpx;
    margin-bottom: 20rpx;

    .delivery-type,
    .delivery-time {
      display: flex;
      justify-content: space-between;
      margin-bottom: 20rpx;

      .label {
        font-size: 28rpx;
        color: #333;
      }

      .value {
        font-size: 28rpx;
        color: #666;
      }
    }
  }

  .goods-list {
    background-color: #fff;
    padding: 30rpx;
    margin-bottom: 20rpx;

    .goods-item {
      display: flex;
      margin-bottom: 20rpx;

      .goods-image {
        width: 120rpx;
        height: 120rpx;
        margin-right: 20rpx;
        border-radius: 8rpx;
      }

      .goods-info {
        flex: 1;

        .goods-name {
          font-size: 28rpx;
          color: #333;
          margin-bottom: 10rpx;
        }

        .goods-flavor {
          font-size: 24rpx;
          color: #666;
          margin-bottom: 10rpx;
        }

        .goods-price {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .price {
            font-size: 28rpx;
            color: #ff5722;
            font-weight: 500;
          }

          .count {
            font-size: 26rpx;
            color: #666;
          }
        }
      }
    }
  }

  .tableware-section {
    background-color: #fff;
    padding: 30rpx;
    margin-bottom: 20rpx;

    .tableware-options {
      display: flex;
      margin-bottom: 20rpx;

      .option {
        padding: 10rpx 20rpx;
        font-size: 26rpx;
        color: #666;
        background-color: #f5f5f5;
        margin-right: 20rpx;
        border-radius: 30rpx;

        &.active {
          color: #fff;
          background-color: #2588FF;
        }
      }
    }

    .tableware-number {
      .number-control {
        display: flex;
        align-items: center;

        .minus,
        .plus {
          width: 50rpx;
          height: 50rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          background-color: #f5f5f5;
          border-radius: 25rpx;
          font-size: 28rpx;
        }

        .number {
          margin: 0 30rpx;
          font-size: 28rpx;
        }
      }
    }
  }

  .remark-section {
    background-color: #fff;
    padding: 30rpx;
    margin-bottom: 20rpx;

    .remark-input {
      width: 100%;
      height: 80rpx;
      background-color: #f5f5f5;
      border-radius: 8rpx;
      padding: 0 20rpx;
      font-size: 26rpx;
    }
  }

  .payment-section {
    background-color: #fff;
    padding: 30rpx;
    margin-bottom: 20rpx;

    .payment-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx 0;

      .payment-name {
        font-size: 28rpx;
        color: #333;
      }

      .payment-selected {
        color: #2588FF;
      }

      &.active {
        .payment-name {
          color: #2588FF;
        }
      }
    }
  }

  .amount-section {
    background-color: #fff;
    padding: 30rpx;

    .amount-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 20rpx;

      .label {
        font-size: 28rpx;
        color: #333;
      }

      .value {
        font-size: 28rpx;
        color: #666;
      }

      &.total {
        margin-top: 20rpx;
        padding-top: 20rpx;
        border-top: 1px solid #eee;

        .label,
        .value {
          font-size: 32rpx;
          color: #ff5722;
          font-weight: bold;
        }
      }
    }
  }

  .submit-btn {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    height: 100rpx;
    line-height: 100rpx;
    text-align: center;
    background-color: #2588FF;
    color: #fff;
    font-size: 32rpx;
  }
}
</style>