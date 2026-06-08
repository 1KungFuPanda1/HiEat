<template>
  <view class="setmeal-panel" :class="{ 'panel-show': show }">
    <!-- 遮罩层 -->
    <view class="panel-mask" @click="close" @touchmove.stop.prevent></view>

    <!-- 内容区域 -->
    <view class="panel-content" @touchmove.stop.prevent>
      <!-- 头部 -->
      <view class="panel-header">
        <view class="panel-title">{{ setmeal.name }}</view>
        <view class="close-btn" @click="close">
          <uni-icons type="closeempty" size="20" color="#999"></uni-icons>
        </view>
      </view>

      <!-- 套餐图片 -->
      <view class="setmeal-image" v-if="setmeal.image">
        <image :src="setmeal.image" mode="aspectFill"></image>
      </view>

      <!-- 价格信息 -->
      <view class="price-info">
        <view class="price-row">
          <text class="price-label">现价:</text>
          <text class="price-value">¥{{ formatPrice(setmeal.price) }}</text>
        </view>
        <view class="price-row" v-if="hasDiscount">
          <text class="price-label">原价:</text>
          <text class="price-original">¥{{ formatPrice(setmeal.originalPrice) }}</text>
          <text class="discount-tag">{{ discount }}折</text>
        </view>
      </view>

      <!-- 描述 -->
      <view class="setmeal-desc" v-if="setmeal.description">
        {{ setmeal.description }}
      </view>

      <!-- 套餐内容 -->
      <view class="setmeal-dishes" v-if="hasDishes">
        <view class="section-title">套餐内容</view>
        <view class="dish-list">
          <view class="dish-item" v-for="(dish, index) in dishes" :key="index">
            <view class="dish-info-row">
              <view class="dish-name">{{ dish.name }}</view>
              <view class="dish-copies" v-if="dish.copies && dish.copies > 1">x{{ dish.copies }}</view>
            </view>
            <view class="dish-price">¥{{ formatPrice(dish.price) }}</view>
          </view>
        </view>

        <!-- 套餐总价值 -->
        <view class="total-value" v-if="hasSavings">
          <text>套餐总价值：¥{{ formatPrice(totalValue) }}</text>
          <text class="save-text">省¥{{ formatPrice(savedAmount) }}</text>
        </view>
      </view>

      <!-- 底部操作区 -->
      <view class="panel-footer">
        <view class="action-btn cancel" @click="close">取消</view>
        <view class="action-btn confirm" @click="addToCart">加入购物车</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { shoppingCartApi } from '@/api/index';

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  setmeal: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['close', 'confirm']);

// 格式化价格
const formatPrice = (price) => {
  if (!price) return '0.00';

  if (typeof price === 'string') {
    return Number(price).toFixed(2);
  }

  return price.toFixed(2);
};

// 计算套餐菜品列表
const dishes = computed(() => {
  // 优先使用setmealDishes
  if (props.setmeal.setmealDishes && Array.isArray(props.setmeal.setmealDishes) && props.setmeal.setmealDishes.length > 0) {
    return props.setmeal.setmealDishes;
  }

  // 否则使用dishList
  if (props.setmeal.dishList && Array.isArray(props.setmeal.dishList) && props.setmeal.dishList.length > 0) {
    return props.setmeal.dishList;
  }

  return [];
});

// 是否有菜品
const hasDishes = computed(() => dishes.value.length > 0);

// 是否有折扣
const hasDiscount = computed(() => {
  return props.setmeal.price &&
    props.setmeal.originalPrice &&
    Number(props.setmeal.originalPrice) > Number(props.setmeal.price);
});

// 计算折扣
const discount = computed(() => {
  if (!hasDiscount.value) return '';
  return (Number(props.setmeal.price) / Number(props.setmeal.originalPrice) * 10).toFixed(1);
});

// 计算套餐总价值
const totalValue = computed(() => {
  if (!hasDishes.value) return 0;

  return dishes.value.reduce((total, dish) => {
    const price = typeof dish.price === 'number' ? dish.price : Number(dish.price) || 0;
    const copies = dish.copies || 1;
    return total + (price * copies);
  }, 0);
});

// 计算节省金额
const savedAmount = computed(() => {
  const saving = totalValue.value - Number(props.setmeal.price);
  return saving > 0 ? saving : 0;
});

// 是否有节省金额
const hasSavings = computed(() => savedAmount.value > 0);

// 关闭面板
const close = () => {
  emit('close');
};

// 添加到购物车
const addToCart = async () => {
  try {
    // 构建购物车请求数据
    const shoppingCartData = {
      shopId: props.setmeal.shopId || uni.getStorageSync('shopId'),
      dishId: null,
      setmealId: props.setmeal.id
    };

    console.log('正在添加套餐到购物车:', shoppingCartData);

    // 调用添加购物车API
    const result = await shoppingCartApi.addShoppingCart(shoppingCartData);

    if (result.code === 1) {
      // 添加标记，避免重复请求
      const setmealWithMark = {
        ...props.setmeal,
        fromPanel: true,
        isSetmeal: true,
        type: 'setmeal'
      };

      // 触发confirm事件将商品数据传递给父组件
      emit('confirm', setmealWithMark);

      // 成功提示
      uni.showToast({
        title: '已加入购物车',
        icon: 'success'
      });

      // 延迟关闭面板
      setTimeout(() => {
        close();
      }, 500);
    } else {
      uni.showToast({
        title: result.msg || '添加购物车失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('添加套餐到购物车失败:', error);
    uni.showToast({
      title: '操作失败，请重试',
      icon: 'none'
    });
  }
};
</script>

<style lang="scss">
.setmeal-panel {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  visibility: hidden;

  &.panel-show {
    visibility: visible;

    .panel-mask {
      opacity: 1;
    }

    .panel-content {
      transform: translateY(0);
    }
  }

  .panel-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  .panel-content {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    max-height: 75vh; // 最大高度为屏幕的75%
    background-color: #fff;
    border-radius: 30rpx 30rpx 0 0;
    transform: translateY(100%);
    transition: transform 0.3s ease;
    overflow: hidden;
    display: flex;
    flex-direction: column;

    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 30rpx;
      border-bottom: 1rpx solid #f5f5f5;

      .panel-title {
        font-size: 32rpx;
        font-weight: 500;
        color: #333;
      }

      .close-btn {
        width: 50rpx;
        height: 50rpx;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }

    .setmeal-image {
      width: 100%;
      height: 400rpx;
      background-color: #f8f8f8;
      position: relative;
      overflow: hidden;

      image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.3s ease;
      }
    }

    .price-info {
      padding: 20rpx 30rpx;

      .price-row {
        display: flex;
        align-items: baseline;
        margin-bottom: 10rpx;

        .price-label {
          font-size: 26rpx;
          color: #666;
          margin-right: 10rpx;
        }

        .price-value {
          font-size: 36rpx;
          color: #ff5722;
          font-weight: 500;
        }

        .price-original {
          font-size: 26rpx;
          color: #999;
          text-decoration: line-through;
          margin-right: 10rpx;
        }

        .discount-tag {
          font-size: 22rpx;
          color: #ff5722;
          background-color: #fff0eb;
          padding: 2rpx 8rpx;
          border-radius: 6rpx;
        }
      }
    }

    .setmeal-desc {
      padding: 0 30rpx 20rpx;
      font-size: 26rpx;
      color: #666;
      line-height: 1.5;
    }

    .setmeal-dishes {
      padding: 20rpx 30rpx;
      max-height: calc(75vh - 500rpx); // 留出其他区域的空间
      overflow-y: auto;

      .section-title {
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
        margin-bottom: 20rpx;
        padding-bottom: 10rpx;
        border-bottom: 1rpx dashed #eee;
      }

      .dish-list {
        .dish-item {
          margin-bottom: 15rpx;

          .dish-info-row {
            display: flex;
            justify-content: space-between;

            .dish-name {
              font-size: 26rpx;
              color: #333;
            }

            .dish-copies {
              font-size: 24rpx;
              color: #666;
            }
          }

          .dish-price {
            font-size: 24rpx;
            color: #ff5722;
            margin-top: 4rpx;
          }
        }
      }

      .total-value {
        margin-top: 20rpx;
        padding-top: 15rpx;
        border-top: 1rpx dashed #eee;
        font-size: 26rpx;
        color: #666;
        display: flex;
        justify-content: space-between;

        .save-text {
          color: #ff5722;
          font-weight: 500;
        }
      }
    }

    .panel-footer {
      padding: 20rpx 30rpx;
      display: flex;
      border-top: 1rpx solid #f5f5f5;
      margin-top: auto;

      .action-btn {
        flex: 1;
        height: 80rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28rpx;
        border-radius: 40rpx;

        &.cancel {
          background-color: #f5f5f5;
          color: #666;
          margin-right: 15rpx;
        }

        &.confirm {
          background-color: #2588FF;
          color: #fff;
        }
      }
    }
  }
}
</style>