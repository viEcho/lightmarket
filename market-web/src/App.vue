<template>
  <div class="app">
    <!-- 公共Header - 只在非admin页面显示 -->
    <Header v-if="!isAdminPage" />
    <div class="app-content" :class="{ 'admin-page': isAdminPage }">
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
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding-top: 80px; /* 为 fixed Header 留出空间 */
  box-sizing: border-box;
}

.app-content.admin-page {
  padding-top: 0; /* admin页面不需要顶部padding */
}
</style>
