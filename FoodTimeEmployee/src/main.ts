import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'normalize.css'
import moment from 'moment'
import * as echarts from 'echarts'

import '@/styles/element-variables.scss'
import '@/styles/index.scss'
import '@/styles/home.scss'
import '@/styles/newRJWMsystem.scss'
import '@/styles/icon/iconfont.css'

import App from '@/App.vue'
import router from '@/router'
import '@/permission'
import { checkProcessEnv } from '@/utils/common'
import SvgIcon from '@/components/SvgIcon/index.vue'

const app = createApp(App)

// 全局错误处理，捕获 el-dialog/el-popper 等 Teleport 组件在路由切换时的 DOM 错误
app.config.errorHandler = (err, _instance, info) => {
  if (err instanceof TypeError) {
    const msg = err.message || ''
    // 这些错误都是 Vue3 + Element Plus 已知问题：
    // Teleport 组件（el-dialog/el-popper/el-select-dropdown 等）在路由切换时，
    // 父组件被销毁但 Teleported DOM 还在 body 中，清理时访问已销毁的节点
    if (msg.includes('parentNode') || msg.includes('nextSibling') || msg.includes('insertBefore')) {
      console.warn('[已捕获] DOM 节点访问错误 (Teleport 清理):', msg)
      return
    }
  }
  console.error(`[Vue Error] ${info}:`, err)
}

// Register global components
app.component('svg-icon', SvgIcon)

// Pinia
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
app.use(pinia)

// Router
app.use(router)

// Element Plus
app.use(ElementPlus)

// Global properties (replaces Vue.prototype)
app.config.globalProperties.moment = moment
app.config.globalProperties.$checkProcessEnv = checkProcessEnv
app.config.globalProperties.$echarts = echarts

app.mount('#app')
