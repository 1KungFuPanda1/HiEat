import App from './App'
import { createSSRApp } from 'vue'
import * as Pinia from 'pinia'

// 配置API基础URL
// 根据不同环境设置不同的baseURL
let baseURL = 'http://localhost:8080';

export function createApp() {
  const app = createSSRApp(App)
  app.use(Pinia.createPinia())
  // 存储到全局
  uni.setStorageSync('baseURL', baseURL);
  
  return {
    app,
    Pinia
  }
}