<template>
  <div>
    <div class="logo">
      <div v-if="!isCollapse" class="sidebar-logo">
        <img src="@/assets/login/food_time_s.png">
      </div>
      <div v-else class="sidebar-logo-mini">
        <img src="@/assets/login/mini-logo.png">
      </div>
    </div>
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-openeds="defOpen"
        :default-active="defAct"
        :collapse="isCollapse"
        :background-color="variables.menuBg"
        :text-color="variables.menuText"
        :active-text-color="variables.menuActiveText"
        :unique-opened="false"
        :collapse-transition="false"
        mode="vertical"
      >
        <sidebar-item
          v-for="route in routes"
          :key="route.path"
          :item="route"
          :base-path="route.path"
          :is-collapse="isCollapse"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import SidebarItem from './SidebarItem.vue'
import Cookies from 'js-cookie'

export default defineComponent({
  name: 'SideBar',
  components: {
    SidebarItem
  },

  computed: {
    name() {
      const userStore = useUserStore()
      return (userStore.userInfo as any).name
        ? (userStore.userInfo as any).name
        : JSON.parse(Cookies.get('user_info') as any)?.name
    },

    defOpen() {
      let path = ['/']
      this.routes.forEach((n: any, i: number) => {
        if (n.meta.roles && n.meta.roles[0] === this.roles[0]) {
          path.splice(0, 1, n.path)
        }
      })
      return path
    },

    defAct() {
      return this.$route.path
    },

    sidebar() {
      return useAppStore().sidebar
    },

    roles() {
      return useUserStore().roles
    },

    routes() {
      const router = this.$router as any
      let routes = JSON.parse(
        JSON.stringify([...router.options.routes])
      )
      let menuList: any[] = []
      let menu = routes.find((item: any) => item.path === '/')
      if (menu) {
        menuList = menu.children
      }
      return menuList
    },

    variables() {
      return {
        menuBg: 'rgb(52, 55, 68)',
        menuText: '#bfcbd9',
        menuActiveText: '#ffd04b'
      }
    },

    isCollapse() {
      return !this.sidebar.opened
    }
  },

  beforeUnmount() {
    // 清理 el-menu submenu popper（popper-append-to-body 会创建 teleported 元素）
    // 通过强制关闭 sidebar 来销毁所有打开的 popper
    const appStore = useAppStore()
    if (appStore.sidebar.opened) {
      appStore.CloseSideBar(true)
    }
  },

  methods: {
    async logout() {
      const userStore = useUserStore()
      await userStore.LogOut()
      this.$router.replace({ path: '/login' }).catch((err: any) => {
        console.log('路由跳转:', err)
      })
    }
  }
})
</script>

<style lang="scss" scoped>
.logo {
  text-align: center;
  background-color: #918acd;
  padding: 15px 0 0;
  height: 60px;
  img {
    display: inline-block;
  }
}

.sidebar-logo {
  img {
    height: 30px;
    width: auto;
  }
}

.sidebar-logo-mini {
  img {
    width: 30px;
    height: 30px;
  }
}

.el-scrollbar {
  height: 100%;
  background-color: rgb(52, 55, 68);
}

.el-menu {
  border: none;
  height: calc(95vh - 23px);
  width: 100% !important;
  padding: 47px 15px 0;
}
</style>
