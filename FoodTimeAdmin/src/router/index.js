import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'
import Login from '../views/login/Login.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login //当访问这个路由路径时，显示 User.vue 这个页面组件。
  },
  {
    path: '/',
    component: Layout,// 用外壳页面
    redirect: '/shop',// 直接转发到 /shop
  },
  {
    path: '/shop',
    component: Layout, //直接转发到Layout共享组件：嵌套路由中，父级组件（如布局组件）在切换子路由时不会重新渲染，实现布局复用。
    children: [  //嵌套路由：路由内包含子路由，需在父组件中放置 <router-view> 渲染子组件，常用于页面布局（侧边栏 + 内容区）。
      {
        path: '',
        name: 'Shop',
        component: () => import('@/views/shop/index.vue'), //组件懒加载
        meta: { title: '店铺审核', icon: 'shop', requiresAuth: true }
      } // 需要登录才能进入 requiresAuth: true 
    ]
  },
  {
    path: '/shop-category',
    component: Layout,
    children: [
      {
        path: '',
        name: 'ShopCategory', //1.跳转不用写长路径 2.传参更方便，不会拼错 3.菜单高亮、面包屑必须用 4.path 改了，代码不用动
        component: () => import('@/views/shopCategory/index.vue'),
        meta: { title: '店铺分类管理', icon: 'menu', requiresAuth: true }
      }//比如：this.$router.push({ name: 'ShopCategory' })不用写this.$router.push({/shop-category })
    ]
  },
  {
    path: '/notice',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Notice',
        component: () => import('@/views/notice/index.vue'),
        meta: { title: '通知管理', icon: 'bell', requiresAuth: true }
      }
    ]
  },
  {
    path: '/carousel',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Carousel',
        component: () => import('@/views/carousel/index.vue'),
        meta: { title: '轮播图管理', icon: 'picture', requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),  // 使用 HTML5 History 模式
  routes    // 传入路由规则数组
})

// 路由守卫 每次页面跳转前，先执行这里检查
// to：要去哪里
//from：从哪里来
//next：放行 or 拦截
router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('token')

  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login') //next：放行
  } else if (to.path === '/login' && isAuthenticated) {
    next('/')
  } else {
    next() //next() = 放行，去你本来想去的页面
  }
})

//next(false)；拦截

export default router