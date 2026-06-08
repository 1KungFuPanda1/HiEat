<template>
  <view class="forgot-password-container">
    <view class="form-group">
      <view class="input-item">
        <text class="label">手机号</text>
        <input type="number" v-model="formData.phone" placeholder="请输入手机号" maxlength="11" />
      </view>
      <view class="input-item">
        <text class="label">新密码</text>
        <input type="password" v-model="formData.newPassword" placeholder="请输入新密码" />
      </view>
      <!-- 再次输入密码 -->
      <view class="input-item">
        <text class="label">再次输入密码</text>
        <input type="password" v-model="formData.confirmPassword" placeholder="请再次输入密码" />
      </view>
    </view>

    <view class="submit-btn" @click="handleResetPassword">重置密码</view>

    <view class="login-link" @click="goToLogin">
      返回登录
    </view>
  </view>
</template>

<script>
import { userApi } from '@/api/user'

export default {
  data() {
    return {
      formData: {
        phone: '',
        newPassword: '',
        confirmPassword: ''
      }
    }
  },
  methods: {
    // 重置密码
    async handleResetPassword() {
      // 表单验证
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

      if (!this.formData.newPassword) {
        uni.showToast({
          title: '请输入新密码',
          icon: 'none'
        })
        return
      }

      if (!this.formData.confirmPassword) {
        uni.showToast({
          title: '请再次输入密码',
          icon: 'none'
        })
        return
      }

      if (this.formData.newPassword !== this.formData.confirmPassword) {
        uni.showToast({
          title: '两次输入密码不一致',
          icon: 'none'
        })
        return
      }

      try {
        uni.showLoading({
          title: '重置中...'
        })

        const res = await userApi.resetPassword(this.formData)

        uni.hideLoading()

        if (res.code === 1) {
          uni.showToast({
            title: '密码重置成功',
            icon: 'success'
          })

          // 延迟返回登录页
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          uni.showToast({
            title: res.msg || '重置失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('重置密码失败:', error)
        uni.showToast({
          title: '网络异常，请重试',
          icon: 'none'
        })
      }
    },

    // 返回登录页
    goToLogin() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss">
.forgot-password-container {
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