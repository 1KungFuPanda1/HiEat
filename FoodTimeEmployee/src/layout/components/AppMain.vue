<template>
  <section class="app-main">
    <!-- 使用 :key 强制重新挂载，但去掉 transition 避免与 Element Plus Teleport 组件的 DOM 清理竞态 -->
    <router-view :key="$route.path" />
  </section>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'AppMain'
})
</script>

<style lang="scss" scoped>
.app-main {
  height: calc(100% - 64px);
  overflow-y: auto;
}
</style>
