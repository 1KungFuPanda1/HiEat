<template>
   <!-- 整体布局最外层容器 -->
  <div class="app-wrapper">
     <!-- ====================== 左侧侧边栏区域 ====================== -->
    <div class="sidebar-container" :class="{ 'is-collapsed': isCollapsed }">
      <div class="logo">
        <!-- 侧边栏展开显示全称，收缩显示缩写 -->
        <h1 v-if="!isCollapsed">Food Time</h1>
        <h1 v-else>FT</h1>
      </div>

      <!-- Element Plus 侧边栏菜单，开启router：开启路由模式，点击自动跳转 -->
      <!--:default-active="activeMenu"当前激活的菜单，高亮显示 -->
      <!-- router 核心：开启路由跳转，index就是路由path，点击自动跳转 -->
      <el-menu :default-active="activeMenu" class="sidebar-menu" :collapse="isCollapsed" background-color="#ffffff"
        text-color="#333333" active-text-color="#1890ff" router>
        <!-- 店铺审核：index="/shop" 对应路由path，点击跳转到店铺审核组件 -->
        <el-menu-item index="/shop">
          <el-icon>
            <Shop />
          </el-icon>
          <template #title>店铺审核</template>
        </el-menu-item>
         <!-- 店铺分类管理：跳转到 /shop-category 组件 -->
        <el-menu-item index="/shop-category">
          <el-icon>
            <Menu />
          </el-icon>
          <template #title>店铺分类管理</template>
        </el-menu-item>
        <!-- 通知管理：跳转到 /notice 组件 -->
        <el-menu-item index="/notice">
          <el-icon>
            <Bell />
          </el-icon>
          <template #title>通知管理</template>
        </el-menu-item>
        <!-- 轮播图管理：跳转到 /carousel 组件 -->
        <el-menu-item index="/carousel">
          <el-icon>
            <Picture />
          </el-icon>
          <template #title>轮播图管理</template>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- ====================== 右侧主内容区域（顶部导航 + 页面主体） ====================== -->
    <div class="main-container">
      <!-- 顶栏 -->
      <div class="navbar">
        <!-- //汉堡菜单按钮，就是左上角三条杠 -->
        <div class="hamburger" @click="toggleSidebar"> 
          <el-icon v-if="!isCollapsed">
            <Fold />
          </el-icon>
          <el-icon v-else>
            <Expand />
          </el-icon>
        </div>
        <!-- 就是页面顶部的路径导航，比如：首页 / 菜品管理 / 添加菜品 -->
        <div class="breadcrumb"> 
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-if="route.meta && route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <!-- 顶部右侧：全屏、消息、用户头像下拉菜单 -->
        <div class="right-menu">
          <div class="action-item">
            <el-tooltip content="全屏" placement="bottom">
              <el-icon class="action-icon">
                <FullScreen />
              </el-icon>
            </el-tooltip>
          </div>
          <div class="action-item">
            <el-badge :value="3" class="notification-badge">
              <el-tooltip content="消息" placement="bottom">
                <el-icon class="action-icon">
                  <Bell />
                </el-icon>
              </el-tooltip>
            </el-badge>
          </div>
          <el-dropdown trigger="click">
            <div class="user-info">
              <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <span class="username">{{ userStore.userInfo?.name || '管理员' }}</span>
              <el-icon>
                <CaretBottom />
              </el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <el-icon>
                    <User />
                  </el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item>
                  <el-icon>
                    <Setting />
                  </el-icon>系统设置
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon>
                    <SwitchButton />
                  </el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- ====================== 核心：路由内容展示区域 ====================== -->
      <!-- router-view：路由匹配到的页面组件，全部渲染在这里！ -->
      <div class="app-main">
        <router-view v-slot="{ Component }">
          <!-- 页面切换动画 -->
          <transition name="fade-transform" mode="out-in">
           <!-- keep-alive：缓存页面，不重复请求数据 -->
            <keep-alive>
               <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>
<!-- 1. router-view：路由视图，渲染匹配的页面组件 -->
 <!-- 2. transition：页面切换动画 Vue 过渡动画 -->
<!-- 3. keep-alive：缓存页面，不重复加载 Vue 缓存-->
 <!-- 4. component 动态渲染当前路由组件 -->
              <!-- // 假设当前路由是 /shop
              // Component 的值就是 Shop 组件 -->
             <!-- // 假设当前路由是 /notice
            // Component 的值就是 Notice 组件  -->
<!-- <router-view />
当前路径: /shop
    ↓
匹配路由: { path: '/shop', component: Shop }
    ↓
渲染组件: Shop 组件
 v-slot="{ Component }" 的作用：将当前匹配的组件赋值给 Component 变量，然后使用动态组件渲染
 
 为什么需要这样写 ？
- 为了在 router-view 内部添加 transition 和 keep-alive
- 如果直接写 <router-view /> ，就无法添加动画和缓存
 -->

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  HomeFilled,
  Document,
  Shop,
  Fold,
  Expand,
  CaretBottom,
  User,
  Setting,
  SwitchButton,
  Bell,
  FullScreen,
  Menu,
  Picture
} from '@element-plus/icons-vue'

const route = useRoute() //获取当前页面路由对象，只能读、不能改
const router = useRouter() //路由实例，用来控制页面跳转、前进后退，负责操作
const userStore = useUserStore()
const isCollapsed = ref(false) //已折叠

const activeMenu = computed(() => {//自动计算、自动缓存的属性 依赖数据变了才更新，不变就用缓存，不重复执行 只能同步，不能写异步请求（axios、定时器不行）
  return route.path
})

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.app-wrapper {
  display: flex;
  width: 100%;
  height: 100vh;
  background-color: #f5f7fa;

  .sidebar-container {
    width: 220px;
    height: 100%;
    background-color: #ffffff;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    transition: width 0.3s;
    overflow-y: auto;
    z-index: 10;

    &.is-collapsed {
      width: 64px;
    }

    .logo {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 10px;
      border-bottom: 1px solid #f0f0f0;

      h1 {
        color: #1890ff;
        font-size: 18px;
        font-weight: bold;
      }
    }

    .sidebar-menu {
      border-right: none;

      :deep(.el-menu-item) {
        &.is-active {
          background-color: #e6f7ff;
          border-right: 3px solid #1890ff;

          &:hover {
            background-color: #e6f7ff;
          }
        }

        &:hover {
          background-color: #f5f5f5;
        }
      }

      :deep(.el-sub-menu__title) {
        &:hover {
          background-color: #f5f5f5;
        }
      }
    }
  }

  .main-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .navbar {
      height: 60px;
      display: flex;
      align-items: center;
      padding: 0 20px;
      background-color: #ffffff;
      box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

      .hamburger {
        font-size: 20px;
        cursor: pointer;
        padding: 0 12px;
        color: #666;

        &:hover {
          color: #1890ff;
        }
      }

      .breadcrumb {
        margin-left: 8px;
        font-size: 14px;
      }

      .right-menu {
        margin-left: auto;
        display: flex;
        align-items: center;

        .action-item {
          padding: 0 12px;
          cursor: pointer;

          .action-icon {
            font-size: 18px;
            color: #666;

            &:hover {
              color: #1890ff;
            }
          }

          .notification-badge {
            :deep(.el-badge__content) {
              background-color: #ff4d4f;
            }
          }
        }

        .user-info {
          display: flex;
          align-items: center;
          cursor: pointer;
          padding: 0 12px;

          .username {
            margin: 0 8px;
            font-size: 14px;
            color: #333;
          }
        }
      }
    }

    .app-main {
      flex: 1;
      // padding: 20px;
      overflow-y: auto;
      background-color: #f5f7fa;
      border-radius: 4px;
      margin: 16px;
      box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    }
  }
}

// 页面切换动画
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}
</style>