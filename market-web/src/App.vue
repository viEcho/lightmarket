<template>
  <div class="app">
    <!-- 公共Header - 只在非admin页面显示 -->
    <Header v-if="!isAdminPage" />
    <div class="app-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Header from './components/Header.vue'

const route = useRoute()

// 判断是否是admin页面
const isAdminPage = computed(() => {
  return route.path.startsWith('/admin') || route.path === '/admin-login'
})
</script>

<style scoped>
.app {
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
}

.app-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}
</style>
