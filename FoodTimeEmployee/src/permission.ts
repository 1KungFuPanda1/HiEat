import router from './router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import Cookies from 'js-cookie'

NProgress.configure({ showSpinner: false })

// 防止重复重定向的白名单
const whiteList = ['/login', '/register', '/404']

router.beforeEach(async (to, _from, next) => {
  NProgress.start()

  try {
    if (Cookies.get('token')) {
      // 已登录：如果要去登录/注册页，直接跳转到首页
      if (whiteList.includes(to.path)) {
        next('/')
        return
      }
      next()
    } else {
      // 未登录：白名单页面直接放行，其他页面跳转登录
      if (to.meta.notNeedAuth || whiteList.includes(to.path)) {
        next()
      } else {
        next('/login')
      }
    }
  } catch (error) {
    console.error('路由守卫异常:', error)
    NProgress.done()
    next(false)
  }
})

router.afterEach((to) => {
  NProgress.done()
  // 安全设置标题，防止 parentNode 相关错误
  if (document && document.title !== undefined) {
    document.title = (to.meta.title as string) || '好食光'
  }
})
