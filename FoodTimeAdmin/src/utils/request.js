import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 5000
})
//请求 → service → 拦截器 → config → 发出去
// 请求拦截器,添加token
service.interceptors.request.use(
  config => {//这个 config 就是 axios 自动传给你的
    const userStore = useUserStore()
    if (userStore.token) {
      // 使用token作为头部名称，而不是Authorization
      config.headers['token'] = userStore.token
    }
    return config
  },
  error => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data

    // 如果响应类型是blob，直接返回
    if (response.config.responseType === 'blob') {
      return response.data
    }

    if (res.code !== 1) {  // 判断条件为 1
      // 如果是401错误，处理登录过期
      if (res.code === 401) {
        handleTokenExpired()
        return Promise.reject(new Error('登录已过期，请重新登录'))
      }

      ElMessage({
        message: res.msg || '请求失败',  // 使用 msg 字段
        type: 'error',
        duration: 3000
      })
      return Promise.reject(new Error(res.msg || '请求失败'))  // 这里也使用 msg 字段
    }
    return res
  },
  error => {
    // 处理401错误（Token校验失败）
    if (error.response && error.response.status === 401) {
      handleTokenExpired()
    } else {
      console.error('响应错误：', error)
      ElMessage({
        message: error.response?.data?.msg || '请求失败',
        type: 'error',
        duration: 3000
      })
    }
    return Promise.reject(error)
  }
)

// 统一处理Token过期
const handleTokenExpired = () => {
  // 清除token
  const userStore = useUserStore()
  userStore.clearToken()

  // 显示错误消息
  ElMessage({
    message: '登录已过期，请重新登录',
    type: 'error',
    duration: 3000
  })

  // 跳转到登录页面
  router.push('/login')
}

export default service