<template>
  <el-breadcrumb
    class="app-breadcrumb"
    separator="/"
  >
    <transition-group name="breadcrumb">
      <el-breadcrumb-item
        v-for="(item, index) in breadcrumbs"
        :key="item.path"
      >
        <span
          v-if="item.redirect === 'noredirect' || index === breadcrumbs.length-1"
          class="no-redirect"
        >{{ item.meta.title }}</span>
        <a
          v-else
          @click.prevent="handleLink(item)"
        >{{ item.meta.title }}</a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script lang="ts">
import pathToRegexp from 'path-to-regexp'
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'Breadcrumb',

  data() {
    return {
      breadcrumbs: [] as any[]
    }
  },

  watch: {
    $route(route: any) {
      if (route.path.startsWith('/redirect/')) {
        return
      }
      this.getBreadcrumb()
    }
  },

  created() {
    this.getBreadcrumb()
  },

  methods: {
    getBreadcrumb() {
      let matched = (this.$route as any).matched.filter(
        (item: any) => item.meta && item.meta.title
      )
      this.breadcrumbs = matched.filter((item: any) => {
        return item.meta && item.meta.title && item.meta.breadcrumb !== false
      })
    },

    isDashboard(route: any) {
      const name = route && route.meta && route.meta.title
      return name === '集团管理'
    },

    pathCompile(path: string) {
      const { params } = this.$route as any
      const toPath = pathToRegexp.compile(path)
      return toPath(params)
    },

    handleLink(item: any) {
      const { redirect, path } = item
      if (redirect) {
        this.$router.push(redirect)
        return
      }
      this.$router.push(this.pathCompile(path))
    }
  }
})
</script>

<style lang="scss" scoped>
.el-breadcrumb__inner,
.el-breadcrumb__inner a {
  font-weight: 400 !important;
}

.app-breadcrumb.el-breadcrumb {
  display: inline-block;
  font-size: 14px;
  line-height: 50px;
  margin-left: 8px;

  .no-redirect {
    color: #97a8be;
    cursor: text;
  }
}
</style>
