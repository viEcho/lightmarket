<template>
  <div class="admin-layout">
    <!-- Admin Header -->
    <header class="admin-header">
      <div class="header-container">
        <div class="logo">
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
            <rect x="4" y="4" width="24" height="24" rx="4" stroke="currentColor" stroke-width="2"/>
            <path d="M4 12H28M4 20H28M12 4V28M20 4V28" stroke="currentColor" stroke-width="2"/>
          </svg>
          <h1>Admin Panel</h1>
        </div>
        <nav class="admin-nav">
          <router-link
            to="/admin/review"
            class="nav-link"
            :class="{ active: $route.name === 'admin-review' }"
          >
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
              <path d="M9 2L4 7V16H14V7L9 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              <path d="M9 10V13M9 6H9.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            Review Markets
          </router-link>
          <router-link
            to="/admin/dashboard"
            class="nav-link"
            :class="{ active: $route.name === 'admin-dashboard' }"
          >
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
              <rect x="2" y="2" width="14" height="14" rx="2" stroke="currentColor" stroke-width="2"/>
              <path d="M5 9H13M9 5V13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            Dashboard
          </router-link>
        </nav>
        <div class="header-actions">
          <span class="admin-user">{{ adminUsername }}</span>
          <button @click="logout" class="logout-btn">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M11 8H3M3 8L6 11M3 8L6 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M9 4V12C9 12.5304 9.21071 13.0391 9.58579 13.4142C9.96086 13.7893 10.4696 14 11 14H12C12.5304 14 13.0391 13.7893 13.4142 13.4142C13.7893 13.0391 14 12.5304 14 12V4C14 3.46957 13.7893 2.96086 13.4142 2.58579C13.0391 2.21071 12.5304 2 12 2H11C10.4696 2 9.96086 2.21071 9.58579 2.58579C9.21071 2.96086 9 3.46957 9 4Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            Logout
          </button>
        </div>
      </div>
    </header>

    <!-- Admin Content -->
    <main class="admin-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const adminUsername = ref('')

onMounted(() => {
  adminUsername.value = localStorage.getItem('adminUsername') || 'Admin'
})

const logout = () => {
  if (confirm('Are you sure you want to logout?')) {
    localStorage.removeItem('adminLoggedIn')
    localStorage.removeItem('adminUsername')
    router.push({ name: 'admin-login' })
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f7;
  overflow: hidden;
}

.admin-header {
  background: white;
  border-bottom: 1px solid #e5e5e5;
  flex-shrink: 0;
}

.header-container {
  max-width: 1600px;
  margin: 0 auto;
  padding: 1rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo h1 {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.admin-nav {
  display: flex;
  gap: 2rem;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  text-decoration: none;
  font-weight: 500;
  font-size: 0.875rem;
  transition: color 0.15s ease;
  position: relative;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: -1.25rem;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--accent-light);
  transform: scaleX(0);
  transition: transform 0.15s ease;
}

.nav-link:hover {
  color: var(--text-primary);
}

.nav-link.active {
  color: var(--accent-light);
}

.nav-link.active::after {
  transform: scaleX(1);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.admin-user {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
  padding: 0.5rem 1rem;
  background: var(--input-bg);
  border-radius: 8px;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0.5rem 1rem;
  background: #EF4444;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
}

.logout-btn:hover {
  background: #DC2626;
}

.admin-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 0.5rem 2rem;
  max-width: 1600px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

/* 自定义滚动条样式 */
.admin-content::-webkit-scrollbar {
  width: 8px;
}

.admin-content::-webkit-scrollbar-track {
  background: #f5f5f7;
}

.admin-content::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 4px;
}

.admin-content::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
</style>
