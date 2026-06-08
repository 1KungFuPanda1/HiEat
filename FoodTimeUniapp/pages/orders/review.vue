<template>
  <view class="review-container">
    <view class="header">
      <text class="title">评价订单</text>
    </view>

    <view class="shop-info" v-if="shopInfo">
      <image class="shop-avatar" :src="shopInfo.image || '/static/image/shop/default-shop.png'" mode="aspectFill">
      </image>
      <text class="shop-name">{{ shopInfo.name }}</text>
    </view>

    <!-- 评分 -->
    <view class="rating-section">
      <text class="section-title">店铺评分</text>
      <view class="rating-stars">
        <view v-for="star in 5" :key="star" class="star-item" @click="setRating(star)">
          <text class="iconfont icon-star" :class="{ active: star <= rating }"></text>
        </view>
      </view>
      <text class="rating-text">{{ getRatingText() }}</text>
    </view>

    <!-- 评价标题 -->
    <view class="form-item">
      <text class="item-label">评价标题</text>
      <input class="item-input" v-model="title" placeholder="简短的标题" maxlength="30" />
    </view>

    <!-- 评价内容 -->
    <view class="form-item">
      <text class="item-label">评价内容</text>
      <textarea class="item-textarea" v-model="content" placeholder="分享您的用餐体验（口味、服务、外卖体验等）" maxlength="500" />
      <text class="word-count">{{ content.length }}/500</text>
    </view>

    <!-- 上传图片 -->
    <view class="form-item">
      <text class="item-label">上传图片(选填)</text>
      <view class="upload-area">
        <view class="image-list">
          <view class="image-item" v-for="(url, index) in photoUrls" :key="index">
            <image :src="url" mode="aspectFill"></image>
            <text class="delete-icon" @click="deleteImage(index)">×</text>
          </view>
          <view class="upload-btn" @click="chooseImage" v-if="photoUrls.length < 6">
            <text class="iconfont icon-camera"></text>
            <text>添加图片</text>
          </view>
        </view>
        <text class="upload-tip">最多上传6张图片</text>
      </view>
    </view>

    <!-- 提交按钮 -->
    <button class="submit-btn" @click="submitReview" :disabled="!canSubmit">提交评价</button>
  </view>
</template>

<script>
import { reviewApi } from '@/api/review'
import { shopApi } from '@/api/shop'
import { orderApi } from '@/api/order'
import { commonApi } from '@/api/common'

export default {
  data() {
    return {
      orderId: null,
      shopId: null,
      shopInfo: null,
      rating: 5,
      title: '',
      content: '',
      photoUrls: [],
      submitting: false
    }
  },
  computed: {
    /**
     * 判断是否可以提交评价
     * @returns {boolean} 是否可以提交
     */
    canSubmit() {
      return this.rating > 0 && this.title.trim() && this.content.trim() && !this.submitting
    }
  },
  onLoad(options) {
    if (options.orderId) {
      this.orderId = options.orderId
      this.loadOrderDetail()
    } else {
      uni.showToast({
        title: '缺少订单信息',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  },
  methods: {
    /**
     * 加载订单详情
     */
    async loadOrderDetail() {
      try {
        uni.showLoading({
          title: '加载中...'
        })

        // 从API获取订单详细信息
        const res = await orderApi.getOrderDetail(this.orderId)

        uni.hideLoading()

        if (res.code === 1 && res.data) {
          this.shopId = res.data.shopId
          this.loadShopInfo(this.shopId)
        } else {
          uni.showToast({
            title: res.msg || '获取订单信息失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('加载订单详情失败:', error)
        uni.showToast({
          title: '网络异常，请重试',
          icon: 'none'
        })
      }
    },

    /**
     * 加载店铺信息
     * @param {number} shopId - 店铺ID
     */
    async loadShopInfo(shopId) {
      try {
        if (!shopId) return

        uni.showLoading({
          title: '加载店铺信息...'
        })

        const res = await shopApi.getShopDetail(shopId)

        uni.hideLoading()

        if (res.code === 1 && res.data) {
          this.shopInfo = {
            name: res.data.shopName || '未知店铺',
            image: res.data.image || '/static/image/shop/default-shop.png'
          }
        } else {
          uni.showToast({
            title: res.msg || '获取店铺信息失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('加载店铺信息失败:', error)
      }
    },

    /**
     * 设置评分
     * @param {number} value - 评分值
     */
    setRating(value) {
      this.rating = value
    },

    /**
     * 获取评分文本描述
     * @returns {string} 评分描述
     */
    getRatingText() {
      const texts = ['', '失望', '一般', '满意', '不错', '非常满意']
      return texts[this.rating] || ''
    },

    /**
     * 选择图片
     */
    chooseImage() {
      if (this.photoUrls.length >= 6) {
        uni.showToast({
          title: '最多上传6张图片',
          icon: 'none'
        })
        return
      }

      uni.chooseImage({
        count: 6 - this.photoUrls.length,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          this.uploadImages(res.tempFilePaths)
        }
      })
    },

    /**
     * 上传图片到服务器
     * @param {Array<string>} tempFilePaths - 临时文件路径
     */
    async uploadImages(tempFilePaths) {
      if (!tempFilePaths || tempFilePaths.length === 0) return

      uni.showLoading({
        title: '上传中...'
      })

      try {
        // 使用commonApi上传图片
        const uploadTasks = tempFilePaths.map(filePath => {
          return commonApi.uploadFile(filePath).then(response => {
            if (response.code === 1 && response.data) {
              return response.data;
            }
            return null;
          });
        });

        // 等待所有图片上传完成
        const responses = await Promise.all(uploadTasks);

        // 过滤有效的URL并添加到图片数组中
        const validUrls = responses.filter(url => url !== null);
        this.photoUrls = [...this.photoUrls, ...validUrls].slice(0, 6);

        uni.hideLoading();
      } catch (error) {
        console.error('上传图片失败:', error);
        uni.hideLoading();
        // 错误提示已在commonApi中处理
      }
    },

    /**
     * 删除已上传图片
     * @param {number} index - 图片索引
     */
    deleteImage(index) {
      this.photoUrls.splice(index, 1)
    },

    /**
     * 提交评价
     */
    async submitReview() {
      if (!this.canSubmit) return

      if (!this.shopId) {
        uni.showToast({
          title: '店铺信息缺失',
          icon: 'none'
        })
        return
      }

      this.submitting = true

      uni.showLoading({
        title: '提交中...'
      })

      try {
        const reviewData = {
          shopId: this.shopId,
          rating: this.rating,
          title: this.title.trim(),
          content: this.content.trim(),
          photoUrls: this.photoUrls,
          orderId: this.orderId // 添加订单ID关联
        }

        const res = await reviewApi.submitReview(reviewData)

        uni.hideLoading()
        this.submitting = false

        if (res.code === 1) {
          uni.showToast({
            title: '评价成功',
            icon: 'success'
          })

          // 延迟返回
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          uni.showToast({
            title: res.msg || '评价失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        this.submitting = false
        console.error('提交评价失败:', error)
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
.review-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;

  .header {
    padding: 20rpx 0;

    .title {
      font-size: 36rpx;
      font-weight: 500;
      color: #333;
    }
  }

  .shop-info {
    background-color: #fff;
    border-radius: 12rpx;
    padding: 30rpx;
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;

    .shop-avatar {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      margin-right: 20rpx;
    }

    .shop-name {
      font-size: 30rpx;
      color: #333;
      font-weight: 500;
    }
  }

  .rating-section {
    background-color: #fff;
    border-radius: 12rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;

    .section-title {
      font-size: 28rpx;
      color: #333;
      margin-bottom: 20rpx;
      display: block;
    }

    .rating-stars {
      display: flex;
      justify-content: center;
      margin-bottom: 20rpx;

      .star-item {
        padding: 0 10rpx;
        cursor: pointer;

        .icon-star {
          font-size: 60rpx;
          color: #ddd;

          &.active {
            color: #ffb800;
          }
        }
      }
    }

    .rating-text {
      text-align: center;
      display: block;
      font-size: 28rpx;
      color: #666;
    }
  }

  .form-item {
    background-color: #fff;
    border-radius: 12rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;

    .item-label {
      font-size: 28rpx;
      color: #333;
      margin-bottom: 20rpx;
      display: block;
    }

    .item-input {
      width: 100%;
      height: 80rpx;
      border: 1px solid #eee;
      border-radius: 8rpx;
      padding: 0 20rpx;
      font-size: 28rpx;
    }

    .item-textarea {
      width: 100%;
      height: 240rpx;
      border: 1px solid #eee;
      border-radius: 8rpx;
      padding: 20rpx;
      font-size: 28rpx;
    }

    .word-count {
      text-align: right;
      font-size: 24rpx;
      color: #999;
      margin-top: 10rpx;
      display: block;
    }

    .upload-area {
      .image-list {
        display: flex;
        flex-wrap: wrap;

        .image-item {
          width: 160rpx;
          height: 160rpx;
          margin-right: 20rpx;
          margin-bottom: 20rpx;
          position: relative;

          image {
            width: 100%;
            height: 100%;
            border-radius: 8rpx;
          }

          .delete-icon {
            position: absolute;
            top: -16rpx;
            right: -16rpx;
            width: 40rpx;
            height: 40rpx;
            background-color: rgba(0, 0, 0, 0.5);
            color: #fff;
            border-radius: 50%;
            text-align: center;
            line-height: 36rpx;
            font-size: 32rpx;
          }
        }

        .upload-btn {
          width: 160rpx;
          height: 160rpx;
          border: 1px dashed #ddd;
          border-radius: 8rpx;
          display: flex;
          flex-direction: column;
          justify-content: center;
          align-items: center;

          .icon-camera {
            font-size: 48rpx;
            color: #999;
            margin-bottom: 10rpx;
          }

          text {
            font-size: 24rpx;
            color: #999;
          }
        }
      }

      .upload-tip {
        font-size: 24rpx;
        color: #999;
        margin-top: 10rpx;
      }
    }
  }

  .submit-btn {
    background-color: #2588FF;
    color: #fff;
    font-size: 32rpx;
    height: 90rpx;
    line-height: 90rpx;
    border-radius: 45rpx;
    margin-top: 40rpx;
    width: 100%;

    &:active {
      opacity: 0.8;
    }

    &[disabled] {
      background-color: #ccc;
      opacity: 0.6;
    }
  }
}
</style>