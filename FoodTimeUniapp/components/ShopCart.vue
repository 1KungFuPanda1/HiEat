<template>
  <view class="shop-cart">
    <!-- ====================== 【弹出层：已选商品列表】 ====================== -->
       <!-- 显示条件：购物车有商品 + 购物车面板打开 -->
    <view class="cart-list-container" v-if="cartList.length > 0 && isCartOpen">
     <!-- 弹出层头部：标题 + 清空购物车 -->
	  <view class="cart-header">
        <text class="cart-title">已选商品</text>
        <view class="cart-clear" @click="clearCart">
          <uni-icons type="trash" size="16" color="#999"></uni-icons>
          <text>清空购物车</text>
        </view>
      </view>
	   <!-- 商品列表：允许纵向滚动 -->
      <scroll-view class="cart-list" scroll-y>
		  <!-- 循环渲染：购物车里的每一个商品 -->
        <view class="cart-item" v-for="(item, index) in cartList" :key="index">
          <image class="item-image" :src="item.image" mode="aspectFill"></image>
          <!-- 商品信息区域：名称 / 规格 / 价格 -->
		  <view class="item-info">
			  <!-- 商品名称 -->
            <view class="item-name">
              {{ item.name }}
			   <!-- 如果是套餐，显示“套餐”标签 -->
              <text v-if="item.type === 'setmeal' || item.isSetmeal" class="setmeal-tag">套餐</text>
            </view>
			  <!-- 商品规格/口味：如“不要葱”“中辣”，有值才显示 -->
            <view class="item-specs" v-if="item.flavorText">{{ item.flavorText }}</view>
           
			   <!-- ========== 套餐内容展示（只有套餐才显示） ========== -->
			<view class="item-setmeal" v-if="(item.type === 'setmeal' || item.isSetmeal) &&
              ((item.setmealDishes && item.setmealDishes.length > 0) ||
                (item.dishList && item.dishList.length > 0))">
              <view class="setmeal-title">
                <text>套餐内容</text>
                <text class="dish-count">共{{ getDishCount(item) }}个菜品</text>
              </view>
			  <!-- 循环展示套餐里的菜品 -->
              <view class="setmeal-dishes-list">
                <view class="setmeal-dish-tag" v-for="(dish, dIndex) in getSetmealDishes(item)" :key="dIndex">
                  {{ dish }}
                </view>
              </view>
            </view>
			<!-- 商品价格：保留两位小数 -->
            <view class="item-price">¥{{ item.price.toFixed(2) }}</view>
          </view>
		   <!-- ====================== 【数量加减按钮】 ====================== -->
          <view class="item-action">
			   <!-- 减号：数量-1 -->
			   <!-- @click.stop 阻止事件冒泡，避免点按钮时关闭购物车 -->
            <view class="minus" @click.stop="updateCartItem(item, -1)">
              <uni-icons type="minus" size="16" color="#666"></uni-icons>
            </view>
			 <!-- 商品数量 -->
            <view class="count">{{ item.count }}</view>
			<!-- 加号：数量+1 -->
            <view class="plus" @click.stop="updateCartItem(item, 1)">
              <uni-icons type="plus" size="16" color="#fff"></uni-icons>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

     <!-- ====================== 【底部固定栏：购物车+结算】 ====================== -->
    <view class="cart-footer">
		 <!-- 左侧：购物车图标 -->
	      <!-- has-items：有商品时改变样式 -->
      <view class="cart-icon" :class="{ 'has-items': totalCount > 0 }" @click="toggleCartList">
        <uni-icons type="cart" size="28" color="#fff"></uni-icons>
        <!-- 小红点角标：显示商品总数，没有商品时不显示 -->
		<view class="cart-badge" v-if="totalCount > 0">{{ totalCount }}</view>
      </view>
	   <!-- 中间：价格信息 -->
      <view class="cart-info">
		   <!-- 有价格时显示合计 -->
        <view class="total-price" v-if="totalPrice > 0">合计 ¥{{ totalPrice.toFixed(2) }}</view>
         <!-- 没价格时显示“未选购商品” -->
		<view class="empty-text" v-else>未选购商品</view>
          <!-- 配送费：后端返回单位是分，所以要 /100 -->
		<view class="delivery-fee" v-if="shopInfo.deliverFee">配送费 ¥{{ (shopInfo.deliverFee / 100).toFixed(2) }}</view>
      </view>
	  
	    <!-- 右侧：结算按钮 -->
	    <!-- btn-active：满足起送价才高亮可点击 -->
		 <!-- 满足起送价显示“结算”，不满足显示“还差XX元起送” -->
      <view class="checkout-btn" :class="{ 'btn-active': canCheckout }" @click="checkout">
        {{ canCheckout ? '结算' : `¥${minFeeGap.toFixed(2)}起送` }}
      </view>
    </view>
  
	
	   <!-- ====================== 【全屏遮罩层】 ====================== -->
	    <!-- 购物车打开时显示，点击灰色区域关闭购物车 -->
    <view class="mask" v-if="isCartOpen" @click="closeCartList"></view>
  </view>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue';
import { shoppingCartApi } from '@/api/index';

const props = defineProps({
  cartList: {
    type: Array,
    default: () => []
  },
  shopInfo: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['update-cart']);

// 购物车是否打开
const isCartOpen = ref(false);

// 切换购物车列表显示/隐藏
const toggleCartList = () => {
  if (totalCount.value > 0) {
    isCartOpen.value = !isCartOpen.value;
  } else {
    uni.showToast({
      title: '购物车是空的',
      icon: 'none'
    });
  }
};

// 关闭购物车列表
const closeCartList = () => {
  isCartOpen.value = false;
};

// 获取套餐中菜品的总数量
const getDishCount = (item) => {
  if (item.setmealDishes && Array.isArray(item.setmealDishes) && item.setmealDishes.length > 0) {
    return item.setmealDishes.length;
  } else if (item.dishList && Array.isArray(item.dishList) && item.dishList.length > 0) {
    return item.dishList.length;
  }
  return 0;
};

// 格式化套餐内菜品
const formatSetmealDishes = (dishes) => {
  // 尝试处理不同的数据来源
  if (!dishes || !Array.isArray(dishes) || dishes.length === 0) {
    return [];
  }

  // 显示所有菜品，标签形式
  return dishes.map(dish => {
    const name = dish.name.length > 5 ? dish.name.substring(0, 5) + '...' : dish.name;
    const copies = dish.copies && dish.copies > 1 ? `×${dish.copies}` : '';
    return `${name}${copies}`;
  });
};

// 获取套餐内菜品数据，兼容两种数据结构
const getSetmealDishes = (item) => {
  // 优先使用setmealDishes
  if (item.setmealDishes && Array.isArray(item.setmealDishes) && item.setmealDishes.length > 0) {
    return formatSetmealDishes(item.setmealDishes);
  }
  // 如果没有setmealDishes但有dishList，使用dishList
  else if (item.dishList && Array.isArray(item.dishList) && item.dishList.length > 0) {
    return formatSetmealDishes(item.dishList);
  }
  // 都没有，返回空数组
  return [];
};

// 清空购物车
const clearCart = async () => {
  uni.showModal({
    title: '提示',
    content: '确定要清空购物车吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用清空购物车API
          const result = await shoppingCartApi.cleanShoppingCart();

          if (result && result.code === 1) {
            // 清空前端购物车
            nextTick(() => {
              emit('update-cart', []);
              closeCartList();

              uni.showToast({
                title: '购物车已清空',
                icon: 'success'
              });
            });
          } else {
            uni.showToast({
              title: result?.msg || '清空购物车失败',
              icon: 'none'
            });
          }
        } catch (error) {
          console.error('清空购物车失败:', error);
          uni.showToast({
            title: '操作失败，请重试',
            icon: 'none'
          });
        }
      }
    }
  });
};

// 更新购物车商品数量
const updateCartItem = async (item, step) => {
  try {
    console.log('更新购物车商品:', item.name, '操作:', step > 0 ? '加' : '减');

    const newCount = item.count + step;
    if (newCount < 0) return;

    // 准备API请求参数
    const cartData = {
      shopId: props.shopInfo.id,
      dishId: null,
      setmealId: null
    };

    // 根据商品类型设置参数
    if (item.type === 'setmeal' || item.isSetmeal) {
      cartData.setmealId = item.id;
    } else {
      cartData.dishId = item.id;
    }

    let apiSuccess = false;

    if (step > 0) {
      // 加号：调用添加购物车API
      const result = await shoppingCartApi.addShoppingCart(cartData);
      if (result && result.code === 1) {
        apiSuccess = true;
        console.log('添加购物车成功:', item.name);
      } else {
        console.error('添加购物车失败:', result?.msg);
        uni.showToast({ title: result?.msg || '添加失败', icon: 'none' });
        return;
      }
    } else {
      // 减号：调用减少购物车API（后端subShoppingCart）
      const result = await shoppingCartApi.removeShoppingCartItem(cartData);
      if (result && result.code === 1) {
        apiSuccess = true;
        console.log('减少购物车成功:', item.name);
      } else {
        console.error('减少购物车失败:', result?.msg);
        uni.showToast({ title: result?.msg || '减少失败', icon: 'none' });
        return;
      }
    }

    if (apiSuccess) {
      // 在 API 调用成功后，更新前端的购物车数据用于 UI 展示 。
      const cartCopy = [...props.cartList];
      const index = cartCopy.findIndex(i => i.id === item.id);// 找到当前商品在数组中的位置

      if (newCount === 0) {
        // 数量为0，从购物车移除
        if (index > -1) {
          cartCopy.splice(index, 1);
        }
      } else if (index > -1) {
        // 创建商品对象的深拷贝，更新数量
        const updatedItem = JSON.parse(JSON.stringify({ ...item, count: newCount }));

        //套餐数据一致性处理（确保套餐有 setmealDishes 数据）
        if ((item.type === 'setmeal' || item.isSetmeal) &&
          (!updatedItem.setmealDishes || updatedItem.setmealDishes.length === 0) &&
          updatedItem.dishList && updatedItem.dishList.length > 0) {
          updatedItem.setmealDishes = JSON.parse(JSON.stringify(updatedItem.dishList.map(dish => ({
            ...dish,
            id: dish.id || null,
            setmealId: updatedItem.id,
            dishId: dish.dishId || dish.id || null,
            name: dish.name,
            price: dish.price || 0,
            copies: dish.copies || 1
          }))));
        }
         // 更新数组中的商品
        cartCopy[index] = updatedItem;
      }
     // 通知父组件更新购物车数据
      nextTick(() => {
        emit('update-cart', cartCopy);
      });
    }
  } catch (error) {
    console.error('更新购物车商品数量出错:', error);
    uni.showToast({ title: '操作失败，请重试', icon: 'none' });
  }
};

// 确保数组安全的 reduce 函数
const safeReduce = (array, callback, initialValue) => {
  if (!Array.isArray(array)) {
    console.error('传入的不是数组:', array);
    return initialValue;
  }
  return array.reduce(callback, initialValue);
};

// 计算总价
const totalPrice = computed(() => {
  return safeReduce(props.cartList, (total, item) => {
    return total + (item.price * item.count);
  }, 0);
});

// 计算总数量
const totalCount = computed(() => {
  return safeReduce(props.cartList, (total, item) => {
    return total + item.count;
  }, 0);
});

// 计算是否达到起送价
const minFeeGap = computed(() => {
  if (!props.shopInfo.minFee) return 0;
  // 将起送价除以100后再比较
  const minFee = props.shopInfo.minFee / 100;
  const gap = minFee - totalPrice.value;
  return gap > 0 ? gap : 0;
});

// 是否可以结算
const canCheckout = computed(() => {
  return totalCount.value > 0 && minFeeGap.value <= 0;
});

// 去结算
const checkout = () => {
  if (!canCheckout.value) {
    uni.showToast({
      title: `还差¥${minFeeGap.value.toFixed(2)}元起送`,
      icon: 'none'
    });
    return;
  }

  // 跳转到提交订单页面
  uni.navigateTo({
    url: `/pages/order/submit?shopId=${props.shopInfo.id}`
  });
};

// 监听购物车变化，处理特殊情况
watch(() => props.cartList, (newVal) => {
  // 确保 cartList 是数组
  if (!Array.isArray(newVal)) {
    console.error('传入的购物车数据不是数组:', newVal);
    return;
  }

  // 如果购物车为空，自动关闭购物车列表
  if (newVal.length === 0 && isCartOpen.value) {
    closeCartList();
  }
}, { deep: true, immediate: true });

// 添加一个调试模式标志
const showDebugInfo = ref(true); // 设置为true可以显示调试信息
</script>

<style lang="scss">
.shop-cart {
  position: relative;
  z-index: 100;

  // 遮罩层
  .mask {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.3);
    z-index: 995;
  }

  // 购物车列表容器
  .cart-list-container {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 100rpx; // 与底部结算栏保持一致的高度
    background-color: #ffffff;
    box-shadow: 0 -4rpx 12rpx rgba(0, 0, 0, 0.1);
    z-index: 996;
    max-height: 60vh; // 最大高度
    display: flex;
    flex-direction: column;
    width: 100%;
    box-sizing: border-box;
    border-top-left-radius: 16rpx;
    border-top-right-radius: 16rpx;
    animation: slideUp 0.25s ease-out;

    .cart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx 24rpx;
      border-bottom: 1rpx solid #f5f5f5;

      .cart-title {
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
      }

      .cart-clear {
        display: flex;
        align-items: center;
        font-size: 24rpx;
        color: #999;
        padding: 6rpx 12rpx;
        background-color: #f8f8f8;
        border-radius: 30rpx;

        text {
          margin-left: 6rpx;
        }
      }
    }

    .cart-list {
      padding: 20rpx 30rpx;
      max-height: 60vh; // 增大最大高度至屏幕的60%，确保大量内容时可以滚动
      overflow-y: auto;
      width: 100%;
      box-sizing: border-box;

      .cart-item {
        position: relative;
        display: flex;
        align-items: flex-start;
        padding: 20rpx 0;
        border-bottom: 1rpx solid #f5f5f5;

        &:last-child {
          border-bottom: none;
        }

        // 为套餐项添加轻微动画效果
        &:has(.item-setmeal) {
          transition: background-color 0.3s;

          &:hover {
            background-color: #f9f9f9;
          }
        }

        .item-image {
          width: 80rpx;
          height: 80rpx;
          border-radius: 8rpx;
          margin-right: 16rpx;
          background-color: #f8f8f8;
          flex-shrink: 0;
        }

        .item-info {
          flex: 1;
          padding-right: 16rpx;
          overflow: hidden;
          min-width: 0; // 防止flex子元素不收缩

          .item-name {
            font-size: 26rpx;
            color: #333;
            margin-bottom: 4rpx;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;

            .setmeal-tag {
              display: inline-block;
              font-size: 20rpx;
              background-color: #2588FF;
              color: #fff;
              padding: 2rpx 8rpx;
              border-radius: 6rpx;
              margin-left: 8rpx;
              line-height: 1;
              vertical-align: middle;
              font-weight: normal;
            }
          }

          .item-specs {
            font-size: 22rpx;
            color: #999;
            margin-bottom: 4rpx;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .item-setmeal {
            margin-top: 8rpx;
            background-color: #f9f9f9;
            border-radius: 12rpx;
            padding: 10rpx 12rpx;
            margin-bottom: 8rpx;

            .setmeal-title {
              font-size: 20rpx;
              color: #666;
              margin-bottom: 8rpx;
              position: relative;
              padding-left: 16rpx;
              display: flex;
              justify-content: space-between;
              align-items: center;

              &::before {
                content: "";
                position: absolute;
                left: 0;
                top: 50%;
                transform: translateY(-50%);
                width: 6rpx;
                height: 16rpx;
                background-color: #2588FF;
                border-radius: 3rpx;
              }

              .dish-count {
                font-size: 18rpx;
                color: #999;
                background-color: #f2f2f2;
                padding: 2rpx 8rpx;
                border-radius: 10rpx;
              }
            }

            .setmeal-dishes-list {
              display: flex;
              flex-wrap: wrap;
              gap: 8rpx;

              .setmeal-dish-tag {
                font-size: 20rpx;
                color: #2588FF;
                background-color: #f0f7ff;
                padding: 4rpx 12rpx;
                border-radius: 20rpx;
                display: inline-block;
                border: 1rpx solid rgba(37, 136, 255, 0.2);
                box-shadow: 0 1rpx 3rpx rgba(0, 0, 0, 0.05);
                transition: all 0.2s;

                &:hover,
                &:active {
                  transform: translateY(-1rpx);
                  box-shadow: 0 2rpx 5rpx rgba(37, 136, 255, 0.1);
                }
              }
            }
          }

          .item-price {
            font-size: 26rpx;
            color: #ff5722;
            font-weight: 500;
            margin-top: 4rpx;
          }
        }

        .item-action {
          display: flex;
          align-items: center;
          flex-shrink: 0;

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

  // 购物车底部栏
  .cart-footer {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    height: 100rpx;
    background-color: #333333;
    display: flex;
    align-items: center;
    z-index: 997;
    box-shadow: 0 -2rpx 6rpx rgba(0, 0, 0, 0.1);

    .cart-icon {
      width: 100rpx;
      height: 100rpx;
      display: flex;
      justify-content: center;
      align-items: center;
      position: relative;
      background-color: #444444;
      border-radius: 0 16rpx 0 0;
      transition: all 0.3s;

      &.has-items {
        background-color: #2588FF;
      }

      .cart-badge {
        position: absolute;
        top: 16rpx;
        right: 16rpx;
        min-width: 30rpx;
        height: 30rpx;
        line-height: 30rpx;
        text-align: center;
        background-color: #ff5722;
        color: #fff;
        border-radius: 15rpx;
        font-size: 20rpx;
        padding: 0 6rpx;
        font-weight: bold;
        border: 2rpx solid #fff;
      }
    }

    .cart-info {
      flex: 1;
      padding-left: 24rpx;

      .total-price {
        font-size: 32rpx;
        font-weight: 500;
        color: #fff;
      }

      .empty-text {
        font-size: 28rpx;
        color: rgba(255, 255, 255, 0.7);
      }

      .delivery-fee {
        font-size: 22rpx;
        color: rgba(255, 255, 255, 0.5);
      }
    }

    .checkout-btn {
      width: 180rpx;
      height: 100%;
      background-color: #666666;
      color: #fff;
      display: flex;
      justify-content: center;
      align-items: center;
      font-size: 30rpx;
      font-weight: 500;
      border-radius: 0 0 0 16rpx;
      transition: all 0.3s;

      &.btn-active {
        background-color: #2588FF;
      }
    }
  }
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }

  to {
    transform: translateY(0);
  }
}
</style>