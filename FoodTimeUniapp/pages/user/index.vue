<template>
  <view class="user-container">
    <!-- 顶部背景和个人信息 -->
    <view class="user-header">
      <view class="user-info" @click="handleUserClick">
        <image class="avatar" :src="userStore.isLoggedIn ? userStore.userInfo.avatar : defaultAvatar" mode="aspectFill">
        </image>
        <view class="username">{{ userStore.isLoggedIn ? userStore.userInfo.username : '未登录' }}</view>
      </view>
      <view class="user-action">
        <template v-if="!userStore.isLoggedIn">
          <button class="login-btn" @click.stop="goLogin">登录</button>
        </template>
        <template v-else>
          <!-- 假设您有一个名为 icon-setting 的设置图标 -->
          <text class="iconfont icon-setting" @click.stop="goSetting"></text>
        </template>
      </view>
    </view>

    <!-- 菜单列表 -->
    <view class="menu-list">
      <view class="menu-item" v-for="(item, index) in menuList" :key="index" @click="handleMenuClick(item)">
        <view class="menu-item-left">
          <text class="iconfont" :class="item.icon"></text>
          <text class="menu-text">{{ item.title }}</text>
        </view>
        <text class="iconfont icon-right"></text>
      </view>

      <!-- 客服电话 -->
      <view class="menu-item service">
        <view class="menu-item-left">
          <text class="iconfont icon-service"></text>
          <text class="menu-text">联系客服</text>
        </view>
        <text class="service-phone">400-959-8521</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { useUserStore } from '@/store/user';

// 使用用户状态管理
const userStore = useUserStore();

// 默认头像路径 (请确保您有这个图片)
const defaultAvatar = '/static/image/user/default.jpg';

// 菜单列表
const menuList = ref([
  {
    title: '我的订单',
    icon: 'icon-orders',
    path: 'orders',
    isTabBar: true
  },
  {
    title: '我的地址',
    icon: 'icon-address',
    path: '/pages/address/index'
  },
  {
    title: '关于我们',
    icon: 'icon-about',
    path: '/pages/about/index'
  }
]);

// 跳转到登录页
const goLogin = () => {
  uni.navigateTo({
    url: '/pages/login/index'
  });
};

// 跳转到设置页
const goSetting = () => {
  uni.navigateTo({
    url: '/pages/setting/index'
  });
};

// 头像/用户名区域点击事件
const handleUserClick = () => {
  if (!userStore.isLoggedIn) {
    goLogin();
  } else {
    // 登录后点击头像/用户名也可以跳转到设置页，或根据您的需求调整
    goSetting();
  }
};

// 菜单点击事件
const handleMenuClick = (item) => {
  if (!userStore.isLoggedIn && (item.path === 'orders' || item.path === '/pages/user/comment' || item.path === '/pages/address/index')) {
    uni.showToast({ title: '请先登录', icon: 'none' });
    setTimeout(() => goLogin(), 500);
    return;
  }

  // 如果是TabBar页面，使用switchTab跳转
  if (item.isTabBar) {
    uni.switchTab({
      url: `/pages/${item.path}/orders`
    });
  } else {
    uni.navigateTo({
      url: item.path
    });
  }
};
</script>

<style lang="scss">
.user-container {
  min-height: 100vh;
  background-color: #f5f5f5;

  .user-header {
    position: relative;
    height: 300rpx; // 将高度从 240rpx 增加回 300rpx，以增加底部空间
    background-color: #2588FF;
    padding: 0 30rpx;
    display: flex; // 改为 flex 布局
    align-items: center; // 垂直居中
    justify-content: space-between; // 两端对齐

    .user-info {
      display: flex;
      align-items: center;
      padding-top: 20rpx; // 保持顶部内边距不变
      cursor: pointer; // 添加手型光标，提示可点击

      .avatar {
        width: 120rpx;
        height: 120rpx;
        border-radius: 50%;
        border: 4rpx solid rgba(255, 255, 255, 0.3);
      }

      .username {
        margin-left: 20rpx;
        font-size: 36rpx;
        color: #fff;
        font-weight: 400;
      }
    }

    // 新增右侧操作区域样式
    .user-action {
      display: flex;
      align-items: center;
      padding-top: 20rpx; // 与 user-info 对齐

      .login-btn {
        background: #fff;
        color: #2588FF;
        font-size: 26rpx; // 字体稍微调小
        border-radius: 40rpx;
        padding: 6rpx 28rpx; // 减小内边距，使按钮更紧凑
        border: none;
        outline: none;
        font-weight: 400;

        // 移除 uni-app 按钮默认边框和背景
        &::after {
          border: none;
        }
      }

      .icon-setting {
        font-size: 40rpx; // 设置图标大小
        color: #fff;
        cursor: pointer; // 添加手型光标
      }
    }
  }

  .menu-list {
    position: relative;
    margin-top: -50rpx;
    margin-left: 30rpx;
    margin-right: 30rpx;
    border-radius: 16rpx;
    background-color: #fff;
    padding: 20rpx 0;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);

    .menu-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 30rpx 40rpx;

      .menu-item-left {
        display: flex;
        align-items: center;

        .iconfont {
          font-size: 36rpx;
          color: #2588FF;
          margin-right: 20rpx;
        }

        .menu-text {
          font-size: 28rpx;
          color: #333;
        }
      }

      .icon-right {
        font-size: 28rpx;
        color: #ccc;
      }

      &.service {
        border-top: 2rpx solid #f5f5f5;
        margin-top: 10rpx;

        .service-phone {
          font-size: 28rpx;
          color: #999;
        }
      }
    }
  }
}
</style>