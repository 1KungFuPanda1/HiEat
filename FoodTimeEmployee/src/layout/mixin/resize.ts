import { defineComponent } from 'vue'
import { useAppStore, DeviceType } from '@/stores/app'

const WIDTH = 992

export default defineComponent({
  name: 'ResizeMixin',

  data() {
    return {
      _isDestroyed: false
    }
  },

  computed: {
    device() {
      return useAppStore().device
    },
    sidebar() {
      return useAppStore().sidebar
    }
  },

  watch: {
    $route() {
      if (this._isDestroyed) return
      const appStore = useAppStore()
      if (this.device === DeviceType.Mobile && this.sidebar.opened) {
        appStore.CloseSideBar(false)
      }
    }
  },

  beforeMount() {
    window.addEventListener('resize', this.resizeHandler)
  },

  mounted() {
    const isMobile = this.isMobile()
    const appStore = useAppStore()
    if (isMobile) {
      appStore.ToggleDevice(DeviceType.Mobile)
      appStore.CloseSideBar(true)
    }
  },

  beforeUnmount() {
    this._isDestroyed = true
    window.removeEventListener('resize', this.resizeHandler)
  },

  methods: {
    isMobile() {
      // 安全访问 document.body，防止组件卸载后的 DOM 访问错误
      if (!document.body) return false
      const rect = document.body.getBoundingClientRect()
      return rect.width - 1 < WIDTH
    },

    resizeHandler() {
      if (this._isDestroyed) return
      if (!document.hidden && document.body) {
        const appStore = useAppStore()
        const isMobile = this.isMobile()
        appStore.ToggleDevice(isMobile ? DeviceType.Mobile : DeviceType.Desktop)
        if (isMobile) {
          appStore.CloseSideBar(true)
        }
      }
    }
  }
})
