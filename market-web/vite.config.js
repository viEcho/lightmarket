import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    // 减少控制台输出
    watch: {
      usePolling: false
    }
  },
  // 清屏配置
  clearScreen: false
})
