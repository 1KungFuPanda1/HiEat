<template>
  <view class="register-container">
    <view class="form-group">
      <view class="input-item">
        <text class="label">用户名</text>
        <input type="text" v-model="formData.username" placeholder="请输入用户名" />
      </view>
      <view class="input-item">
        <text class="label">手机号</text>
        <input type="number" v-model="formData.phone" placeholder="请输入手机号" maxlength="11" />
      </view>
      <view class="input-item">
        <text class="label">密码</text>
        <input type="password" v-model="formData.password" placeholder="请输入密码" />
      </view>
    </view>

    <view class="submit-btn" @click="handleRegister">注册</view>

    <view class="login-link" @click="goToLogin">
      已有账号？去登录
    </view>
  </view>
</template>

<script>
import { userApi } from '@/api/user'

export default {
  data() {
    return {
      formData: {
        username: '',
        phone: '',
        password: ''
      }
    }
  },
  methods: {
    // 注册
    async handleRegister() {
      // 表单验证
      if (!this.formData.username) {
        uni.showToast({
          title: '请输入用户名',
          icon: 'none'
        })
        return
      }

      if (!this.formData.phone) {
        uni.showToast({
          title: '请输入手机号',
          icon: 'none'
        })
        return
      }

      if (!/^1[3-9]\d{9}$/.test(this.formData.phone)) {
        uni.showToast({
          title: '手机号格式不正确',
          icon: 'none'
        })
        return
      }

      if (!this.formData.password) {
        uni.showToast({
          title: '请输入密码',
          icon: 'none'
        })
        return
      }

      try {
        uni.showLoading({
          title: '注册中...'
        })

        const res = await userApi.register(this.formData)

        uni.hideLoading()

        if (res.code === 1) {
          // 保存用户信息和token
          uni.setStorageSync('userInfo', res.data)
          uni.setStorageSync('token', res.data.token)

          uni.showToast({
            title: '注册成功',
            icon: 'success'
          })

          // 延迟跳转到首页
          setTimeout(() => {
            uni.switchTab({
              url: '/pages/index/index'
            })
          }, 1500)
        } else {
          uni.showToast({
            title: res.msg || '注册失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('注册失败:', error)
        uni.showToast({
          title: '网络异常，请重试',
          icon: 'none'
        })
      }
    },

    // 跳转到登录页
    goToLogin() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss">
.register-container {
  min-height: 100vh;
  background-color: #fff;
  padding: 40rpx;

  .form-group {
    margin-top: 40rpx;

    .input-item {
      margin-bottom: 30rpx;

      .label {
        font-size: 28rpx;
        color: #333;
        margin-bottom: 20rpx;
        display: block;
      }

      input {
        width: 100%;
        height: 90rpx;
        background-color: #f5f5f5;
        border-radius: 45rpx;
        padding: 0 40rpx;
        font-size: 28rpx;
        color: #333;
      }
    }
  }

  .submit-btn {
    width: 100%;
    height: 90rpx;
    background-color: #2588FF;
    color: #fff;
    font-size: 32rpx;
    border-radius: 45rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 60rpx;
  }

  .login-link {
    text-align: center;
    font-size: 28rpx;
    color: #2588FF;
    margin-top: 30rpx;
  }
}
</style>