<template>
  <view class="setting-container">
    <view class="setting-list">
      <!-- 个人信息设置 -->
      <view class="setting-group">
        <view class="setting-item" @click="goToProfile">
          <text class="setting-item-title">个人信息</text>
          <text class="iconfont icon-right"></text>
        </view>
      </view>

      <!-- 关于和帮助 -->
      <view class="setting-group">
        <view class="setting-item" @click="goToHelp">
          <text class="setting-item-title">帮助与反馈</text>
          <text class="iconfont icon-right"></text>
        </view>
      </view>

      <!-- 退出登录按钮 -->
      <view class="setting-group" v-if="userStore.isLoggedIn">
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useUserStore } from '@/store/user';

// 使用用户状态管理
const userStore = useUserStore();

// 跳转到个人信息页面
const goToProfile = () => {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' });
    return;
  }
  uni.navigateTo({
    url: '/pages/user/profile'
  });
};

// 跳转到帮助与反馈页面
const goToHelp = () => {
  uni.navigateTo({
    url: '/pages/help/index'
  });
};

// 退出登录
const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        // 调用store中的退出登录方法
        userStore.logout();

        // 提示退出成功
        uni.showToast({
          title: '已退出登录',
          icon: 'success'
        });
      }
    }
  });
};
</script>

<style lang="scss">
.setting-container {
  min-height: 100vh;
  background-color: #f5f5f5;

  .setting-list {
    padding: 20rpx;

    .setting-group {
      background-color: #fff;
      border-radius: 12rpx;
      margin-bottom: 20rpx;
      overflow: hidden;

      .setting-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 30rpx;
        border-bottom: 1rpx solid #f5f5f5;

        &:last-child {
          border-bottom: none;
        }

        .setting-item-title {
          font-size: 28rpx;
          color: #333;
        }

        .setting-item-value {
          font-size: 26rpx;
          color: #999;
        }

        .icon-right {
          font-size: 24rpx;
          color: #ccc;
        }
      }

      .logout-btn {
        width: 100%;
        height: 90rpx;
        line-height: 90rpx;
        text-align: center;
        font-size: 30rpx;
        color: #ff4d4f;
        background-color: #fff;
        border: none;
        border-radius: 0;

        &::after {
          border: none;
        }

        &:active {
          background-color: #f8f8f8;
        }
      }
    }
  }
}
</style>