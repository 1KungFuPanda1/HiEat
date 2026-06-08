<template>
  <view class="address-add">
    <view class="form-container">
      <!-- 表单内容 -->
      <view class="form-group">
        <view class="form-item">
          <view class="form-label">联系人</view>
          <input type="text" v-model="addressForm.consignee" placeholder="请填写收货人姓名" class="form-input" />
        </view>
        <view class="form-item">
          <view class="form-label">性别</view>
          <view class="form-radio-group">
            <view class="form-radio" :class="{ active: addressForm.sex === '1' }" @click="addressForm.sex = '1'">
              <view class="radio-circle"></view>
              <text>先生</text>
            </view>
            <view class="form-radio" :class="{ active: addressForm.sex === '0' }" @click="addressForm.sex = '0'">
              <view class="radio-circle"></view>
              <text>女士</text>
            </view>
          </view>
        </view>
        <view class="form-item">
          <view class="form-label">手机号</view>
          <input type="number" v-model="addressForm.phone" placeholder="请填写收货人手机号" class="form-input" maxlength="11" />
        </view>
      </view>

      <view class="form-group">
        <view class="form-item area-picker" @click="showAreaSelector()">
          <view class="form-label">所在地区</view>
          <view class="area-value" :class="{ 'area-placeholder': !areaSelected }">
            {{ areaText || '请选择收货地址' }}
            <uni-icons type="right" size="18" color="#999"></uni-icons>
          </view>
        </view>
        <view class="form-item">
          <view class="form-label">详细地址</view>
          <input type="text" v-model="addressForm.detail" placeholder="请填写详细地址（如街道、小区、门牌号等）" class="form-input" />
        </view>
      </view>

      <view class="form-group">
        <view class="form-item">
          <view class="form-label">标签</view>
          <view class="tag-group">
            <view class="tag-item" :class="{ active: addressForm.label === '家' }" @click="addressForm.label = '家'">
              家
            </view>
            <view class="tag-item" :class="{ active: addressForm.label === '公司' }" @click="addressForm.label = '公司'">
              公司
            </view>
            <view class="tag-item" :class="{ active: addressForm.label === '学校' }" @click="addressForm.label = '学校'">
              学校
            </view>
          </view>
        </view>
      </view>

      <view class="form-group">
        <view class="form-item">
          <view class="default-switch">
            <text>设为默认地址</text>
            <switch :checked="addressForm.isDefault === 1" @change="handleDefaultChange" color="#2588FF" />
          </view>
        </view>
      </view>
    </view>

    <!-- 底部保存按钮 -->
    <view class="bottom-button">
      <button class="save-btn" @click="saveAddress">保存</button>
    </view>

    <!-- 省市区选择器 -->
	 <!-- ====================== 【核心：uni-popup 底部弹出 省市区选择器】 ====================== -->
    <uni-popup ref="areaPopup" type="bottom" @change="handlePopupChange">
           <!-- 整个选择器内容 -->
	  <view class="area-popup" v-if="showAreaPicker">
       <!-- 1. 头部：取消 + 标题 + 确定 -->
		<view class="area-header">
          <text class="cancel-btn" @click="cancelAreaPicker">取消</text>
          <text class="title">选择地区</text>
          <text class="confirm-btn" @click="confirmAreaPicker">确定</text>
        </view>
        <!-- 2. 选项卡：省 → 市 → 区 -->
		<view class="area-tabs">
			 <!-- 省 Tab -->
          <view class="tab" :class="{ active: currentTab === 0 }" @click="switchTab(0)">
            {{ tempProvince.areaName || '请选择' }}
          </view>
		   <!-- 市 Tab：必须选完省才能点 -->
          <view class="tab" :class="{ active: currentTab === 1, disabled: !tempProvince.areaName }"
            @click="tempProvince.areaName && switchTab(1)">
            {{ tempCity.areaName || '请选择' }}
          </view>
		  <!-- 区 Tab：必须选完市才能点 -->
          <view class="tab" :class="{ active: currentTab === 2, disabled: !tempCity.areaName }"
            @click="tempCity.areaName && switchTab(2)">
            {{ tempDistrict.areaName || '请选择' }}
          </view>
        </view>
		 <!-- 3. 内容区域：滚动显示 当前级别的列表（省列表 / 市列表 / 区列表） -->
        <scroll-view class="area-content" scroll-y>
          <view class="area-list">
			  <!-- 循环渲染当前列表 -->
            <view class="area-item" v-for="item in currentAreaList" :key="item.areaId" @click="selectArea(item)">
              <text>{{ item.areaName }}</text>
			   <!-- 选中了就显示 √ 图标 -->
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
      // 地址表单
      addressForm: {
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

      // 省市区选择器
      showAreaPicker: false,
      areaList: [], // 省市区数据
      currentTab: 0, // 当前选择的级别：0-省，1-市，2-区
	  
	   // 已确认的选择
      selectedProvince: {}, // 已选择的省
      selectedCity: {}, // 已选择的市
      selectedDistrict: {}, // 已选择的区

       // 临时选择状态（确认前使用）
      tempProvince: {},
      tempCity: {},
      tempDistrict: {}
    }
  },

  computed: {
    // computed 是 Vue 里的计算属性，
    // 用来基于现有数据派生新数据，自带缓存，依赖不变就不重新计算，比 methods 高效。
    
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

  onLoad() {
    // 获取省市区数据
    this.getAreaData();
  },

  onReady() {
    // 注册调试辅助函数
    if (process.env.NODE_ENV !== 'production') {
      uni.$on('debug_address', () => {
        console.log('===== 地址选择器调试信息 =====');
        console.log('当前选择Tab:', this.currentTab);
        console.log('临时省份:', this.tempProvince);
        console.log('临时城市:', this.tempCity);
        console.log('临时区县:', this.tempDistrict);
        console.log('已选省份:', this.selectedProvince);
        console.log('已选城市:', this.selectedCity);
        console.log('已选区县:', this.selectedDistrict);
        console.log('当前地区列表:', this.currentAreaList);
        console.log('===========================');
      });
    }
  },

  onUnload() {
    // 移除调试事件监听
    if (process.env.NODE_ENV !== 'production') {
      uni.$off('debug_address');
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
          // 检查数据有效性，确保结构符合省市区三级
          const validData = Array.isArray(res.data) && res.data.length > 0;

          if (validData) {
            // 打印数据结构，方便调试
            console.log('地区数据结构:', JSON.stringify(res.data[0], null, 2).substring(0, 200) + '...');

            this.areaList = res.data;

            // 检查数据是否符合预期的结构
            const hasValidStructure = this.areaList.some(province =>
              // 1. 当前省份必须有 children（必须有下级：市）
               // 2. children 必须是数组
               // 3. 数组不能为空
			  province.children &&
			  Array.isArray(province.children) &&
			   province.children.length > 0 &&
			   
			// 4. 省份下面的【市】里面，必须至少有一个市 有 children（区）
              province.children.some(city => 
			  city.children && Array.isArray(city.children))
            );

            if (!hasValidStructure) {
              console.warn('警告: 地区数据结构可能不完整，三级联动可能无法正常工作');
            }
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
		// 2. 显示选择器界面
      this.showAreaPicker = true;
	   // 3. 打开 uni-popup 弹窗
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

        const res = await addressApi.addAddress(this.addressForm);

        if (res.code === 1) {
          uni.showToast({
            title: '保存成功',
            icon: 'success'
          });

          // 返回上一页
          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        } else {
          uni.showToast({
            title: res.msg || '保存失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('保存地址失败:', error);
        uni.showToast({
          title: '保存失败，请重试',
          icon: 'none'
        });
      } finally {
        uni.hideLoading();
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.address-add {
  min-height: 100vh;
  background-color: #f7f7f7;
  padding-bottom: 120rpx;
}

.form-container {
  padding: 20rpx;
}

.form-group {
  margin-bottom: 20rpx;
  border-radius: 12rpx;
  background-color: #fff;
  padding: 0 30rpx;
}

.form-item {
  position: relative;
  padding: 30rpx 0;

  &:not(:last-child) {
    border-bottom: 1rpx solid #f5f5f5;
  }
}

.form-label {
  margin-bottom: 20rpx;
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.form-input {
  width: 100%;
  font-size: 28rpx;
  color: #333;
}

// 性别选择
.form-radio-group {
  display: flex;
  align-items: center;
}

.form-radio {
  display: flex;
  align-items: center;
  margin-right: 60rpx;
  font-size: 28rpx;
  color: #333;

  .radio-circle {
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

    .radio-circle {
      border-color: #2588FF;

      &::after {
        display: block;
      }
    }
  }
}

// 地区选择
.area-picker {
  cursor: pointer;
}

.area-value {
  font-size: 28rpx;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: space-between;

  &.area-placeholder {
    color: #999;
  }
}

// 标签选择
.tag-group {
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
.default-switch {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 28rpx;
  color: #333;
}

// 底部保存按钮
.bottom-button {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20rpx;
  background-color: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.save-btn {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  background-color: #2588FF;
  color: #fff;
  border-radius: 40rpx;
  font-size: 30rpx;
  font-weight: 500;
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