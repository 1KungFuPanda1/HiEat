import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 用户相关状态管理
 */
export const useUserStore = defineStore('user', () => {
  // 用户token
  const token = ref(localStorage.getItem('token') || '')

  // 用户信息
  const userInfo = ref(null)

  // 登录
  const login = (data) => {
    token.value = data.token
    userInfo.value = data.userInfo
    localStorage.setItem('token', data.token)
  }

  // 清除token
  const clearToken = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  // 登出
  const logout = () => {
    clearToken()
  }

  return {
    token,
    userInfo,
    login,
    clearToken,
    logout
  }
})

// Pinia：存在内存（运行时）Pinia：有响应式，数据变了页面自动刷新
// 页面刷新、小程序重启 → 数据直接清空
// localStorage：存在本地磁盘 localStorage：无响应式，存了不会自动更新页面
// 关闭页面、重启 APP、关机 → 数据还在