import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: uni.getStorageSync('token') || '',
    userInfo: uni.getStorageSync('userInfo') || {
      avatar: '',
      username: '',
      isLogin: false,
      userId: ''
    }
  }),
  
  getters: {
    isLoggedIn: (state) => state.userInfo.isLogin && state.token,
    getUserInfo: (state) => state.userInfo,
    getToken: (state) => state.token
  },
  
  actions: {
    // 设置用户信息
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      // 同步到本地存储
      uni.setStorageSync('userInfo', userInfo)
    },
    
    // 设置 token
    setToken(token) {
      this.token = token
      // 同步到本地存储
      uni.setStorageSync('token', token)
    },
    
    // 登录成功后的操作
    loginSuccess(userData) {
      const userInfo = {
        avatar: userData.avatar || '/static/image/user/default.jpg',
        username: userData.username || '用户' + userData.phone.substring(7),
        isLogin: true,
        userId: userData.userId
      }
      
      // 更新状态
      this.setUserInfo(userInfo)
      this.setToken(userData.token)
      
      // 跳转到主页
      uni.switchTab({
        url: '/pages/index/index'
      })
    },
    
    // 退出登录
    logout() {
      // 清除用户信息和 token
      this.setUserInfo({
        avatar: '',
        username: '',
        isLogin: false,
        userId: ''
      })
      this.setToken('')
      
      // 清除本地存储
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      
      // 跳转到登录页
      uni.navigateTo({
        url: '/pages/login/index'
      })
    }
  }
})