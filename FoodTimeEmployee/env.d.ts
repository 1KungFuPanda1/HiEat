/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module '*.scss' {
  const content: any
  export default content
}

declare module '@/config.json' {
  const content: { baseUrl: string }
  export default content
}

interface ImportMetaEnv {
  readonly VITE_APP_BASE_API: string
  readonly VITE_APP_URL: string
  readonly VITE_APP_SOCKET_URL: string
  readonly VITE_APP_DELETE_PERMISSIONS: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
