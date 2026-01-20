<template>
  <div class="admin-login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
            <rect x="8" y="8" width="32" height="32" rx="4" stroke="currentColor" stroke-width="2"/>
            <path d="M8 20H40M8 32H40M20 8V40M28 8V40" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <h1 class="login-title">Admin Login</h1>
        <p class="login-subtitle">Access to review and manage prediction markets</p>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="username">
            <span class="label-text">Username</span>
            <span class="required">*</span>
          </label>
          <input
            id="username"
            v-model="loginData.username"
            type="text"
            placeholder="Enter your username"
            class="form-input"
            required
            autocomplete="username"
          />
        </div>

        <div class="form-group">
          <label for="password">
            <span class="label-text">Password</span>
            <span class="required">*</span>
          </label>
          <div class="password-input-wrapper">
            <input
              id="password"
              v-model="loginData.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="Enter your password"
              class="form-input"
              required
              autocomplete="current-password"
            />
            <button
              type="button"
              @click="showPassword = !showPassword"
              class="toggle-password"
            >
              <svg v-if="!showPassword" width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M10 4C5 4 1.73 7.11 1 11.5C1.73 15.89 5 19 10 19C15 19 18.27 15.89 19 11.5C18.27 7.11 15 4 10 4ZM10 16.5C7.5 16.5 5.5 14.5 5.5 12C5.5 9.5 7.5 7.5 10 7.5C12.5 7.5 14.5 9.5 14.5 12C14.5 14.5 12.5 16.5 10 16.5Z" stroke="currentColor" stroke-width="1.5"/>
                <circle cx="10" cy="12" r="2.5" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              <svg v-else width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M12.5 4L14.5 6L14.5 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M7.5 4L5.5 6L5.5 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M1 11C1.73 6.61 5 3.5 10 3.5C15 3.5 18.27 6.61 19 11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
        </div>

        <div v-if="errorMessage" class="error-message">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <circle cx="10" cy="10" r="8" stroke="#EF4444" stroke-width="2"/>
            <path d="M10 6V10M10 14H10.01" stroke="#EF4444" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <span>{{ errorMessage }}</span>
        </div>

        <div class="form-actions">
          <button type="button" @click="handleCancel" class="btn btn-secondary">
            Cancel
          </button>
          <button type="submit" class="btn btn-primary" :disabled="isLoading">
            {{ isLoading ? 'Logging in...' : 'Login' }}
          </button>
        </div>
      </form>

      <div class="login-footer">
        <div class="demo-credentials">
          <div class="demo-title">Demo Credentials:</div>
          <div class="demo-info">
            <span class="demo-label">Username:</span>
            <span class="demo-value">admin</span>
          </div>
          <div class="demo-info">
            <span class="demo-label">Password:</span>
            <span class="demo-value">admin123</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';

export default {
  name: 'AdminLogin',
  setup() {
    const router = useRouter();
    const route = useRoute();

    const loginData = ref({
      username: '',
      password: ''
    });
    const showPassword = ref(false);
    const isLoading = ref(false);
    const errorMessage = ref('');

    const handleLogin = async () => {
      if (isLoading.value) return;

      isLoading.value = true;
      errorMessage.value = '';

      try {
        // 模拟登录验证
        await new Promise(resolve => setTimeout(resolve, 1000));

        // 简单的演示验证
        if (loginData.value.username === 'admin' && loginData.value.password === 'admin123') {
          // 保存登录状态到localStorage
          localStorage.setItem('adminLoggedIn', 'true');
          localStorage.setItem('adminUsername', loginData.value.username);

          // 导航到审核页面
          router.push('/admin/review');
        } else {
          errorMessage.value = 'Invalid username or password';
        }
      } catch (error) {
        console.error('Login error:', error);
        errorMessage.value = 'Login failed. Please try again.';
      } finally {
        isLoading.value = false;
      }
    };

    const handleCancel = () => {
      router.push('/');
    };

    return {
      loginData,
      showPassword,
      isLoading,
      errorMessage,
      handleLogin,
      handleCancel
    };
  }
};
</script>

<style scoped>
.admin-login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: var(--bg-primary);
}

.login-card {
  background: var(--card-bg);
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 440px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: var(--accent-light);
  border-radius: 16px;
  color: white;
  margin-bottom: 24px;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.login-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.required {
  color: #EF4444;
  font-weight: 700;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 14px;
  font-family: inherit;
  transition: border-color 0.2s;
}

.form-input::placeholder {
  color: var(--text-secondary);
  opacity: 0.7;
}

.form-input:focus {
  outline: none;
  border-color: var(--accent-light);
}

.password-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.password-input-wrapper .form-input {
  padding-right: 48px;
}

.toggle-password {
  position: absolute;
  right: 12px;
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
}

.toggle-password:hover {
  color: var(--text-primary);
}

.error-message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid #EF4444;
  border-radius: 8px;
  color: #EF4444;
  font-size: 14px;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.btn {
  flex: 1;
  padding: 14px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-secondary {
  background: transparent;
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
}

.btn-secondary:hover {
  background: var(--input-bg);
  border-color: var(--text-secondary);
}

.btn-primary {
  background: var(--accent-light);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #4F46E5;
  transform: translateY(-1px);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-footer {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color);
}

.demo-credentials {
  padding: 16px;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
}

.demo-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.demo-info {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 6px;
}

.demo-info:last-child {
  margin-bottom: 0;
}

.demo-label {
  color: var(--text-secondary);
}

.demo-value {
  color: var(--accent-light);
  font-weight: 600;
  font-family: 'Monaco', 'Courier New', monospace;
  background: rgba(139, 92, 246, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
}

/* 响应式 */
@media (max-width: 480px) {
  .login-card {
    padding: 24px;
  }

  .login-title {
    font-size: 24px;
  }

  .form-actions {
    flex-direction: column-reverse;
  }
}
</style>
