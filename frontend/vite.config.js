import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiOrigin = env.GOOCI_API_ORIGIN
  const server = {
    host: env.GOOCI_BIND_HOST || '0.0.0.0',
    port: Number(env.GOOCI_DEV_PORT || 5173),
    strictPort: true
  }
  if (apiOrigin) {
    server.proxy = {
      '/api': { target: apiOrigin, changeOrigin: true },
      '/ext': { target: apiOrigin, changeOrigin: true, ws: true }
    }
  }
  return { plugins: [vue()], server }
})
