<template>
  <header class="header">
    <div class="header-container">
      <div class="logo">
        <router-link to="/markets" class="logo-link">
          <h1>LightMarket</h1>
        </router-link>
      </div>
      <nav class="nav">
        <router-link
          to="/markets"
          class="nav-link"
          :class="{ active: $route.path === '/markets' || $route.path === '/' }"
        >
          Markets
        </router-link>
        <router-link
          to="/leaderboard"
          class="nav-link"
          :class="{ active: $route.path === '/leaderboard' }"
        >
          Leaderboard
        </router-link>
        <router-link
          to="/activity"
          class="nav-link"
          :class="{ active: $route.path === '/activity' }"
        >
          Activity
        </router-link>
      </nav>
      <div class="header-actions">
        <button
          class="btn-secondary"
          @click="$router.push('/create-market')"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M8 2V14M2 8H14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          Make market
        </button>
        <button
          class="btn-secondary"
          @click="$router.push('/admin-dashboard')"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <rect x="2" y="2" width="12" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
            <path d="M5 8H11M8 5V11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          My markets
        </button>
        <button class="btn-connect">
          <span>Connect Wallet</span>
        </button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<style scoped>
.header {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 1rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo-link {
  text-decoration: none;
  display: inline-block;
  transition: opacity 0.15s ease;
}

.logo-link:hover {
  opacity: 0.8;
}

.logo h1 {
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.025em;
  background: linear-gradient(135deg, var(--accent) 0%, var(--accent-light) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.nav {
  display: flex;
  gap: 2.5rem;
}

.nav-link {
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
  background: var(--accent);
  transform: scaleX(0);
  transition: transform 0.15s ease;
}

.nav-link:hover {
  color: var(--text-primary);
}

.nav-link.active {
  color: var(--text-primary);
}

.nav-link.active::after {
  transform: scaleX(1);
}

.header-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.btn-secondary {
  background: transparent;
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  padding: 0.625rem 1rem;
  border-radius: 8px;
  font-weight: 500;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.15s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-secondary:hover {
  background: var(--input-bg);
  border-color: var(--text-primary);
}

.btn-connect {
  background: var(--accent);
  color: white;
  border: none;
  padding: 0.625rem 1.25rem;
  border-radius: 8px;
  font-weight: 500;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.15s ease;
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05);
}

.btn-connect:hover {
  background: #1a1a1a;
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
  transform: translateY(-1px);
}

.btn-connect:active {
  transform: translateY(0);
}
</style>
