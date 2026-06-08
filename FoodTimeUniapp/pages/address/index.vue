<template>
  <view class="address-container">
    <view class="address-list" v-if="addressList.length > 0">
      <view class="address-item" v-for="(item, index) in addressList" :key="index" @click="selectAddress(item)">
        <view class="address-info">
          <view class="address-header">
            <text class="name">{{ item.consignee }}</text>
            <text class="phone">{{ item.phone }}</text>
            <text class="tag" v-if="item.label">{{ item.label }}</text>
            <text class="default-tag" v-if="item.isDefault === 1">默认</text>
          </view>
          <view class="address-detail">{{ getFullAddress(item) }}</view>
        </view>
        <view class="address-actions">
          <view class="action-btn edit" @click.stop="editAddress(item)">
            <uni-icons type="compose" size="18" color="#666"></uni-icons>
          </view>
          <view class="action-btn delete" @click.stop="onDeleteAddress(item)">
            <uni-icons type="trash" size="18" color="#666"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <view class="empty-state" v-else>
      <image src="/static/image/user/empty-address.png" mode="aspectFit" class="empty-image"></image>
      <text class="empty-text">暂无收货地址</text>
    </view>

    <view class="add-address-btn" @click="addNewAddress">
      <text class="btn-text">新增收货地址</text>
    </view>
  </view>
</template>

<script>
import { addressApi } from '@/api/index';

export default {
  data() {
    return {
      addressList: [],
      isLoading: false
    };
  },
  onShow() {
    this.loadAddressList();
  },
  onLoad() {
    // 监听地址更新事件
    uni.$on('address_updated', this.loadAddressList);
  },
  onUnload() {
    // 移除监听
    uni.$off('address_updated', this.loadAddressList);
  },
  methods: {
    async loadAddressList() {
      this.isLoading = true;
      try {
        const res = await addressApi.getAddressList();
        if (res.code === 1) {
          this.addressList = res.data || [];
        } else {
          uni.showToast({
            title: res.msg || '获取地址列表失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('获取地址列表出错', error);
        uni.showToast({
          title: '网络异常，请稍后重试',
          icon: 'none'
        });
      } finally {
        this.isLoading = false;
      }
    },
    selectAddress(address) {
      // 如果是从订单页面跳转过来选择地址，则返回地址信息
      const pages = getCurrentPages();
      const prevPage = pages[pages.length - 2];

      if (prevPage && prevPage.route.includes('order')) {
        // 将选中的地址传回上一页
        uni.$emit('addressSelected', address);
        uni.navigateBack();
      }
    },
    addNewAddress() {
      uni.navigateTo({
        url: '/pages/address/add'
      });
    },
    editAddress(address) {
      uni.navigateTo({
        url: `/pages/address/edit?id=${address.id}`
      });
    },
    async onDeleteAddress(address) {
      uni.showModal({
        title: '提示',
        content: '确定要删除该地址吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await addressApi.deleteAddress(address.id);
              if (result.code === 1) {
                uni.showToast({
                  title: '删除成功',
                  icon: 'success'
                });
                this.loadAddressList();
              } else {
                uni.showToast({
                  title: result.msg || '删除失败',
                  icon: 'none'
                });
              }
            } catch (error) {
              console.error('删除地址出错', error);
              uni.showToast({
                title: '网络异常，请稍后重试',
                icon: 'none'
              });
            }
          }
        }
      });
    },
    // 获取完整地址
    getFullAddress(address) {
      const { provinceName, cityName, districtName, detail } = address;
      let fullAddress = '';

      if (provinceName) fullAddress += provinceName;
      if (cityName) fullAddress += cityName;
      if (districtName) fullAddress += districtName;
      if (detail) fullAddress += ' ' + detail;

      return fullAddress;
    }
  }
};
</script>

<style lang="scss">
.address-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;

  .address-list {
    padding: 20rpx;

    .address-item {
      display: flex;
      justify-content: space-between;
      background-color: #fff;
      border-radius: 12rpx;
      padding: 30rpx;
      margin-bottom: 20rpx;
      box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

      .address-info {
        flex: 1;
        padding-right: 20rpx;

        .address-header {
          display: flex;
          align-items: center;
          margin-bottom: 10rpx;

          .name {
            font-size: 30rpx;
            font-weight: 500;
            color: #333;
            margin-right: 20rpx;
          }

          .phone {
            font-size: 28rpx;
            color: #666;
            margin-right: 20rpx;
          }

          .tag {
            font-size: 22rpx;
            color: #2588FF;
            background-color: rgba(37, 136, 255, 0.1);
            padding: 4rpx 12rpx;
            border-radius: 6rpx;
            margin-right: 10rpx;
          }

          .default-tag {
            font-size: 22rpx;
            color: #ff6b6b;
            background-color: rgba(255, 107, 107, 0.1);
            padding: 4rpx 12rpx;
            border-radius: 6rpx;
          }
        }

        .address-detail {
          font-size: 28rpx;
          color: #666;
          line-height: 1.4;
        }
      }

      .address-actions {
        display: flex;
        flex-direction: column;
        justify-content: center;

        .action-btn {
          width: 60rpx;
          height: 60rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-bottom: 10rpx;

          &:last-child {
            margin-bottom: 0;
          }
        }
      }
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding-top: 200rpx;

    .empty-image {
      width: 200rpx;
      height: 200rpx;
      margin-bottom: 30rpx;
    }

    .empty-text {
      font-size: 28rpx;
      color: #999;
    }
  }

  .add-address-btn {
    position: fixed;
    left: 30rpx;
    right: 30rpx;
    bottom: 30rpx;
    height: 90rpx;
    background-color: #2588FF;
    color: #fff;
    border-radius: 45rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4rpx 16rpx rgba(37, 136, 255, 0.3);

    .btn-text {
      font-size: 30rpx;
      font-weight: 500;
    }
  }
}
</style>