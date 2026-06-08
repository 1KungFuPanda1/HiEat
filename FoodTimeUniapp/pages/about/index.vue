<template>
  <view class="about-container">
    <view class="logo-section">
      <image class="logo" src="/static/image/logo.png" mode="aspectFit"></image>
      <text class="app-name">好食光外卖</text>
      <text class="version">版本 1.0.0</text>
    </view>

    <view class="content-section">
      <view class="section-title">公司简介</view>
      <view class="section-content">
        <text>
          好食光是一家专注于为用户提供优质外卖服务的平台，致力于将最佳的美食体验带给每一位用户。
          我们与众多优质餐厅合作，提供多样化的菜品选择，让用户足不出户就能享受美食。
        </text>
      </view>

      <view class="section-title">服务理念</view>
      <view class="section-content">
        <text>
          我们的使命是"让生活更美好"。我们相信，好的食物可以改变人的心情，让一天充满能量。
          好食光通过提供便捷、高效、可靠的外卖服务，让用户在忙碌的生活中也能享受到美食带来的幸福感。
        </text>
      </view>

      <view class="section-title">联系我们</view>
      <view class="section-content">
        <view class="contact-item">
          <text class="contact-label">客服电话：</text>
          <text class="contact-value">400-959-8521</text>
        </view>
        <view class="contact-item">
          <text class="contact-label">客服邮箱：</text>
          <text class="contact-value">service@foodtime.com</text>
        </view>
        <view class="contact-item">
          <text class="contact-label">公司地址：</text>
          <text class="contact-value">北京市朝阳区建国路88号foodtime大厦</text>
        </view>
      </view>
    </view>

    <view class="footer">
      <text class="copyright">Copyright © 2023-2024 foodtime.com All Rights Reserved</text>
    </view>
  </view>
</template>

<script setup>
// 不需要特殊的业务逻辑，这是一个静态展示页面
</script>

<style lang="scss">
.about-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;

  .logo-section {
    background-color: #fff;
    padding: 60rpx 0;
    display: flex;
    flex-direction: column;
    align-items: center;

    .logo {
      width: 160rpx;
      height: 160rpx;
      border-radius: 24rpx;
      margin-bottom: 20rpx;
    }

    .app-name {
      font-size: 36rpx;
      font-weight: 500;
      color: #333;
      margin-bottom: 10rpx;
    }

    .version {
      font-size: 24rpx;
      color: #999;
    }
  }

  .content-section {
    margin-top: 20rpx;
    background-color: #fff;
    padding: 30rpx;

    .section-title {
      font-size: 32rpx;
      font-weight: 500;
      color: #333;
      margin-bottom: 20rpx;
      margin-top: 30rpx;

      &:first-child {
        margin-top: 0;
      }
    }

    .section-content {
      font-size: 28rpx;
      color: #666;
      line-height: 1.6;
      margin-bottom: 30rpx;

      .contact-item {
        display: flex;
        margin-bottom: 16rpx;

        .contact-label {
          color: #333;
          min-width: 150rpx;
        }

        .contact-value {
          flex: 1;
        }
      }
    }
  }

  .footer {
    text-align: center;
    padding: 40rpx 0;

    .copyright {
      font-size: 24rpx;
      color: #999;
    }
  }
}
</style>