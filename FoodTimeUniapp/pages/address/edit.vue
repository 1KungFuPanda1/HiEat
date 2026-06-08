<template>
  <view class="address-edit-container">
    <view class="form-section">
      <view class="form-item">
        <text class="form-label">联系人</text>
        <input class="form-input" type="text" v-model="addressForm.consignee" placeholder="请输入收货人姓名" />
      </view>

      <view class="form-item">
        <text class="form-label">性别</text>
        <view class="gender-group">
          <view class="gender-item" :class="{ active: addressForm.sex === '1' }" @click="addressForm.sex = '1'">
            <view class="gender-radio"></view>
            <text>先生</text>
          </view>
          <view class="gender-item" :class="{ active: addressForm.sex === '0' }" @click="addressForm.sex = '0'">
            <view class="gender-radio"></view>
            <text>女士</text>
          </view>
        </view>
      </view>

      <view class="form-item">
        <text class="form-label">手机号</text>
        <input class="form-input" type="number" v-model="addressForm.phone" placeholder="请输入手机号码" maxlength="11" />
      </view>

      <view class="form-item">
        <text class="form-label">所在地区</text>
        <view class="form-input area-picker" @click="showAreaSelector()">
          <text :class="{ 'placeholder': !areaSelected }">{{ areaText || '请选择所在地区' }}</text>
          <uni-icons type="right" size="16" color="#ccc"></uni-icons>
        </view>
      </view>

      <view class="form-item">
        <text class="form-label">详细地址</text>
        <textarea class="form-textarea" v-model="addressForm.detail" placeholder="请输入详细地址信息" />
      </view>

      <view class="form-item">
        <text class="form-label">标签</text>
        <view class="tag-list">
          <view class="tag-item" v-for="tag in tags" :key="tag" :class="{ 'active': addressForm.label === tag }"
            @click="addressForm.label = addressForm.label === tag ? '' : tag">
            {{ tag }}
          </view>
        </view>
      </view>

      <view class="form-item switch-item">
        <text class="form-label">设为默认地址</text>
        <switch :checked="addressForm.isDefault === 1" @change="handleDefaultChange" color="#2588FF" />
      </view>
    </view>

    <view class="save-btn" @click="saveAddress">保存</view>

    <!-- 省市区选择器 -->
    <uni-popup ref="areaPopup" type="bottom" @change="handlePopupChange">
      <view class="area-popup" v-if="showAreaPicker">
        <view class="area-header">
          <text class="cancel-btn" @click="cancelAreaPicker">取消</text>
          <text class="title">选择地区</text>
          <text class="confirm-btn" @click="confirmAreaPicker">确定</text>
        </view>
        <view class="area-tabs">
          <view class="tab" :class="{ active: currentTab === 0 }" @click="switchTab(0)">
            {{ tempProvince.areaName || '请选择' }}
          </view>
          <view class="tab" :class="{ active: currentTab === 1, disabled: !tempProvince.areaName }"
            @click="tempProvince.areaName && switchTab(1)">
            {{ tempCity.areaName || '请选择' }}
          </view>
          <view class="tab" :class="{ active: currentTab === 2, disabled: !tempCity.areaName }"
            @click="tempCity.areaName && switchTab(2)">
            {{ tempDistrict.areaName || '请选择' }}
          </view>
        </view>
        <scroll-view class="area-content" scroll-y>
          <view class="area-list">
            <view class="area-item" v-for="item in currentAreaList" :key="item.areaId" @click="selectArea(item)">
              <text>{{ item.areaName }}</text>
              <uni-icons v-if="isSelected(item)" type="checkmarkempty" size="24" color="#2588FF"></uni-icons>
            </view>
          </view>
        </scroll-view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import { addressApi } from '@/api/index';

export default {
  data() {
    return {
      addressId: null,
      addressForm: {
        id: null,
        consignee: '', // 收货人
        sex: '1', // 性别：1-男，0-女
        phone: '', // 手机号
        provinceCode: '', // 省编码
        provinceName: '', // 省名称
        cityCode: '', // 市编码
        cityName: '', // 市名称
        districtCode: '', // 区编码
        districtName: '', // 区名称
        detail: '', // 详细地址
        label: '', // 标签
        isDefault: 0 // 是否默认：0-否，1-是
      },
      tags: ['家', '公司', '学校'],

      // 省市区选择器
      showAreaPicker: false,
      areaList: [], // 省市区数据
      currentTab: 0, // 当前选择的级别：0-省，1-市，2-区
      selectedProvince: {}, // 已选择的省
      selectedCity: {}, // 已选择的市
      selectedDistrict: {}, // 已选择的区

      // 临时选择的地区（确认前不更新表单）
      tempProvince: {},
      tempCity: {},
      tempDistrict: {}
    };
  },

  computed: {
    // 当前显示的地区列表
    currentAreaList() {
      if (this.currentTab === 0) {
        return this.areaList;
      } else if (this.currentTab === 1) {
        // 使用临时选择的省级数据来获取市级列表
        return this.tempProvince.children || [];
      } else {
        // 使用临时选择的市级数据来获取区级列表
        return this.tempCity.children || [];
      }
    },

    // 是否已选择完整地区
    areaSelected() {
      return this.addressForm.provinceName && this.addressForm.cityName && this.addressForm.districtName;
    },

    // 地区文本展示
    areaText() {
      if (this.areaSelected) {
        return `${this.addressForm.provinceName} ${this.addressForm.cityName} ${this.addressForm.districtName}`;
      }
      return '';
    }
  },

  onLoad(options) {
    // 获取省市区数据
    this.getAreaData();

    if (options.id) {
      this.addressId = options.id;
      this.loadAddressDetail();
    }
  },

  methods: {
    // 获取省市区数据
    async getAreaData() {
      try {
        uni.showLoading({
          title: '加载中...'
        });

        const res = await addressApi.getAreaTree();

        if (res.code === 1 && res.data) {
          // 检查数据有效性
          if (Array.isArray(res.data) && res.data.length > 0) {
            this.areaList = res.data;
          } else {
            console.error('地区数据无效:', res.data);
            uni.showToast({
              title: '地区数据格式错误',
              icon: 'none'
            });
          }
        } else {
          console.error('获取地区数据失败:', res);
          uni.showToast({
            title: res.msg || '获取地区数据失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('获取地区数据失败:', error);
        uni.showToast({
          title: '获取地区数据失败',
          icon: 'none'
        });
      } finally {
        uni.hideLoading();
      }
    },

    // 加载地址详情
    async loadAddressDetail() {
      try {
        uni.showLoading({
          title: '加载中...'
        });

        const res = await addressApi.getAddressList();
        if (res.code === 1 && res.data) {
          // 查找指定ID的地址
          const address = res.data.find(item => item.id == this.addressId);

          if (address) {
            // 填充表单数据
            this.addressForm = { ...address };

            // 设置已选择的省市区
            if (address.provinceName && address.provinceCode) {
              this.selectedProvince = {
                areaId: address.provinceCode,
                areaName: address.provinceName
              };
            }

            if (address.cityName && address.cityCode) {
              this.selectedCity = {
                areaId: address.cityCode,
                areaName: address.cityName
              };
            }

            if (address.districtName && address.districtCode) {
              this.selectedDistrict = {
                areaId: address.districtCode,
                areaName: address.districtName
              };
            }
          } else {
            uni.showToast({
              title: '地址不存在',
              icon: 'none'
            });
            setTimeout(() => {
              uni.navigateBack();
            }, 1500);
          }
        } else {
          uni.showToast({
            title: res.msg || '获取地址详情失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('获取地址详情失败:', error);
        uni.showToast({
          title: '获取地址详情失败',
          icon: 'none'
        });
      } finally {
        uni.hideLoading();
      }
    },

    // 处理默认地址开关切换
    handleDefaultChange(e) {
      this.addressForm.isDefault = e.detail.value ? 1 : 0;
    },

    // 处理弹窗状态变化
    handlePopupChange(e) {
      if (!e.show) {
        this.showAreaPicker = false;
      }
    },

    // 显示地区选择器
    showAreaSelector() {
      // 初始化临时选择状态为当前已选择的状态
      this.tempProvince = JSON.parse(JSON.stringify(this.selectedProvince || {}));
      this.tempCity = JSON.parse(JSON.stringify(this.selectedCity || {}));
      this.tempDistrict = JSON.parse(JSON.stringify(this.selectedDistrict || {}));

      this.showAreaPicker = true;
      this.$refs.areaPopup.open();
    },

    // 切换地区选择标签页
    switchTab(index) {
      if ((index === 1 && !this.tempProvince.areaName) ||
        (index === 2 && !this.tempCity.areaName)) {
        return;
      }
      this.currentTab = index;
    },

    // 选择地区
    selectArea(item) {
      if (this.currentTab === 0) {
        // 选择省
        this.tempProvince = JSON.parse(JSON.stringify(item)); // 深拷贝防止引用问题
        this.tempCity = {};
        this.tempDistrict = {};

        // 确保tempProvince有children再切换到市级
        if (this.tempProvince.children && this.tempProvince.children.length > 0) {
          this.currentTab = 1; // 自动切换到市级选择
        } else {
          console.error('省级数据没有子级数据');
          uni.showToast({
            title: '地区数据异常',
            icon: 'none'
          });
        }
      } else if (this.currentTab === 1) {
        // 选择市
        this.tempCity = JSON.parse(JSON.stringify(item)); // 深拷贝防止引用问题
        this.tempDistrict = {};

        // 确保tempCity有children再切换到区级
        if (this.tempCity.children && this.tempCity.children.length > 0) {
          this.currentTab = 2; // 自动切换到区级选择
        } else {
          console.error('市级数据没有子级数据');
          uni.showToast({
            title: '地区数据异常',
            icon: 'none'
          });
        }
      } else {
        // 选择区
        this.tempDistrict = item;
        // 选完区后自动关闭并确认
        this.confirmAreaPicker();
      }
    },

    // 判断当前项是否已选中
    isSelected(item) {
      if (this.currentTab === 0) {
        return this.tempProvince.areaId === item.areaId;
      } else if (this.currentTab === 1) {
        return this.tempCity.areaId === item.areaId;
      } else {
        return this.tempDistrict.areaId === item.areaId;
      }
    },

    // 取消地区选择
    cancelAreaPicker() {
      this.showAreaPicker = false;
      this.$refs.areaPopup.close();
    },

    // 确认地区选择
    confirmAreaPicker() {
      // 确保已选择完整地区
      if (this.tempProvince.areaId && this.tempCity.areaId && this.tempDistrict.areaId) {
        try {
          // 更新已选择的省市区
          this.selectedProvince = JSON.parse(JSON.stringify(this.tempProvince));
          this.selectedCity = JSON.parse(JSON.stringify(this.tempCity));
          this.selectedDistrict = JSON.parse(JSON.stringify(this.tempDistrict));

          // 更新表单数据
          this.addressForm.provinceCode = String(this.selectedProvince.areaId);
          this.addressForm.provinceName = this.selectedProvince.areaName;
          this.addressForm.cityCode = String(this.selectedCity.areaId);
          this.addressForm.cityName = this.selectedCity.areaName;
          this.addressForm.districtCode = String(this.selectedDistrict.areaId);
          this.addressForm.districtName = this.selectedDistrict.areaName;

          console.log('已选择地区:', this.addressForm.provinceName, this.addressForm.cityName, this.addressForm.districtName);

          // 关闭选择器
          this.showAreaPicker = false;
          this.$refs.areaPopup.close();
        } catch (error) {
          console.error('确认地区选择错误:', error);
          uni.showToast({
            title: '地区选择错误',
            icon: 'none'
          });
        }
      } else {
        uni.showToast({
          title: '请选择完整的省市区',
          icon: 'none'
        });
      }
    },

    // 表单验证
    validateForm() {
      if (!this.addressForm.consignee) {
        uni.showToast({
          title: '请输入收货人姓名',
          icon: 'none'
        });
        return false;
      }

      if (!this.addressForm.phone) {
        uni.showToast({
          title: '请输入手机号',
          icon: 'none'
        });
        return false;
      }

      // 简单的手机号验证
      if (!/^1\d{10}$/.test(this.addressForm.phone)) {
        uni.showToast({
          title: '请输入正确的手机号',
          icon: 'none'
        });
        return false;
      }

      if (!this.areaSelected) {
        uni.showToast({
          title: '请选择所在地区',
          icon: 'none'
        });
        return false;
      }

      if (!this.addressForm.detail) {
        uni.showToast({
          title: '请输入详细地址',
          icon: 'none'
        });
        return false;
      }

      return true;
    },

    // 保存地址
    async saveAddress() {
      if (!this.validateForm()) {
        return;
      }

      try {
        uni.showLoading({
          title: '保存中...'
        });

        // 构建提交的地址数据，确保包含id
        const addressData = {
          ...this.addressForm,
          id: Number(this.addressId) // 确保id是数字类型
        };

        console.log('提交的地址数据:', addressData);

        // 调用修改地址API
        const res = await addressApi.updateAddress(addressData);

        if (res.code === 1) {
          uni.showToast({
            title: '修改成功',
            icon: 'success'
          });

          // 返回上一页并刷新地址列表
          setTimeout(() => {
            // 发送事件通知地址列表刷新
            uni.$emit('address_updated');
            uni.navigateBack();
          }, 1500);
        } else {
          uni.showToast({
            title: res.msg || '修改失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('保存地址失败:', error);
        uni.showToast({
          title: '修改失败，请重试',
          icon: 'none'
        });
      } finally {
        uni.hideLoading();
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.address-edit-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
}

.form-section {
  background-color: #fff;
  margin-top: 20rpx;
}

.form-item {
  padding: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }
}

.form-label {
  display: block;
  margin-bottom: 20rpx;
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.form-input {
  width: 100%;
  font-size: 28rpx;
  color: #333;

  &.area-picker {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.form-textarea {
  width: 100%;
  height: 180rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
}

.placeholder {
  color: #999;
}

// 性别选择
.gender-group {
  display: flex;
  align-items: center;
}

.gender-item {
  display: flex;
  align-items: center;
  margin-right: 60rpx;
  font-size: 28rpx;
  color: #333;

  .gender-radio {
    width: 40rpx;
    height: 40rpx;
    border-radius: 50%;
    border: 1rpx solid #ddd;
    margin-right: 10rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    &::after {
      content: '';
      display: none;
      width: 24rpx;
      height: 24rpx;
      border-radius: 50%;
      background-color: #2588FF;
    }
  }

  &.active {
    color: #2588FF;

    .gender-radio {
      border-color: #2588FF;

      &::after {
        display: block;
      }
    }
  }
}

// 标签选择
.tag-list {
  display: flex;
  flex-wrap: wrap;
}

.tag-item {
  padding: 12rpx 30rpx;
  background-color: #f7f7f7;
  border-radius: 30rpx;
  margin-right: 20rpx;
  margin-bottom: 20rpx;
  font-size: 26rpx;
  color: #666;

  &.active {
    background-color: #e6f1ff;
    color: #2588FF;
  }
}

// 默认地址开关
.switch-item {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .form-label {
    margin-bottom: 0;
  }
}

// 保存按钮
.save-btn {
  position: fixed;
  left: 30rpx;
  right: 30rpx;
  bottom: 30rpx;
  height: 90rpx;
  background-color: #2588FF;
  color: #fff;
  border-radius: 45rpx;
  font-size: 30rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(37, 136, 255, 0.3);
}

// 省市区选择器弹窗
.area-popup {
  background-color: #fff;
  border-top-left-radius: 20rpx;
  border-top-right-radius: 20rpx;
  overflow: hidden;
  height: 800rpx;
  display: flex;
  flex-direction: column;
}

.area-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;

  .title {
    font-size: 32rpx;
    color: #333;
    font-weight: 500;
  }

  .cancel-btn {
    font-size: 28rpx;
    color: #999;
  }

  .confirm-btn {
    font-size: 28rpx;
    color: #2588FF;
    font-weight: 500;
  }
}

.area-tabs {
  display: flex;
  align-items: center;
  padding: 0 30rpx;
  border-bottom: 1rpx solid #f5f5f5;

  .tab {
    padding: 20rpx 10rpx;
    margin-right: 40rpx;
    position: relative;
    font-size: 28rpx;
    color: #333;
    max-width: 200rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    &.active {
      color: #2588FF;
      font-weight: 500;

      &::after {
        content: '';
        position: absolute;
        left: 0;
        right: 0;
        bottom: -1rpx;
        height: 4rpx;
        background-color: #2588FF;
        border-radius: 2rpx;
      }
    }

    &.disabled {
      color: #ccc;
    }
  }
}

.area-content {
  flex: 1;
  height: 0;
}

.area-list {
  padding: 0 30rpx;
}

.area-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 0;
  font-size: 28rpx;
  color: #333;
  border-bottom: 1rpx solid #f8f8f8;
}
</style>