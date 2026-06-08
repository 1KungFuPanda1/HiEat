<template>
	<!--GoodsItem 用于菜品列表中单个菜品的精简展示-->
	<!-- ====================== 单个菜品卡片 整体容器 ====================== -->
	 <!-- 点击整个卡片 → 跳转到菜品详情页 -->
  <view class="goods-item" @click="viewDetail">
	   <!-- 左侧：菜品图片区域 -->
    <view class="goods-image">
		 <!-- 菜品图片：动态绑定 goods.image -->
      <image :src="goods.image" mode="aspectFill"></image>
       <!-- 如果是套餐（isSetmeal=true），显示【套餐】标签 -->
	  <view class="goods-tag" v-if="goods.isSetmeal">套餐</view>
    </view>
	  <!-- 右侧：菜品信息区域 -->
    <view class="goods-info">
		 <!-- 菜品名称 -->
      <view class="goods-name">{{ goods.name }}</view>
       <!-- 情况1：如果菜品有描述 → 显示描述 -->
	  <view class="goods-desc" v-if="goods.description">{{ goods.description }}</view>
       <!-- 情况2：如果是套餐 + 有包含菜品 → 显示套餐包含什么 -->
	  <view class="goods-desc" v-else-if="goods.isSetmeal && goods.dishList && goods.dishList.length">
        <text>{{ getSetmealDesc() }}</text>
      </view>
	    <!-- 月销量：月售xx份 -->
      <view class="goods-sales">月售{{ goods.quantitySold }}份</view>
       <!-- 底部：价格 + 购物车加减按钮 -->
	  <view class="goods-bottom">
         <!-- 价格区域 -->
		<view class="goods-price">
          <!-- 价格符号 ¥ -->
		  <text class="price-symbol">¥</text>
          <!-- 实际价格（保留两位小数） -->
		  <text class="price-value">{{ typeof goods.price === 'string' ? Number(goods.price).toFixed(2) :
            goods.price.toFixed(2) }}</text>
          <!-- 如果是套餐，显示划线原价 -->
		  <text class="price-original" v-if="goods.isSetmeal && goods.originalPrice">¥{{ typeof goods.originalPrice ===
            'string' ? Number(goods.originalPrice).toFixed(2) : goods.originalPrice.toFixed(2) }}</text>
        </view>
		   <!-- 右侧：购物车加减按钮 -->
        <view class="goods-action">
			 <!-- 减号按钮：数量 > 0 才显示 -->
          <view class="minus" v-if="count > 0" @click.stop="updateCount(-1)">
            <uni-icons type="minus" size="18" color="#666"></uni-icons>
          </view>
		     <!-- 数量显示：数量 > 0 才显示 -->
          <view class="count" v-if="count > 0">{{ count }}</view>
		    <!-- 加号按钮：点击添加到购物车 -->
          <view class="plus" @click.stop="handleAddClick">
            <uni-icons type="plus" size="18" color="#fff"></uni-icons>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { shoppingCartApi } from '@/api/index';

const props = defineProps({
  goods: {
    type: Object,
    required: true
  },
  cartList: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['add', 'view-detail']);

const count = ref(0);

// 监听购物车变化，更新商品数量
watch(() => props.cartList, (newVal) => {
  if (newVal && newVal.length) {
    const item = newVal.find(item => item.id === props.goods.id);
    count.value = item ? item.count : 0;
  } else {
    count.value = 0;
  }
}, { deep: true, immediate: true });

// 初始化时检查商品在购物车中的数量
onMounted(() => {
  if (props.cartList && props.cartList.length) {
    const item = props.cartList.find(item => item.id === props.goods.id);
    if (item) {
      count.value = item.count;
    }
  }
});

// 处理添加按钮点击事件
const handleAddClick = async (event) => {
  event.stopPropagation(); // 阻止冒泡，避免触发viewDetail

  // 如果商品需要选择口味或查看详情才能添加
  if (needsSelection()) {
    // 触发view-detail事件，显示详情面板
    emit('view-detail', props.goods);
  } else {
    try {
      // 简单商品直接添加到购物车，不需要显示详情面板
      // 首先发送添加购物车请求
      const shoppingCartData = {
        shopId: uni.getStorageSync('shopId'),
        dishId: null,
        setmealId: null
      };

      // 根据商品类型设置dishId或setmealId
      if (props.goods.isSetmeal || props.goods.type === 'setmeal') {
        shoppingCartData.setmealId = props.goods.id;
      } else {
        shoppingCartData.dishId = props.goods.id;
      }

      // 调用添加购物车API
      const result = await shoppingCartApi.addShoppingCart(shoppingCartData);

      if (result && result.code === 1) {
        // 添加成功，发送到父组件更新UI
        emit('add', { ...props.goods, isDirectAdd: true });
      } else {
        console.error('添加购物车失败:', result?.msg);
        uni.showToast({
          title: result?.msg || '添加商品失败',
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('添加购物车出错:', error);
      uni.showToast({
        title: '操作失败，请重试',
        icon: 'none'
      });
    }
  }
};



// 判断商品是否需要在详情面板中选择(口味/份数等)才能添加
const needsSelection = () => {
  // 如果是套餐，通常需要查看详情
  if (props.goods.isSetmeal || props.goods.type === 'setmeal') {
    return true;
  }

  // 如果是普通菜品，但有口味选项，需要显示面板选择口味
  if (props.goods.flavors && props.goods.flavors.length > 0) {
    return true;
  }

  // 其他情况可以直接添加
  return false;
};

// 获取套餐描述
const getSetmealDesc = () => {
  if (!props.goods.dishList || props.goods.dishList.length === 0) {
    return '';
  }

  // 提取套餐中的菜品名称和份数
  const dishTexts = props.goods.dishList.map(item => {
    let text = item.name;
    if (item.copies && item.copies > 1) {
      text += ` x${item.copies}`;
    }
    return text;
  });

  // 如果菜品太多，只显示前3个
  if (dishTexts.length > 3) {
    return dishTexts.slice(0, 3).join('、') + `等${dishTexts.length}件`;
  }

  return dishTexts.join('、');
};

// 更新商品数量
const updateCount = async (step) => {
  try {
    const newCount = count.value + step;
    if (newCount >= 0) {
      // 如果是减少商品数量，发送删除请求
      if (step < 0) {
        // 准备删除请求的参数
        const removeData = {
          shopId: uni.getStorageSync('shopId'), // 从本地存储获取shopId
          dishId: null,
          setmealId: null
        };

        // 根据商品类型设置参数
        if (props.goods.isSetmeal || props.goods.type === 'setmeal') {
          removeData.setmealId = props.goods.id;
        } else {
          removeData.dishId = props.goods.id;
        }

        // 如果有口味信息，添加到请求参数
        if (props.goods.selectedFlavors) {
          removeData.dishFlavor = JSON.stringify(props.goods.selectedFlavors);
        }

        // 发送删除购物车商品的请求
        try {
          const result = await shoppingCartApi.removeShoppingCartItem(removeData);

          if (result && result.code === 1) {
            // 删除成功，更新本地商品数量
            count.value = newCount;
            // 添加isReduce标记，让父组件知道这是减少操作
            const updatedGoods = { ...props.goods, isReduce: true };
            emit('add', updatedGoods);
          } else {
            console.error('删除购物车商品失败:', result?.msg);
            uni.showToast({
              title: result?.msg || '删除商品失败',
              icon: 'none'
            });
          }
        } catch (error) {
          console.error('删除购物车商品出错:', error);
          uni.showToast({
            title: '操作失败，请重试',
            icon: 'none'
          });
        }
        return;
      }

      // 增加商品数量的逻辑已经移到handleAddClick中
    }
  } catch (error) {
    console.error('更新商品数量出错:', error);
    uni.showToast({
      title: '操作失败，请重试',
      icon: 'none'
    });
  }
};

// 查看商品详情
const viewDetail = () => {
  // 触发view-detail事件，让父组件处理菜品详情或套餐详情的显示
  emit('view-detail', props.goods);
};
</script>

<style lang="scss">
.goods-item {
  display: flex;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f5f5f5;

  .goods-image {
    width: 160rpx;
    height: 160rpx;
    margin-right: 24rpx;
    position: relative;
    border-radius: 8rpx;
    overflow: hidden;
    box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.05);

    image {
      width: 100%;
      height: 100%;
    }

    .goods-tag {
      position: absolute;
      top: 0;
      left: 0;
      background-color: #ff9500;
      color: #fff;
      font-size: 20rpx;
      padding: 4rpx 10rpx;
      border-radius: 0 0 10rpx 0;
      font-weight: 500;
    }
  }

  .goods-info {
    flex: 1;
    position: relative;
    display: flex;
    flex-direction: column;
    justify-content: space-between;

    .goods-name {
      font-size: 28rpx;
      font-weight: 500;
      margin-bottom: 8rpx;
      color: #333;
      // 文字超出一行省略
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .goods-desc {
      font-size: 22rpx;
      color: #999;
      margin-bottom: 8rpx;
      // 文字超出两行省略
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
      overflow: hidden;
      line-height: 1.4;

      // 套餐内容样式增强
      text {
        color: #666;
      }
    }

    .goods-sales {
      font-size: 22rpx;
      color: #999;
      margin-bottom: 12rpx;
    }

    .goods-bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: auto;

      .goods-price {
        font-size: 30rpx;
        color: #ff5722;
        font-weight: 500;
        display: flex;
        align-items: baseline;

        .price-symbol {
          font-size: 22rpx;
        }

        .price-value {
          font-size: 30rpx;
        }

        .price-original {
          font-size: 22rpx;
          color: #999;
          text-decoration: line-through;
          margin-left: 10rpx;
        }
      }

      .goods-action {
        display: flex;
        align-items: center;

        .minus,
        .plus {
          width: 40rpx;
          height: 40rpx;
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
          width: 50rpx;
          text-align: center;
          font-size: 24rpx;
        }
      }
    }
  }
}
</style>