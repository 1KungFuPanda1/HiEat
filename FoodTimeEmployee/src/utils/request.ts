import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getRequestKey,
  pending,
  checkPending,
  removePending
} from './requestOptimize'
import router from '@/router'

const CancelToken = axios.CancelToken

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 600000
})

// Request interceptors
service.interceptors.request.use(
  (config: any) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['token'] = userStore.token
    } else if (userStore.token && config.url != '/login') {
      window.location.href = '/login'
      return false
    }

    // get请求映射params参数
    if (config.method === 'get' && config.params) {
      let url = config.url + '?'
      for (const propName of Object.keys(config.params)) {
        const value = config.params[propName]
        var part = encodeURIComponent(propName) + '='
        if (value !== null && typeof (value) !== 'undefined') {
          if (typeof value === 'object') {
            for (const key of Object.keys(value)) {
              let params = propName + '[' + key + ']'
              var subPart = encodeURIComponent(params) + '='
              url += subPart + encodeURIComponent(value[key]) + '&'
            }
          } else {
            url += part + encodeURIComponent(value) + '&'
          }
        }
      }
      url = url.slice(0, -1)
      config.params = {}
      config.url = url
    }

    // 计算当前请求key值
    const key = getRequestKey(config)
    if (checkPending(key)) {
      const source = CancelToken.source()
      config.cancelToken = source.token
      source.cancel('重复请求')
    } else {
      pending[key] = true
    }
    return config
  },
  (error: any) => {
    Promise.reject(error)
  }
)

// Response interceptors
service.interceptors.response.use(
  (response: any) => {
    if (response.data.status === 401) {
      router.push('/login')
    }
    response.config.url = response.config.url.replace('/api', '')
    const key = getRequestKey(response.config)
    removePending(key)
    if (response.data.code === 1) {
      return response
    }
    return response
  },
  (error: any) => {
    if (error && error.response) {
      switch (error.response.status) {
        case 400:
          ElMessage.error(error.response.data?.msg || '请求参数错误')
          break
        case 401:
          router.push('/login')
          break
        case 403:
          ElMessage.error('没有权限')
          break
        case 404:
          console.warn('请求的资源不存在:', error.config?.url)
          break
        case 405:
          error.message = '请求错误'
          break
        case 500:
          ElMessage.error('服务器内部错误，请稍后重试')
          break
        default:
          console.error('请求异常:', error.response.status, error.config?.url)
      }
    } else {
      console.error('网络请求失败:', error.message)
    }
    if (error.config) {
      error.config.url = error.config.url.replace('/api', '')
      const key = getRequestKey(error.config)
      removePending(key)
    }
    return Promise.reject(error)
  }
)

export default service
