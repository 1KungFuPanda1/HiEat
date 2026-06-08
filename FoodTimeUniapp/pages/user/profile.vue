<template>
  <view class="profile-container">

    <view class="profile-list">
      <!-- 头像 -->
      <view class="profile-item" @click="changeAvatar">
        <text class="item-label">头像</text>
        <view class="item-right">
          <image class="avatar" :src="userInfo.avatar || defaultAvatar" mode="aspectFill"></image>
          <text class="iconfont icon-right"></text>
        </view>
      </view>

      <!-- 昵称 -->
      <view class="profile-item" @click="showNicknameModal">
        <text class="item-label">昵称</text>
        <view class="item-right">
          <text class="item-value">{{ userInfo.username || '未设置' }}</text>
          <text class="iconfont icon-right"></text>
        </view>
      </view>

      <!-- 性别 -->
      <view class="profile-item" @click="showGenderModal">
        <text class="item-label">性别</text>
        <view class="item-right">
          <text class="item-value">{{ formatGender(userInfo.sex) || '未设置' }}</text>
          <text class="iconfont icon-right"></text>
        </view>
      </view>

      <!-- 手机号 -->
      <view class="profile-item">
        <text class="item-label">手机号</text>
        <view class="item-right">
          <text class="item-value">{{ formatPhone(userInfo.phone) || '未绑定' }}</text>
          <text class="item-tag" v-if="userInfo.phone" @click="showPhoneModal">修改</text>
          <text class="item-tag" v-else @click="showPhoneModal">绑定</text>
        </view>
      </view>
      
      <!-- 密码修改 -->
      <view class="profile-item" @click="showPasswordModal">
        <text class="item-label">修改密码</text>
        <view class="item-right">
          <text class="item-value">******</text>
          <text class="iconfont icon-right"></text>
        </view>
      </view>
    </view>

    <!-- 昵称修改弹窗 -->
    <uni-popup ref="nicknamePopup" type="dialog">
      <uni-popup-dialog title="修改昵称"  :value="nickname" placeholder="请输入昵称" :beforeClose="true"
        @confirm="confirmNickname" @close="closeNickname"></uni-popup-dialog>
    </uni-popup>

    <!-- 性别选择弹窗 -->
    <uni-popup ref="genderPopup" type="bottom">
      <view class="gender-popup">
        <view class="popup-title">选择性别</view>
        <view class="gender-options">
          <view class="gender-option" :class="{ active: tempGender === '男' }" @click="selectGender('男')">男</view>
          <view class="gender-option" :class="{ active: tempGender === '女' }" @click="selectGender('女')">女</view>
        </view>
        <view class="popup-actions">
          <button class="cancel-btn" @click="closeGender">取消</button>
          <button class="confirm-btn" @click="confirmGender">确定</button>
        </view>
      </view>
    </uni-popup>

    <!-- 手机号修改弹窗 -->
    <uni-popup ref="phonePopup" type="dialog">
      <uni-popup-dialog title="绑定手机号" :value="phone" placeholder="请输入手机号" :beforeClose="true" @confirm="confirmPhone"
        @close="closePhone"></uni-popup-dialog>
    </uni-popup>
    
    <!-- 密码修改弹窗 -->
    <uni-popup ref="passwordPopup" type="bottom">
      <view class="password-popup">
        <view class="popup-title">修改密码</view>
        <view class="password-form">
          <view class="form-item">
            <input type="password" v-model="passwordForm.newPassword" placeholder="请输入新密码" />
          </view>
          <view class="form-item">
            <input type="password" v-model="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
          </view>
        </view>
        <view class="popup-actions">
          <button class="cancel-btn" @click="closePassword">取消</button>
          <button class="confirm-btn" @click="confirmPassword">确定</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useUserStore } from '@/store/user';
import { commonApi } from '@/api/index';
import { userApi } from '@/api/index';

// 使用用户状态管理
const userStore = useUserStore();

// 默认头像路径
const defaultAvatar = '/static/image/user/default.jpg';

// 用户信息
const userInfo = reactive({
  id: null,
  avatar: '',
  username: '',
  phone: '',
  sex: ''
});

// 修改信息相关变量
const nicknamePopup = ref(null);
const genderPopup = ref(null);
const phonePopup = ref(null);
const passwordPopup = ref(null);
const nickname = ref('');
const phone = ref('');
const tempGender = ref('');

// 密码表单
const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
});

// 页面加载时获取用户信息
onMounted(() => {
  loadUserInfo();
});

/**
 * 加载用户信息
 */
const loadUserInfo = async () => {
  if (userStore.isLoggedIn) {
    try {
      const res = await userApi.getUserInfo();
      if (res && res.code === 1 && res.data) {
        const userData = res.data;
        userInfo.id = userData.id;
        userInfo.avatar = userData.avatar || defaultAvatar;
        userInfo.username = userData.username || '';
        userInfo.phone = userData.phone || '';
        userInfo.sex = userData.sex || '';

        nickname.value = userInfo.username;
        phone.value = userInfo.phone;
        tempGender.value = userInfo.sex;
      } else {
        uni.showToast({
          title: '获取用户信息失败',
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('获取用户信息异常:', error);
      uni.showToast({
        title: '网络异常，请稍后重试',
        icon: 'none'
      });
    }
  } else {
    // 未登录，返回上一页
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    });
    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  }
};

/**
 * 格式化手机号显示
 * @param {string} phoneNumber - 手机号
 * @returns {string} 格式化后的手机号
 */
const formatPhone = (phoneNumber) => {
  if (!phoneNumber) return '';
  return phoneNumber.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
};

/**
 * 格式化性别显示
 * @param {string} gender - 性别
 * @returns {string} 格式化后的性别
 */
const formatGender = (gender) => {
  if (!gender) return '';
  return gender;
};

/**
 * 更换头像
 */
const changeAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      uploadAvatar(res.tempFilePaths[0]);
    }
  });
};

/**
 * 上传头像
 * @param {string} filePath - 图片路径
 */
const uploadAvatar = async (filePath) => {
  uni.showLoading({
    title: '上传中...'
  });

  try {
    // 使用commonApi上传头像
    const response = await commonApi.uploadFile(filePath);

    if (response.code === 1 && response.data) {
      // 更新头像
      const avatarUrl = response.data;

      // 更新用户信息
      await updateUserProfile({
        avatar: avatarUrl
      });
    }
  } catch (error) {
    console.error('上传头像失败:', error);
    // 错误提示已在commonApi中处理
  } finally {
    uni.hideLoading();
  }
};

/**
 * 显示昵称修改弹窗
 */
const showNicknameModal = () => {
  nickname.value = userInfo.username;
  nicknamePopup.value.open();
};

/**
 * 关闭昵称修改弹窗
 */
const closeNickname = () => {
  nicknamePopup.value.close();
};

/**
 * 确认修改昵称
 * @param {string} val - 昵称
 */
const confirmNickname = (val) => {
  if (!val.trim()) {
    uni.showToast({
      title: '昵称不能为空',
      icon: 'none'
    });
    return;
  }

  // 更新用户信息
  updateUserProfile({
    username: val.trim()
  });

  nicknamePopup.value.close();
};

/**
 * 显示性别选择弹窗
 */
const showGenderModal = () => {
  tempGender.value = userInfo.sex || '男';
  genderPopup.value.open();
};

/**
 * 选择性别
 * @param {string} gender - 性别
 */
const selectGender = (gender) => {
  tempGender.value = gender;
};

/**
 * 关闭性别选择弹窗
 */
const closeGender = () => {
  genderPopup.value.close();
};

/**
 * 确认修改性别
 */
const confirmGender = () => {
  if (tempGender.value !== userInfo.sex) {
    // 更新用户信息
    updateUserProfile({
      sex: tempGender.value
    });
  }

  genderPopup.value.close();
};

/**
 * 显示手机号修改弹窗
 */
const showPhoneModal = () => {
  phone.value = '';
  phonePopup.value.open();
};

/**
 * 关闭手机号修改弹窗
 */
const closePhone = () => {
  phonePopup.value.close();
};

/**
 * 确认修改手机号
 * @param {string} val - 手机号
 */
const confirmPhone = (val) => {
  if (!val.trim()) {
    uni.showToast({
      title: '手机号不能为空',
      icon: 'none'
    });
    return;
  }

  // 验证手机号格式
  const phoneRegex = /^1[3-9]\d{9}$/;
  if (!phoneRegex.test(val)) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    });
    return;
  }

  // 更新用户信息
  updateUserProfile({
    phone: val
  });

  phonePopup.value.close();
};

/**
 * 显示密码修改弹窗
 */
const showPasswordModal = () => {
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  passwordPopup.value.open();// 打开弹窗
};

/**
 * 关闭密码修改弹窗
 */
const closePassword = () => {
  passwordPopup.value.close();
};

/**
 * 确认修改密码
 */
const confirmPassword = () => {
  // 验证密码
  if (!passwordForm.newPassword) {
    uni.showToast({
      title: '请输入新密码',
      icon: 'none'
    });
    return;
  }
  
  if (passwordForm.newPassword.length < 6) {
    uni.showToast({
      title: '密码长度不能少于6位',
      icon: 'none'
    });
    return;
  }
  
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    uni.showToast({
      title: '两次输入的密码不一致',
      icon: 'none'
    });
    return;
  }
  
  // 更新用户信息
  updateUserProfile({
    newPassword: passwordForm.newPassword,
    confirmPassword: passwordForm.confirmPassword
  });
  
  passwordPopup.value.close();
};

/**
 * 更新用户信息
 * @param {Object} data - 要更新的用户信息
 */
const updateUserProfile = async (data) => {
  try {
    uni.showLoading({
      title: '更新中...'
    });

    // 准备更新数据
    const updateData = {
      id: userInfo.id,
      ...data
    };

    const res = await userApi.updateUserInfo(updateData);

    if (res && res.code === 1) {
      // 更新本地信息（不包括密码相关字段）
      const { newPassword, confirmPassword, ...updateUserInfo } = data;
      Object.assign(userInfo, updateUserInfo);

      // 更新用户存储信息
      const { newPassword: storeNewPassword, confirmPassword: storeConfirmPassword, ...storeUpdateInfo } = data;
      const newUserInfo = {
        ...userStore.userInfo,
        ...storeUpdateInfo
      };
      userStore.setUserInfo(newUserInfo);

      uni.showToast({
        title: res?.data || '更新成功',
        icon: 'success'
      });
    } else {
      uni.showToast({
        title: res?.msg || '更新失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('更新用户信息异常:', error);
    uni.showToast({
      title: '网络异常，请稍后重试',
      icon: 'none'
    });
  } finally {
    uni.hideLoading();
  }
};
</script>

<style lang="scss">
.profile-container {
  min-height: 100vh;
  background-color: #f5f5f5;

  .profile-list {
    margin-top: 20rpx;

    .profile-item {
      background-color: #fff;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 30rpx;
      border-bottom: 1rpx solid #f5f5f5;

      .item-label {
        font-size: 28rpx;
        color: #333;
      }

      .item-right {
        display: flex;
        align-items: center;

        .avatar {
          width: 100rpx;
          height: 100rpx;
          border-radius: 50%;
          margin-right: 16rpx;
        }

        .item-value {
          font-size: 28rpx;
          color: #666;
          margin-right: 16rpx;
        }

        .item-tag {
          font-size: 24rpx;
          color: #2588FF;
        }

        .icon-right {
          font-size: 24rpx;
          color: #ccc;
        }
      }
    }
  }

  .gender-popup {
    background-color: #fff;
    border-radius: 20rpx 20rpx 0 0;
    padding: 30rpx;

    .popup-title {
      font-size: 32rpx;
      font-weight: 500;
      text-align: center;
      margin-bottom: 30rpx;
    }

    .gender-options {
      display: flex;
      justify-content: space-around;
      margin-bottom: 40rpx;

      .gender-option {
        width: 40%;
        height: 80rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 2rpx solid #eee;
        border-radius: 10rpx;
        font-size: 28rpx;
        color: #666;

        &.active {
          color: #2588FF;
          border-color: #2588FF;
          background-color: rgba(37, 136, 255, 0.05);
        }
      }
    }

    .popup-actions {
      display: flex;
      justify-content: space-between;

      button {
        width: 45%;
        height: 80rpx;
        line-height: 80rpx;
        font-size: 28rpx;
        border-radius: 40rpx;
      }

      .cancel-btn {
        background-color: #f5f5f5;
        color: #666;
      }

      .confirm-btn {
        background-color: #2588FF;
        color: #fff;
      }
    }
  }
  
  .password-popup {
    background-color: #fff;
    border-radius: 20rpx 20rpx 0 0;
    padding: 30rpx;
    
    .popup-title {
      font-size: 32rpx;
      font-weight: 500;
      text-align: center;
      margin-bottom: 30rpx;
    }
    
    .password-form {
      margin-bottom: 40rpx;
      
      .form-item {
        margin-bottom: 20rpx;
        
        input {
          width: 100%;
          height: 80rpx;
          border: 2rpx solid #eee;
          border-radius: 10rpx;
          padding: 0 20rpx;
          box-sizing: border-box;
        }
      }
    }
    
    .popup-actions {
      display: flex;
      justify-content: space-between;
      
      button {
        width: 45%;
        height: 80rpx;
        line-height: 80rpx;
        font-size: 28rpx;
        border-radius: 40rpx;
      }
      
      .cancel-btn {
        background-color: #f5f5f5;
        color: #666;
      }
      
      .confirm-btn {
        background-color: #2588FF;
        color: #fff;
      }
    }
  }
}
</style>