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

<style>
/* 全局样式确保 html 和 body 有高度 */
html, body {
  height: 100%;
  margin: 0;
  padding: 0;
}
</style>

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
  position: relative;
  /* 不设置 margin-top，让子元素自己处理 */
}

.app-content.admin-page {
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>
