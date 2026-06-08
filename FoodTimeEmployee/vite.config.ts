import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern-compiler',
        additionalData: `
          @use "@/styles/_variables.scss" as *;
          @use "@/styles/_mixins.scss" as *;
        `
      }
    }
  },
  server: {
    host: '0.0.0.0',
    port: 8888,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        ws: false,
        secure: false,
        changeOrigin: true,
        rewrite: (path) => '/admin' + path.replace(/^\/api/, '')
      }
    }
  },
  build: {
    sourcemap: true
  }
})
