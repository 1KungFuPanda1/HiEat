<template>
  <view class="help-container">
    <view class="help-header">
      <view class="tab-bar">
        <view class="tab-item" :class="{ active: activeTab === 0 }" @click="switchTab(0)">常见问题</view>
        <view class="tab-item" :class="{ active: activeTab === 1 }" @click="switchTab(1)">意见反馈</view>
      </view>
    </view>

    <!-- 常见问题 -->
    <view class="faq-section" v-if="activeTab === 0">
      <view class="faq-list">
        <view class="faq-item" v-for="(item, index) in faqList" :key="index" @click="toggleFaq(index)">
          <view class="faq-header">
            <text class="faq-title">{{ item.title }}</text>
            <text class="faq-icon" :class="{ expanded: item.expanded }">{{ item.expanded ? '−' : '+' }}</text>
          </view>
          <view class="faq-content" v-if="item.expanded">
            <text>{{ item.content }}</text>
          </view>
        </view>
      </view>

      <view class="contact-info">
        <text class="contact-title">联系客服</text>
        <text class="contact-desc">如果您有其他问题，可以通过以下方式联系我们</text>
        <view class="contact-item">
          <text class="contact-label">客服电话：</text>
          <text class="contact-value" @click="callService">400-959-8521</text>
        </view>
        <view class="contact-item">
          <text class="contact-label">客服邮箱：</text>
          <text class="contact-value">service@foodtime.com</text>
        </view>
        <view class="contact-item">
          <text class="contact-label">工作时间：</text>
          <text class="contact-value">9:00-20:00（节假日不休）</text>
        </view>
      </view>
    </view>

    <!-- 意见反馈 -->
    <view class="feedback-section" v-if="activeTab === 1">
      <view class="feedback-form">
        <!-- 问题类型 -->
        <view class="form-item">
          <text class="item-label">问题类型</text>
          <view class="type-selector">
            <view class="type-item" v-for="(type, index) in feedbackTypes" :key="index"
              :class="{ active: selectedType === index }" @click="selectType(index)">{{ type }}</view>
          </view>
        </view>

        <!-- 问题描述 -->
        <view class="form-item">
          <text class="item-label">问题描述</text>
          <textarea class="feedback-textarea" v-model="feedbackContent" placeholder="请详细描述您遇到的问题，以便我们更好地为您解决"
            maxlength="500"></textarea>
          <text class="word-count">{{ feedbackContent.length }}/500</text>
        </view>

        <!-- 上传图片 -->
        <view class="form-item">
          <text class="item-label">上传图片(选填)</text>
          <view class="upload-area">
            <view class="image-list">
              <view class="image-item" v-for="(url, index) in imageList" :key="index">
                <image :src="url" mode="aspectFill"></image>
                <text class="delete-icon" @click="deleteImage(index)">×</text>
              </view>
              <view class="upload-btn" @click="chooseImage" v-if="imageList.length < 3">
                <text class="iconfont icon-camera"></text>
                <text>添加图片</text>
              </view>
            </view>
            <text class="upload-tip">最多上传3张图片</text>
          </view>
        </view>

        <!-- 联系方式 -->
        <view class="form-item">
          <text class="item-label">联系方式</text>
          <input class="feedback-input" v-model="contactInfo" placeholder="请留下您的手机号或邮箱，方便我们联系您" />
        </view>
      </view>

      <!-- 提交按钮 -->
      <button class="submit-btn" @click="submitFeedback" :disabled="!canSubmit">提交反馈</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';

// 当前激活的标签页
const activeTab = ref(0);

// 常见问题列表
const faqList = ref([
  {
    title: '如何修改收货地址？',
    content: '您可以在"我的 > 我的地址"中添加、编辑或删除收货地址。在下单时也可以选择或新增收货地址。',
    expanded: false
  },
  {
    title: '如何取消订单？',
    content: '在订单页面中，如果订单状态为"待支付"，您可以点击"取消订单"按钮进行取消。如果订单已经被商家接单，则无法取消订单，您可以联系客服寻求帮助。',
    expanded: false
  },
  {
    title: '订单超时未送达怎么办？',
    content: '如果您的订单超过预计送达时间还未送达，您可以在订单详情页点击"催单"按钮，或拨打我们的客服电话进行咨询。',
    expanded: false
  },
  {
    title: '如何申请退款？',
    content: '如果您需要申请退款，可以在订单详情页联系客服说明情况，我们的客服会协助您处理退款事宜。',
    expanded: false
  },
  {
    title: '如何评价订单？',
    content: '订单完成后，您可以在订单详情页点击"去评价"按钮，对商家和餐品进行评价。您的评价对其他用户有很大帮助。',
    expanded: false
  }
]);

// 意见反馈相关变量
const feedbackTypes = ['商品相关', '配送问题', '应用功能', '账户问题', '其他'];
const selectedType = ref(-1);
const feedbackContent = ref('');
const contactInfo = ref('');
const imageList = ref([]);

// 切换标签页
const switchTab = (index) => {
  activeTab.value = index;
};

// 切换FAQ展开/收起状态
const toggleFaq = (index) => {
  faqList.value[index].expanded = !faqList.value[index].expanded;
};

// 拨打客服电话
const callService = () => {
  uni.makePhoneCall({
    phoneNumber: '4009598521'
  });
};

// 选择问题类型
const selectType = (index) => {
  selectedType.value = index;
};

// 选择图片
const chooseImage = () => {
  if (imageList.value.length >= 3) {
    uni.showToast({
      title: '最多上传3张图片',
      icon: 'none'
    });
    return;
  }

  uni.chooseImage({
    count: 3 - imageList.value.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      // 模拟上传图片，实际项目中应该调用上传接口
      const tempFilePaths = res.tempFilePaths;
      imageList.value = [...imageList.value, ...tempFilePaths].slice(0, 3);
    }
  });
};

// 删除图片
const deleteImage = (index) => {
  imageList.value.splice(index, 1);
};

// 是否可以提交
const canSubmit = computed(() => {
  return selectedType.value !== -1 && feedbackContent.value.trim().length > 0;
});

// 提交反馈
const submitFeedback = () => {
  if (!canSubmit.value) {
    uni.showToast({
      title: '请选择问题类型并填写问题描述',
      icon: 'none'
    });
    return;
  }

  uni.showLoading({
    title: '提交中...'
  });

  // 模拟提交，实际项目中应该调用API
  setTimeout(() => {
    uni.hideLoading();

    uni.showToast({
      title: '反馈提交成功',
      icon: 'success'
    });

    // 重置表单
    selectedType.value = -1;
    feedbackContent.value = '';
    contactInfo.value = '';
    imageList.value = [];

    // 切换到常见问题标签
    activeTab.value = 0;
  }, 1000);
};
</script>

<style lang="scss">
.help-container {
  min-height: 100vh;
  background-color: #f5f5f5;

  .help-header {
    background-color: #fff;
    padding: 20rpx 0;

    .tab-bar {
      display: flex;
      justify-content: center;
      border-bottom: 1rpx solid #eee;

      .tab-item {
        padding: 20rpx 40rpx;
        font-size: 28rpx;
        color: #666;
        position: relative;

        &.active {
          color: #2588FF;
          font-weight: 500;

          &:after {
            content: '';
            position: absolute;
            bottom: -2rpx;
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
  }

  .faq-section {
    padding: 20rpx;

    .faq-list {
      background-color: #fff;
      border-radius: 12rpx;
      overflow: hidden;
      margin-bottom: 20rpx;

      .faq-item {
        .faq-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 30rpx;
          border-bottom: 1rpx solid #f5f5f5;

          .faq-title {
            font-size: 28rpx;
            color: #333;
            flex: 1;
          }

          .faq-icon {
            width: 40rpx;
            height: 40rpx;
            line-height: 36rpx;
            text-align: center;
            font-size: 32rpx;
            color: #999;

            &.expanded {
              color: #2588FF;
            }
          }
        }

        .faq-content {
          padding: 20rpx 30rpx 30rpx;
          background-color: #f9f9f9;

          text {
            font-size: 26rpx;
            color: #666;
            line-height: 1.6;
          }
        }
      }
    }

    .contact-info {
      background-color: #fff;
      border-radius: 12rpx;
      padding: 30rpx;

      .contact-title {
        font-size: 30rpx;
        color: #333;
        font-weight: 500;
        margin-bottom: 16rpx;
        display: block;
      }

      .contact-desc {
        font-size: 26rpx;
        color: #666;
        margin-bottom: 30rpx;
        display: block;
      }

      .contact-item {
        display: flex;
        margin-bottom: 16rpx;

        .contact-label {
          font-size: 26rpx;
          color: #333;
          min-width: 150rpx;
        }

        .contact-value {
          font-size: 26rpx;
          color: #666;

          &:active {
            color: #2588FF;
          }
        }
      }
    }
  }

  .feedback-section {
    padding: 20rpx;

    .feedback-form {
      background-color: #fff;
      border-radius: 12rpx;
      padding: 30rpx;

      .form-item {
        margin-bottom: 30rpx;

        .item-label {
          font-size: 28rpx;
          color: #333;
          margin-bottom: 20rpx;
          display: block;
        }

        .type-selector {
          display: flex;
          flex-wrap: wrap;

          .type-item {
            padding: 10rpx 30rpx;
            background-color: #f5f5f5;
            border-radius: 30rpx;
            font-size: 26rpx;
            color: #666;
            margin-right: 20rpx;
            margin-bottom: 20rpx;

            &.active {
              background-color: rgba(37, 136, 255, 0.1);
              color: #2588FF;
            }
          }
        }

        .feedback-textarea {
          width: 100%;
          height: 240rpx;
          border: 1rpx solid #eee;
          border-radius: 8rpx;
          padding: 20rpx;
          font-size: 28rpx;
        }

        .feedback-input {
          width: 100%;
          height: 80rpx;
          border: 1rpx solid #eee;
          border-radius: 8rpx;
          padding: 0 20rpx;
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
              border: 1rpx dashed #ddd;
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
}
</style>