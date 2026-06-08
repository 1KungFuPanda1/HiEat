<template>
  <view class="login-container">
    <!-- 登录方式切换 -->
    <view class="login-tabs">
      <view class="tab-item" :class="{ active: loginType === 'password' }" @click="loginType = 'password'">密码登录</view>
      <view class="tab-item" :class="{ active: loginType === 'sms' }" @click="loginType = 'sms'">验证码登录</view>
    </view>

    <!-- 登录表单区域 -->
    <view class="login-form">
      <!-- 密码登录表单 -->
      <view v-if="loginType === 'password'">
        <view class="form-item">
          <text class="iconfont icon-user"></text>
          <input type="text" v-model="passwordForm.phone" placeholder="请输入手机号" maxlength="11"
            placeholder-class="placeholder" />
        </view>
        <view class="form-item">
          <text class="iconfont icon-lock"></text>
          <input :type="showPassword ? 'text' : 'password'" v-model="passwordForm.password" placeholder="请输入密码"
            placeholder-class="placeholder" />
          <text class="iconfont" :class="showPassword ? 'icon-eye-open' : 'icon-eye-close'"
            @click="showPassword = !showPassword"></text>
        </view>
        <button class="login-btn" @click="handlePasswordLogin">登录</button>
        <view class="form-footer">
          <text class="link" @click="goToForgetPassword">忘记密码?</text>
          <text class="link" @click="goToRegister">注册账号</text>
        </view>
      </view>

      <!-- 验证码登录表单 -->
      <view v-if="loginType === 'sms'">
        <view class="form-item">
          <text class="iconfont icon-user"></text>
          <input type="text" v-model="smsForm.phone" placeholder="请输入手机号" maxlength="11"
            placeholder-class="placeholder" />
        </view>
        <view class="form-item">
          <text class="iconfont icon-verify"></text>
          <input type="text" v-model="smsForm.code" placeholder="请输入验证码" maxlength="6"
            placeholder-class="placeholder" />
          <view class="code-btn" @click="sendSmsCode" :class="{ disabled: counting }">
            {{ codeText }}
          </view>
        </view>
        <button class="login-btn" @click="handleSmsLogin">登录</button>
      </view>
    </view>


    <!-- 底部返回按钮 -->
    <view class="login-footer">
      <button class="back-btn" @click="goBack">暂不登录，返回浏览</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { userApi } from '@/api/index';
import { useUserStore } from '@/store/user';

// 获取用户状态管理 store
const userStore = useUserStore();

// 登录类型：password-密码登录，sms-验证码登录
const loginType = ref('password');
const showPassword = ref(false);

// 密码登录表单
const passwordForm = reactive({
  phone: '',
  password: ''
});

// 验证码登录表单
const smsForm = reactive({
  phone: '',
  code: ''
});

// 验证码倒计时
const counting = ref(false);
const countdown = ref(60);
const codeText = ref('获取验证码');

// 密码登录
const handlePasswordLogin = () => {
  // 表单验证
  if (!passwordForm.phone || !/^1\d{10}$/.test(passwordForm.phone)) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    });
    return;
  }

  if (!passwordForm.password) {
    uni.showToast({
      title: '请输入密码',
      icon: 'none'
    });
    return;
  }

  // 显示加载中
  uni.showLoading({ title: '登录中...' });

  // 调用登录接口
  userApi.login({
    phone: passwordForm.phone,
    password: passwordForm.password
  }).then(res => {
    // 登录成功，使用 Pinia Store 保存用户信息
    const userData = res.data;
    userData.phone = passwordForm.phone; // 添加手机号信息

    // 使用 store 的 loginSuccess 方法处理登录成功
    userStore.loginSuccess(userData);

    // 提示登录成功
    uni.showToast({
      title: '登录成功',
      icon: 'success',
      duration: 1500
    });
  }).catch(err => {
    uni.showToast({
      title: err.msg || '登录失败，请重试',
      icon: 'none'
    });
  }).finally(() => {
    uni.hideLoading();
  });
};

// 发送验证码
const sendSmsCode = () => {
  // 校验手机号
  if (!smsForm.phone || !/^1\d{10}$/.test(smsForm.phone)) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    });
    return;
  }

  // 防止重复点击
  if (counting.value) return;

  counting.value = true;
  countdown.value = 60;
  codeText.value = countdown.value + 's';

  const timer = setInterval(() => {
    countdown.value--;
    codeText.value = countdown.value + 's';
    if (countdown.value <= 0) {
      clearInterval(timer);
      counting.value = false;
      codeText.value = '重新获取';
    }
  }, 1000);

  // 调用发送验证码接口
  userApi.sendCode({ phone: smsForm.phone }).then(() => {
    uni.showToast({
      title: '验证码已发送，请查收QQ邮箱',
      icon: 'success'
    });
  }).catch(err => {
    uni.showToast({
      title: err.msg || '发送失败，请重试',
      icon: 'none'
    });
    // 发送失败，重置倒计时
    clearInterval(timer);
    counting.value = false;
    codeText.value = '获取验证码';
  });
};

// 验证码登录
const handleSmsLogin = () => {
  // 表单验证
  if (!smsForm.phone || !/^1\d{10}$/.test(smsForm.phone)) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    });
    return;
  }

  if (!smsForm.code || smsForm.code.length !== 6) {
    uni.showToast({
      title: '请输入6位验证码',
      icon: 'none'
    });
    return;
  }

  // 显示加载中
  uni.showLoading({ title: '登录中...' });

  // 调用验证码登录接口
  userApi.smsLogin({
    phone: smsForm.phone,
    code: smsForm.code
  }).then(res => {
    // 登录成功，使用 Pinia Store 保存用户信息
    const userData = res.data;
    userData.phone = smsForm.phone; // 添加手机号信息

    // 使用 store 的 loginSuccess 方法处理登录成功
    userStore.loginSuccess(userData);

    // 提示登录成功
    uni.showToast({
      title: '登录成功',
      icon: 'success',
      duration: 1500
    });
  }).catch(err => {
    uni.showToast({
      title: err.msg || '登录失败，请重试',
      icon: 'none'
    });
  }).finally(() => {
    uni.hideLoading();
  });
};

// 处理微信登录
const handleWechatLogin = (e) => {
  if (e.detail.userInfo) {
    // 用户同意授权
    const userInfo = e.detail.userInfo;

    // 获取微信登录code
    uni.login({
      provider: 'weixin',
      success: (loginRes) => {
        if (loginRes.code) {
          // 显示加载中
          uni.showLoading({ title: '登录中...' });

          // 调用微信登录接口
          userApi.wechatLogin({
            code: loginRes.code,
            userInfo: userInfo
          }).then(res => {
            // 登录成功，使用 Pinia Store 保存用户信息
            const userData = res.data;

            // 使用 store 的 loginSuccess 方法处理登录成功
            userStore.loginSuccess(userData);

            // 提示登录成功
            uni.showToast({
              title: '登录成功',
              icon: 'success',
              duration: 1500
            });
          }).catch(err => {
            uni.showToast({
              title: err.msg || '登录失败，请重试',
              icon: 'none'
            });
          }).finally(() => {
            uni.hideLoading();
          });
        } else {
          uni.showToast({
            title: '登录失败，请重试',
            icon: 'none'
          });
        }
      },
      fail: () => {
        uni.showToast({
          title: '登录失败，请重试',
          icon: 'none'
        });
      }
    });
  } else {
    // 用户拒绝授权
    uni.showToast({
      title: '您拒绝了授权，无法登录',
      icon: 'none'
    });
  }
};

// 跳转到忘记密码页面
const goToForgetPassword = () => {
  uni.navigateTo({
    url: '/pages/user/forgot-password'
  });
};

// 跳转到注册页面
const goToRegister = () => {
  uni.navigateTo({
    url: '/pages/user/register'
  });
};

// 跳转到用户协议
const goToUserAgreement = () => {
  uni.navigateTo({
    url: '/pages/agreement/user'
  });
};

// 跳转到隐私政策
const goToPrivacyPolicy = () => {
  uni.navigateTo({
    url: '/pages/agreement/privacy'
  });
};

// 返回上一页
const goBack = () => {
  uni.navigateBack();
};
</script>

<style lang="scss">
.login-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 30rpx 50rpx 20rpx;
  background-color: #fff;
  box-sizing: border-box;

  .login-tabs {
    display: flex;
    justify-content: center;
    margin-bottom: 30rpx;
    margin-top: 20rpx;

    .tab-item {
      position: relative;
      padding: 0 20rpx 8rpx;
      /* 减少内边距 */
      margin: 0 30rpx;
      font-size: 30rpx;
      /* 减小字体 */
      color: #666;

      &.active {
        color: #2588FF;
        font-weight: 500;

        &::after {
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

  .login-form {
    width: 100%;
    flex: 0 0 auto;

    .form-item {
      display: flex;
      align-items: center;
      height: 90rpx;
      /* 减少高度 */
      border-bottom: 1rpx solid #eee;
      margin-bottom: 15rpx;
      /* 减少底部外边距 */

      .iconfont {
        font-size: 36rpx;
        /* 减小图标 */
        color: #999;
        margin-right: 20rpx;
      }

      input {
        flex: 1;
        height: 100%;
        font-size: 28rpx;
        /* 减小字体 */
      }

      .placeholder {
        color: #ccc;
        font-size: 28rpx;
        /* 减小字体 */
      }

      .code-btn {
        padding: 8rpx 16rpx;
        /* 减少内边距 */
        font-size: 24rpx;
        /* 减小字体 */
        color: #2588FF;
        border-radius: 4rpx;

        &.disabled {
          color: #999;
        }
      }
    }

    .login-btn {
      width: 100%;
      height: 80rpx;
      /* 减少高度 */
      line-height: 80rpx;
      /* 减少行高 */
      background-color: #2588FF;
      color: #fff;
      font-size: 30rpx;
      /* 减小字体 */
      border-radius: 40rpx;
      /* 减少圆角 */
      margin: 30rpx 0 15rpx;
      /* 减少外边距 */
    }

    .form-footer {
      display: flex;
      justify-content: space-between;
      font-size: 24rpx;
      /* 减小字体 */
      color: #999;
      margin-bottom: 20rpx;
      /* 减少底部外边距 */

      .link {
        color: #2588FF;
      }
    }
  }

  .other-login {
    margin-top: 10rpx;
    /* 减少顶部外边距 */

    .divider {
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 20rpx;
      /* 减少底部外边距 */

      .line {
        width: 80rpx;
        /* 减少宽度 */
        height: 1rpx;
        background-color: #eee;
      }

      text {
        color: #999;
        font-size: 22rpx;
        /* 减小字体 */
        margin: 0 15rpx;
        /* 减少外边距 */
      }
    }

    .other-login-btns {
      display: flex;
      justify-content: center;

      .wechat-login-btn {
        width: 80rpx;
        /* 减少尺寸 */
        height: 80rpx;
        /* 减少尺寸 */
        border-radius: 50%;
        background-color: #07C160;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0;

        .iconfont {
          font-size: 44rpx;
          /* 减小图标 */
          color: #fff;
        }

        &::after {
          border: none;
        }
      }
    }
  }

  .login-footer {
    margin-top: 20rpx;
    /* 改为固定边距，不再使用auto */
    padding-bottom: 20rpx;
    text-align: center;

    .back-btn {
      background: none;
      border: none;
      color: #666;
      font-size: 24rpx;
      /* 减小字体 */
      padding: 10rpx;
      /* 减少内边距 */

      &::after {
        border: none;
      }
    }
  }

  .tips {
    text-align: center;
    font-size: 22rpx;
    /* 减小字体 */
    color: #999;
    margin-top: 15rpx;
    /* 减少顶部外边距 */

    .link {
      color: #2588FF;
    }
  }
}
</style>