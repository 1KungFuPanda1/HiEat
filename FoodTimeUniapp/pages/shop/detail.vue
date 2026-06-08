<template>
  <view class="shop-detail">
    <!-- 店铺基本信息 -->
    <view class="shop-info">
      <view class="shop-banner">
        <view class="shop-overlay"></view>
      </view>

      <!-- 白色卡片 -->
      <view class="shop-card">
        <view class="shop-header-content">
          <view class="shop-info-left">
            <view class="shop-name-large">{{ shopInfo.shopName }}</view>
            <view class="shop-desc-header">{{ shopInfo.des }}</view>
          </view>
          <view class="shop-logo">
            <image :src="shopInfo.image" mode="aspectFill"></image>
          </view>
        </view>

        <view class="shop-notice-bar" @click="showNotice">
          <uni-icons type="sound" size="16" color="#2588FF"></uni-icons>
          <text class="notice-text">公告: {{ shopNotice }}</text>
        </view>

        <!-- 打烊中提示 -->
        <view class="shop-closed-bar" v-if="shopStatus === 0">
          <text class="closed-text">店铺已打烊，暂时无法下单</text>
        </view>
      </view>
    </view>

    <!-- 选项卡 -->
    <view class="tabs">
      <view class="tab-item" :class="{ active: activeTab === 'menu' }" @click="switchTab('menu')">
        点餐
      </view>
      <view class="tab-item" :class="{ active: activeTab === 'comment' }" @click="switchTab('comment')">
        评价
      </view>
      <view class="tab-item" :class="{ active: activeTab === 'merchant' }" @click="switchTab('merchant')">
        商家
      </view>
    </view>

    <!-- 内容区域 -->
    <view class="content-area">
      <!-- 点餐内容 -->
      <view v-if="activeTab === 'menu'" class="menu-content">
        <view class="menu-container">
          <!-- 左侧分类列表 -->
          <scroll-view class="category-list" scroll-y>
            <view class="category-item" v-for="(category, index) in categories" :key="index"
              :class="{ active: currentCategory === index }" @click="selectCategory(index)">
              <view class="category-badge" v-if="category.count > 0">{{ category.count }}</view>
              <text>{{ category.name }}</text>
              <view class="category-tag" v-if="category.type === 2">套餐</view>
            </view>
          </scroll-view>

          <!-- 右侧商品列表 -->
          <scroll-view class="goods-list" scroll-y @scroll="onGoodsScroll" scroll-with-animation
            :scroll-into-view="scrollToViewId" :scroll-with-animation="true" :enhanced="true" :show-scrollbar="false">
			  <!-- ====================== 循环渲染【商品分类】 ====================== -->
			  <!-- 比如：暖胃靓汤、主食必点、人气推荐... 每一个分类一块 -->
			<view v-for="(category, index) in categories" :key="index" :id="`category-${index}`"
              class="category-section">
			   <!-- 分类标题：如 “暖胃靓汤” -->
              <view class="category-title">
                {{ category.name }}
				<!-- 如果是套餐分类（type=2），显示“套餐”标签 -->
                <view class="category-label" v-if="category.type === 2">套餐</view>
              </view>
              <!-- ====================== 分类下的【商品列表】 ====================== -->
                 <!-- 如果该分类下有菜品 → 循环渲染商品 -->
              <block v-if="category.dishList && category.dishList.length > 0">
			   <!-- 
			          循环渲染 商品组件 GoodsItem
			          你截图里 一行一行的商品 就是这里渲染出来的！
			          每一个 goods-item 就是一个商品
			        -->
                <goods-item v-for="(item, idx) in category.dishList" :key="`${index}-${idx}`" :goods="item"
                  :cart-list="cartList" @add="addToCart" @view-detail="viewGoodsDetail"></goods-item>
              </block>
               <!-- ====================== 加载中状态 ====================== -->
                  <!-- 数据还没加载完 → 显示加载中 -->
              <view v-else-if="!category.loaded && !category.isEmpty" class="loading-state">
                <view class="loading-icon">
                  <uni-icons type="spinner-cycle" size="24" color="#ccc"></uni-icons>
                </view>
                <text class="loading-text">加载中...</text>
              </view>
             <!-- ====================== 该分类无商品 空状态 ====================== -->
              <view v-else class="empty-state">
                <text class="empty-text">暂无{{ category.type === 2 ? '套餐' : '菜品' }}</text>
              </view>
            </view>
          </scroll-view>
        </view>
      </view>

      <!-- 评价内容 -->
      <view v-if="activeTab === 'comment'" class="comment-content">
        <!-- 评分区 -->
        <view class="comment-header single">
          <view class="header-center">
           <view class="score-main">{{ (shopInfo.score || 4.8).toFixed(1) }}</view>
            <uni-rate :value="shopInfo.score || 4.8" readonly size="22" margin="2"></uni-rate>
            <view class="score-desc">商品质量 共多少人点评</view>
          </view>
        </view>
        <!-- 标签区 -->
        <view class="review-tags">
			 <!-- 下面这4个就是 筛选按钮：全部、好评、中评、差评 -->
          <view class="tag-item" v-for="filter in reviewFilters" :key="filter.value"
            :class="{ active: reviewRating === filter.value }" @click="switchReviewFilter(filter.value)">
            {{ filter.label }}
			<template v-if="filter.count !== undefined">({{ filter.count }})</template>
          </view>
          <view class="tag-item" v-for="tag in reviewExtraTags" :key="tag.value">
            {{ tag.label }}<template v-if="tag.count !== undefined">({{ tag.count }})</template>
          </view>
        </view>
        <!-- 评论卡片区 -->
        <view class="review-list">
          <view class="review-card" v-for="review in reviewList" :key="review.id">
            <view class="review-card-header">
              <image class="user-avatar" :src="review.userAvatar || '/static/image/avatar.png'" mode="aspectFill">
              </image>
              <view class="user-meta">
                <view class="user-name">{{ review.userName }}</view>
                <uni-rate :value="review.rating" readonly size="16" margin="1"></uni-rate>
              </view>
              <view class="review-time">{{ review.createTime }}</view>
            </view>
            <view class="review-content">{{ review.content }}</view>
            <view class="review-images" v-if="review.photoUrls && review.photoUrls.length > 0">
              <image v-for="(img, imgIdx) in review.photoUrls" :key="imgIdx" :src="img" mode="aspectFill"
                @click="previewImage(review.photoUrls, imgIdx)"></image>
            </view>
            <view class="review-replies" v-if="review.replies && review.replies.length > 0">
              <view class="reply-item" v-for="(reply, replyIdx) in review.replies" :key="replyIdx">
                <text class="reply-label">商家回复：</text>
                <text class="reply-content">{{ reply.content }}</text>
              </view>
            </view>
          </view>
          <!-- 加载状态 -->
          <view class="loading-more" v-if="reviewLoading">
            <uni-icons type="spinner-cycle" size="20" color="#999"></uni-icons>
            <text>加载中...</text>
          </view>
          <!-- 加载完成提示 -->
          <view class="load-complete" v-if="!reviewLoading && reviewList.length >= reviewTotal">
            <text>没有更多评论了</text>
          </view>
          <!-- 空状态 -->
          <view class="empty-state" v-if="!reviewLoading && reviewList.length === 0">
            <text>暂无评论</text>
          </view>
        </view>
      </view>

      <!-- 商家内容 -->
      <view v-if="activeTab === 'merchant'" class="merchant-content">
        <view class="merchant-info">
          <view class="info-item">
            <view class="info-label">店铺名称</view>
            <view class="info-value">{{ shopInfo.shopName }}</view>
          </view>
          <view class="info-item">
            <view class="info-label">店铺地址</view>
            <view class="info-value">{{ shopInfo.address }}</view>
          </view>
          <view class="info-item">
            <view class="info-label">联系电话</view>
            <view class="info-value">{{ shopInfo.phone }}</view>
          </view>
          <view class="info-item">
            <view class="info-label">营业时间</view>
            <view class="info-value">09:00-22:00</view>
          </view>
          <view class="info-item">
            <view class="info-label">配送费</view>
            <view class="info-value">¥{{ formattedShopInfo.formattedDeliverFee }}
            </view>
          </view>
          <view class="info-item">
            <view class="info-label">起送价</view>
            <view class="info-value">¥{{ formattedShopInfo.formattedMinFee }}
            </view>
          </view>
          <view class="info-item">
            <view class="info-label">平均送达时间</view>
            <view class="info-value">{{ shopInfo.averageSendTime }}分钟</view>
          </view>
        </view>
        <view class="shop-images" v-if="shopInfo.shopImages && shopInfo.shopImages.length > 0">
          <view class="images-title">店铺实景</view>
          <view class="images-list">
            <image v-for="(img, index) in shopInfo.shopImages" :key="index" :src="img" mode="aspectFill"
              @click="previewImage(shopInfo.shopImages, index)"></image>
          </view>
        </view>
      </view>
    </view>

    <!-- 购物车 -->
    <shop-cart v-if="activeTab === 'menu'" :cart-list="cartList" :shop-info="shopInfo"
      @update-cart="updateCart"></shop-cart>

    <!-- 套餐详情面板 -->
    <setmeal-panel :show="showSetmealPanel" :setmeal="currentSetmeal" @close="closeSetmealPanel"
      @confirm="confirmAddSetmeal"></setmeal-panel>

    <!-- 菜品详情面板 -->
    <dish-panel :show="showDishPanel" :dish="currentDish" @close="closeDishPanel" @add="confirmAddDish"></dish-panel>
  </view>
</template>

<script src="./detail.js"></script>
<style lang="scss" src="./detail.scss"></style>

<style lang="scss" scoped>
/* 评论相关样式 */
.comment-content {
  padding: 20rpx;
  background-color: #fff;
}

.comment-header.single {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 32rpx 0 18rpx 0;
  border-bottom: none;
}

.header-center {
  text-align: center;
}

.score-main {
  font-size: 52rpx;
  font-weight: bold;
  color: #2588FF;
  line-height: 1.1;
}

.score-desc {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

.review-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 18rpx 18rpx;
  padding: 18rpx 0 10rpx 0;
  margin-bottom: 10rpx;
}

.tag-item {
  padding: 10rpx 32rpx;
  font-size: 26rpx;
  color: #666;
  background-color: #f5f5f5;
  border-radius: 30rpx;
  transition: all 0.3s;
  margin: 0;
}

.tag-item.active {
  color: #fff;
  background-color: #2588FF;
  font-weight: bold;
}

.review-list {
  .review-card {
    padding: 30rpx 0;
    border-bottom: 1rpx solid #eee;

    &:last-child {
      border-bottom: none;
    }
  }
}

.review-card-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
}

.user-meta {
  flex: 1;
}

.user-name {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 6rpx;
}

.review-time {
  font-size: 24rpx;
  color: #999;
}

.review-content {
  margin: 20rpx 0;
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  margin: 20rpx 0;

  image {
    width: 200rpx;
    height: 200rpx;
    margin-right: 20rpx;
    margin-bottom: 20rpx;
    border-radius: 8rpx;
  }
}

.review-replies {
  background-color: #f8f8f8;
  padding: 20rpx;
  border-radius: 8rpx;
  margin-top: 20rpx;
}

.reply-item {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;

  .reply-label {
    color: #ff6b6b;
    margin-right: 10rpx;
  }

  .reply-content {
    display: block;
  }
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30rpx 0;
  color: #999;
  font-size: 26rpx;

  .uni-icons {
    margin-right: 10rpx;
  }
}

.load-complete {
  text-align: center;
  padding: 30rpx 0;
  color: #999;
  font-size: 26rpx;
}

.empty-state {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
  font-size: 28rpx;
}

/* 打烊中提示栏 */
.shop-closed-bar {
  margin-top: 16rpx;
  padding: 16rpx 24rpx;
  background: #FFF3E0;
  border-left: 6rpx solid #FF9800;
  border-radius: 8rpx;
}

.closed-text {
  font-size: 26rpx;
  color: #E65100;
  font-weight: 500;
}
</style>