<template>
	<!--DishPanel 是点击菜品后弹出的详情规格面板-->
  <view class="dish-panel" :class="{ 'panel-show': show }">
    <!-- 遮罩层 -->
    <view class="panel-mask" @click="close" @touchmove.stop.prevent></view>

    <!-- 内容区域 -->
    <view class="panel-content" @touchmove.stop.prevent>
      <!-- 头部 -->
      <view class="panel-header">
        <view class="panel-title">{{ dish.name }}</view>
        <view class="close-btn" @click="close">
          <uni-icons type="closeempty" size="20" color="#999"></uni-icons>
        </view>
      </view>

      <!-- 菜品图片 -->
      <view class="dish-image" v-if="dish.image">
        <image :src="dish.image" mode="aspectFill"></image>
      </view>

      <!-- 价格信息 -->
      <view class="price-info">
        <view class="price-row">
          <text class="price-value">¥{{ formatPrice(dish.price) }}</text>
          <text class="price-original" v-if="dish.originalPrice && dish.originalPrice > dish.price">¥{{
            formatPrice(dish.originalPrice) }}</text>
        </view>
      </view>

      <!-- 描述 -->
      <view class="dish-desc" v-if="dish.description">
        <view class="desc-title">商品描述</view>
        <view class="desc-content">{{ dish.description }}</view>
      </view>

      <!-- 销量 -->
      <view class="dish-sales">
        月售 {{ dish.sales || 0 }}份
      </view>

      <!-- 口味选择 -->
      <view class="flavor-section" v-if="hasFlavors">
        <view class="section-title">口味选择</view>
        <view class="flavor-list">
          <view class="flavor-item" v-for="(flavor, index) in dish.flavors" :key="index">
            <view class="flavor-name">{{ flavor.name }}</view>
            <view class="flavor-options">
              <view class="flavor-option" v-for="(option, optIndex) in parseFlavorOptions(flavor.value)" :key="optIndex"
                :class="{ active: selectedFlavors[flavor.name] === option }" @click="selectFlavor(flavor.name, option)">
                {{ option }}
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 套餐菜品列表 -->
      <view class="setmeal-dishes" v-if="isSetmeal && hasSetmealDishes">
        <view class="section-title">套餐内容</view>
        <view class="dish-list">
          <view class="dish-item" v-for="(item, index) in setmealDishes" :key="index">
            <view class="dish-info-row">
              <view class="dish-name">{{ item.name }}</view>
              <view class="dish-copies" v-if="item.copies && item.copies > 1">x{{ item.copies }}</view>
            </view>
            <view class="dish-price">¥{{ formatPrice(item.price) }}</view>
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
        <view class="counter">
          <view class="minus" v-if="count > 0" @click="updateCount(-1)">
            <uni-icons type="minus" size="18" color="#666"></uni-icons>
          </view>
          <view class="count">{{ count }}</view>
          <view class="plus" @click="updateCount(1)">
            <uni-icons type="plus" size="18" color="#fff"></uni-icons>
          </view>
        </view>
        <view class="action-btn" @click="addToCart">加入购物车</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { shoppingCartApi } from '@/api/index';

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  dish: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['close', 'add']);

// 商品数量
const count = ref(1);

// 选择的口味
const selectedFlavors = ref({});

// 是否是套餐
const isSetmeal = computed(() => {
  return props.dish.isSetmeal || props.dish.type === 'setmeal';
});

// 是否有套餐菜品列表
const hasSetmealDishes = computed(() => {
  // 判断是否有套餐菜品数据
  if (props.dish.setmealDishes && Array.isArray(props.dish.setmealDishes) && props.dish.setmealDishes.length > 0) {
    return true;
  }
  if (props.dish.dishList && Array.isArray(props.dish.dishList) && props.dish.dishList.length > 0) {
    return true;
  }
  return false;
});

// 套餐菜品列表
const setmealDishes = computed(() => {
  // 优先使用setmealDishes
  if (props.dish.setmealDishes && Array.isArray(props.dish.setmealDishes) && props.dish.setmealDishes.length > 0) {
    return props.dish.setmealDishes;
  }

  // 其次使用dishList
  if (props.dish.dishList && Array.isArray(props.dish.dishList) && props.dish.dishList.length > 0) {
    return props.dish.dishList;
  }

  return [];
});

// 计算套餐总价值
const totalValue = computed(() => {
  if (!hasSetmealDishes.value) return 0;

  return setmealDishes.value.reduce((total, dish) => {
    const price = typeof dish.price === 'number' ? dish.price : Number(dish.price) || 0;
    const copies = dish.copies || 1;
    return total + (price * copies);
  }, 0);
});

// 计算节省金额
const savedAmount = computed(() => {
  const saving = totalValue.value - Number(props.dish.price);
  return saving > 0 ? saving : 0;
});

// 是否有节省金额
const hasSavings = computed(() => savedAmount.value > 0);

// 格式化价格
const formatPrice = (price) => {
  if (!price) return '0.00';

  if (typeof price === 'string') {
    return Number(price).toFixed(2);
  }

  return price.toFixed(2);
};

// 是否有口味选择
const hasFlavors = computed(() => {
  return props.dish.flavors && Array.isArray(props.dish.flavors) && props.dish.flavors.length > 0;
});

// 解析口味选项
const parseFlavorOptions = (value) => {
  try {
    if (!value) return [];

    // 尝试解析JSON字符串
    if (typeof value === 'string') {
      return JSON.parse(value);
    }

    // 如果已经是数组，直接返回
    if (Array.isArray(value)) {
      return value;
    }

    return [];
  } catch (error) {
    console.error('解析口味选项失败:', error);
    return [];
  }
};

// 初始化口味选择
const initFlavors = () => {
  selectedFlavors.value = {};
  if (hasFlavors.value) {
    props.dish.flavors.forEach(flavor => {
      const options = parseFlavorOptions(flavor.value);
      if (options.length > 0) {
        selectedFlavors.value[flavor.name] = options[0]; // 默认选择第一个选项
      }
    });
  }
};

// 选择口味
const selectFlavor = (flavorName, option) => {
  selectedFlavors.value[flavorName] = option;
};

// 获取已选择的口味文本描述
const getSelectedFlavorsText = () => {
  const parts = [];
  for (const [name, value] of Object.entries(selectedFlavors.value)) {
    parts.push(`${name}: ${value}`);
  }
  return parts.join(', ');
};

// 更新数量
const updateCount = (step) => {
  const newCount = count.value + step;
  if (newCount > 0) {
    count.value = newCount;
  }
};

// 加入购物车
const addToCart = async () => {
  if (count.value <= 0) {
    count.value = 1; // 如果数量为0，自动设为1
  }

  try {
    // 构建购物车请求数据
    const shoppingCartData = {
      shopId: props.dish.shopId || uni.getStorageSync('shopId'),
      dishId: null,
      setmealId: null,
    };

    // 根据商品类型设置dishId或setmealId
    if (isSetmeal.value) {
      shoppingCartData.setmealId = props.dish.id;
    } else {
      shoppingCartData.dishId = props.dish.id;
    }

    // 如果是普通菜品且选择了口味，添加口味信息
    if (!isSetmeal.value && Object.keys(selectedFlavors.value).length > 0) {
      shoppingCartData.dishFlavor = JSON.stringify(selectedFlavors.value);
    }

    // 调用添加购物车API
    const result = await shoppingCartApi.addShoppingCart(shoppingCartData);

    if (result.code === 1) {
      // 构建要添加到前端购物车数据的商品对象
      let cartItem = {
        ...props.dish,
        count: count.value,
        fromPanel: true // 添加标记，表示是从面板中添加的，避免重复请求
      };

      // 如果是普通菜品，添加口味信息
      if (!isSetmeal.value && Object.keys(selectedFlavors.value).length > 0) {
        cartItem.selectedFlavors = { ...selectedFlavors.value };
        cartItem.flavorText = getSelectedFlavorsText();
      }

      // 如果是套餐，确保套餐标识和菜品数据
      if (isSetmeal.value) {
        cartItem.isSetmeal = true;
        cartItem.type = 'setmeal';
      }

      // 触发add事件，将商品传递给父组件
      emit('add', cartItem);

      // 显示成功提示
      uni.showToast({
        title: '已加入购物车',
        icon: 'success'
      });

      // 延迟关闭面板，让用户看到成功提示
      setTimeout(() => {
        emit('close');
      }, 500);
    } else {
      uni.showToast({
        title: result.msg || '添加购物车失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('添加购物车失败:', error);
    uni.showToast({
      title: '操作失败，请重试',
      icon: 'none'
    });
  }
};

// 关闭面板
const close = () => {
  emit('close');
};

// 监听显示状态变化，初始化数据
watch(() => props.show, (val) => {
  if (val) {
    count.value = 1;
    initFlavors();
  }
});

// 组件挂载时执行一次初始化，确保数据正确
onMounted(() => {
  initFlavors();
});
</script>

<style lang="scss">
.dish-panel {
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

    .dish-image {
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
      padding: 20rpx 30rpx 10rpx;

      .price-row {
        display: flex;
        align-items: baseline;

        .price-value {
          font-size: 36rpx;
          color: #ff5722;
          font-weight: 500;
        }

        .price-original {
          font-size: 24rpx;
          color: #999;
          text-decoration: line-through;
          margin-left: 10rpx;
        }
      }
    }

    .dish-desc {
      padding: 10rpx 30rpx 20rpx;
      border-bottom: 1rpx dashed #eee;

      .desc-title {
        font-size: 26rpx;
        color: #666;
        margin-bottom: 6rpx;
        font-weight: 500;
      }

      .desc-content {
        font-size: 26rpx;
        color: #666;
        line-height: 1.5;
      }
    }

    .dish-sales {
      padding: 15rpx 30rpx;
      font-size: 24rpx;
      color: #999;
      border-bottom: 1rpx solid #f5f5f5;
    }

    .flavor-section {
      padding: 20rpx 30rpx;

      .section-title {
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
        margin-bottom: 20rpx;
      }

      .flavor-list {
        .flavor-item {
          margin-bottom: 20rpx;

          &:last-child {
            margin-bottom: 0;
          }

          .flavor-name {
            font-size: 26rpx;
            color: #666;
            margin-bottom: 15rpx;
          }

          .flavor-options {
            display: flex;
            flex-wrap: wrap;

            .flavor-option {
              padding: 10rpx 20rpx;
              background-color: #f8f8f8;
              border-radius: 30rpx;
              margin-right: 20rpx;
              margin-bottom: 15rpx;
              font-size: 24rpx;
              color: #666;

              &.active {
                background-color: #2588FF;
                color: #fff;
              }
            }
          }
        }
      }
    }

    .setmeal-dishes {
      padding: 20rpx 30rpx;
      border-top: 1rpx solid #f5f5f5;
      max-height: calc(75vh - 600rpx); // 留出其他区域的空间
      overflow-y: auto;

      .section-title {
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
        margin-bottom: 20rpx;
      }

      .dish-list {
        .dish-item {
          margin-bottom: 15rpx;
          padding-bottom: 15rpx;
          border-bottom: 1rpx dashed #f5f5f5;

          &:last-child {
            border-bottom: none;
          }

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

      .counter {
        display: flex;
        align-items: center;
        margin-right: 20rpx;

        .minus,
        .plus {
          width: 50rpx;
          height: 50rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 50%;
        }

        .minus {
          background-color: #f5f5f5;
          border: 1rpx solid #eee;
        }

        .plus {
          background-color: #2588FF;
        }

        .count {
          width: 60rpx;
          text-align: center;
          font-size: 28rpx;
        }
      }

      .action-btn {
        flex: 1;
        height: 80rpx;
        background-color: #2588FF;
        color: #fff;
        font-size: 28rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 40rpx;
      }
    }
  }
}
</style>