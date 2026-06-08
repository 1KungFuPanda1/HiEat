<template>
  <span
    v-if="svgContent"
    class="svg-icon"
    :style="{ width: width + 'px', height: height + 'px' }"
    v-html="svgContent"
  />
</template>

<script lang="ts">
import { defineComponent, ref, watch, onMounted } from 'vue'

// Import all SVG files as raw strings
const svgModules = import.meta.glob('@/icons/svg/*.svg', { query: '?raw', import: 'default', eager: true })

const svgCache: Record<string, string> = {}
for (const [path, content] of Object.entries(svgModules)) {
  const name = path.replace(/.*\/(.*)\.svg$/, '$1')
  svgCache[name] = content as string
}

export default defineComponent({
  name: 'SvgIcon',
  props: {
    name: { type: String, required: true },
    width: { type: [String, Number], default: 20 },
    height: { type: [String, Number], default: 20 }
  },
  setup(props) {
    const svgContent = ref('')

    const loadIcon = () => {
      svgContent.value = svgCache[props.name] || ''
    }

    onMounted(loadIcon)
    watch(() => props.name, loadIcon)

    return { svgContent }
  }
})
</script>

<style scoped>
.svg-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  vertical-align: middle;
}
.svg-icon :deep(svg) {
  width: 100%;
  height: 100%;
}
</style>
