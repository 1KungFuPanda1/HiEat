<template>
	<view class="content">
		<view class="head">
			<view class="search">
				<uni-search-bar placeholder="搜索你想要的东西" bgColor="#fff" @focus="goToSearch" @click="goToSearch"
					cancelButton='none' radius="40" disabled />
			</view>
		</view>
		<view class="main">
			<view class="kingkongArea">
				<view class="kingkong-container">
					<view class="kingkongItem" v-for="(item, index) in categoryList" :key="index"
						@click="navigateToCategory(item)">
						<view class="icon">
							<image :src="item.image" mode="aspectFill"></image>
						</view>
						<view class="text">
							{{ item.name }}
						</view>
					</view>
				</view>
			</view>
			<view class="notice">
				<view class="title">公告</view>
				<view class="notice-content">
					<swiper class="notice-swiper" vertical :autoplay="true" :interval="3000" :duration="1000" :circular="true">
						<swiper-item v-for="(item, index) in noticeList" :key="index">
							<view class="noticeText">{{ item.content }}</view>
						</swiper-item>
					</swiper>
				</view>
			</view>
			<view class="swipper-container">
				<swiper indicator-dots :autoplay="true" :interval="4000" :duration="1000" :circular="true">
					<swiper-item v-for="(item, index) in swiperList" :key="index" @click="navigateToShop(item)">
						<image :src="item.imageUrl" mode="aspectFill"></image>
					</swiper-item>
				</swiper>
			</view>
			<view>
				<shop-list ref="shopListRef"></shop-list>
			</view>
		</view>
	</view>
</template>

<!--<shop-list ref="shopListRef"></shop-list>这里的ref是给后续用户下拉准备的刷新，这里的
逻辑是，渲染到<shop-list>就会自动进入子组件，渲染子组件在子组件中onMounted加载商家列表-->

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import ShopList from '@/components/ShopList.vue'
import { shopApi, commonApi } from '@/api/index';

// 轮播图数据
const swiperList = ref([]);

// 公告列表数据
const noticeList = ref([]);

// 店铺分类列表
const categoryList = ref([]);

/**
 * 获取店铺分类列表
 */
const fetchCategories = async () => {
	try {
		const res = await shopApi.getShopCategories();
		if (res && res.code === 1 && res.data) {
			categoryList.value = res.data;
		} else {
			console.error('获取分类列表失败:', res);
		}
	} catch (error) {
		console.error('获取分类列表异常:', error);
	}
};

/**
 * 获取公告列表
 */
const fetchNotices = async () => {
	try {
		const res = await commonApi.getNotices();
		if (res && res.code === 1 && res.data) {
			noticeList.value = res.data;
		} else {
			console.error('获取公告列表失败:', res);
		}
	} catch (error) {
		console.error('获取公告列表异常:', error);
	}
};

/**
 * 获取轮播图列表
 */
const fetchCarousels = async () => {
	try {
		const res = await commonApi.getCarousels();
		if (res && res.code === 1 && res.data) {
			swiperList.value = res.data;
		} else {
			console.error('获取轮播图列表失败:', res);
			// 如果接口失败，使用默认图片
			swiperList.value = [
				{
					imageUrl: "/static/image/index/index-slider.png",
					shopId: null
				},
				{
					imageUrl: "/static/image/index/生成宣传轮播图 (7).png",
					shopId: null
				},
				{
					imageUrl: "/static/image/index/生成宣传轮播图 (8).png",
					shopId: null
				}
			];
		}
	} catch (error) {
		console.error('获取轮播图列表异常:', error);
		// 异常时使用默认图片
		swiperList.value = [
			{
				imageUrl: "/static/image/index/index-slider.png",
				shopId: null
			},
			{
				imageUrl: "/static/image/index/生成宣传轮播图 (7).png",
				shopId: null
			},
			{
				imageUrl: "/static/image/index/生成宣传轮播图 (8).png",
				shopId: null
			}
		];
	}
};

/**
 * 跳转到分类店铺列表
 * @param {Object} category - 分类信息
 */
const navigateToCategory = (category) => {
	uni.navigateTo({
		url: `/pages/shop/categoryShop?categoryId=${category.id}&categoryName=${encodeURIComponent(category.name)}`
	});
};

/**
 * 跳转到店铺详情
 * @param {Object} item - 轮播图项
 */
const navigateToShop = (item) => {
	if (item.shopId) {
		uni.navigateTo({
			url: `/pages/shop/detail?id=${item.shopId}`
		});
	}
};

/**
 * 跳转到搜索页面
 */
const goToSearch = () => {
	uni.navigateTo({
		url: '/pages/search/index'
	});
};

// 获取子组件实例
const shopListRef = ref(null);

// 下拉刷新
const onPullDownRefresh = () => {
	// 判断子组件实例存在，调用子组件的 refreshShopList 方法
	if (shopListRef.value && shopListRef.value.refreshShopList) {
		shopListRef.value.refreshShopList();
	}

	// 刷新分类列表
	fetchCategories();

	// 刷新公告列表
	fetchNotices();

	// 刷新轮播图列表
	fetchCarousels();

	// 停止下拉刷新
	setTimeout(() => {
		uni.stopPullDownRefresh();
	}, 1000);
}

// 页面加载时获取数据
onMounted(() => {
	fetchCategories();
	fetchNotices();
	fetchCarousels();
});
</script>

<style lang="scss">
/* 定义变量 */
$swiper-width: 688rpx;
$swiper-height: 182rpx;
$border-radius: 8rpx;
$shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
$text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.5);
$title-bg: rgba(0, 0, 0, 0.3);

.content {
	.head {
		.search {
			width: 100%;
			background-color: #2588FF;
			padding-bottom: 40rpx;
		}
	}

	.main {
		background-color: #fff;
		height: 100%;
		margin-top: -40rpx;
		border-radius: 40rpx;
		padding: 40rpx 32rpx 0 32rpx;

		.kingkongArea {
			width: 100%;
			height: auto;
			margin-bottom: 20rpx;

			.kingkong-container {
				display: flex;
				flex-wrap: wrap;
				justify-content: flex-start;

				.kingkongItem {
					width: calc(20% - 16rpx);
					margin: 0 8rpx 20rpx 8rpx;
					display: flex;
					flex-direction: column;
					align-items: center;
					text-align: center;

					.icon {
						width: 96rpx;
						height: 96rpx;
						margin: 0 auto 10rpx;

						image {
							width: 100%;
							height: 100%;
							border-radius: 325rpx;
						}
					}

					.text {
						font-size: 24rpx;
						color: #333;
						overflow: hidden;
						text-overflow: ellipsis;
						white-space: nowrap;
						width: 100%;
					}
				}
			}
		}

		.notice {
			display: flex;
			width: 686rpx;
			height: 56rpx;
			background-color: #F8F8F8;
			border-radius: 28rpx;
			margin: 20rpx 0;
			padding: 0 36rpx;
			box-sizing: border-box;
			align-items: center;

			.title {
				font-size: 24rpx;
				color: #2588FF;
				margin-right: 20rpx;
			}

			.notice-content {
				flex: 1;
				height: 100%;
				overflow: hidden;

				.notice-swiper {
					height: 100%;
					width: 100%;
				}

				.noticeText {
					font-size: 24rpx;
					line-height: 56rpx;
					white-space: nowrap;
					overflow: hidden;
					text-overflow: ellipsis;
				}
			}
		}

		.swipper-container {
			width: 688rpx;
			height: 182rpx;
			border-radius: 16rpx;
			overflow: hidden;

			swiper {
				width: 688rpx;
				height: 182rpx;

				swiper-item {
					width: 100%;
					height: 100%;

					image {
						height: 100%;
						width: 100%;
					}
				}
			}
		}
	}
}
</style>
