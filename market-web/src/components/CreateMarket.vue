<template>
  <div class="create-market-container">
    <div class="create-market-card">
      <h1 class="page-title">Create New Prediction Market</h1>
      <p class="page-subtitle">Stake tokens to create a prediction event. Once approved, staked tokens become liquidity source</p>

      <!-- 未登录提示 -->
      <div v-if="!isConnected" class="login-prompt">
        <div class="login-prompt-content">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none" class="login-icon">
            <rect x="8" y="16" width="32" height="24" rx="2" stroke="currentColor" stroke-width="2"/>
            <path d="M16 16V12C16 7.58172 19.5817 4 24 4C28.4183 4 32 7.58172 32 12V16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <circle cx="24" cy="28" r="4" stroke="currentColor" stroke-width="2"/>
          </svg>
          <h2 class="login-title">Connect Your Wallet</h2>
          <p class="login-description">You need to connect your wallet to create a prediction market</p>
          <button type="button" @click="handleConnect" class="btn-connect-large" :disabled="isLoading">
            <span v-if="isLoading">Connecting...</span>
            <span v-else>Connect Wallet</span>
          </button>
        </div>
      </div>

      <!-- 创建市场表单 -->
      <form v-else @submit.prevent="handleSubmit" class="market-form">
        <!-- Market Title -->
        <div class="form-group">
          <label for="title">
            <span class="label-text">Market Title</span>
            <span class="required">*</span>
          </label>
          <input
            id="title"
            v-model="formData.title"
            type="text"
            placeholder="e.g., Will Bitcoin exceed $100,000 by December 31, 2024?"
            class="form-input"
            @blur="checkDuplicate"
            required
          />
          <!-- Duplicate Warning -->
          <div v-if="duplicateMarket" class="duplicate-warning">
            <div class="warning-content">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <circle cx="10" cy="10" r="8" stroke="#F59E0B" stroke-width="2"/>
                <path d="M10 6V10M10 14H10.01" stroke="#F59E0B" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <div class="warning-text">
                <div class="warning-title">
                  {{ duplicateMarket.status === 'approved' ? 'Topic Already Exists' : 'Topic Under Review' }}
                </div>
                <div class="warning-desc">
                  {{ duplicateMarket.status === 'approved'
                    ? 'This topic has been approved and published. Cannot submit duplicate.'
                    : 'This topic is currently in pre-review or final-review stage.' }}
                </div>
              </div>
            </div>
            <button
              type="button"
              @click="goToMarket"
              class="view-market-btn"
            >
              View Details →
            </button>
          </div>
        </div>

        <!-- Market Description -->
        <div class="form-group">
          <label for="description">
            <span class="label-text">Detailed Description</span>
            <span class="required">*</span>
          </label>
          <textarea
            id="description"
            v-model="formData.description"
            placeholder="Please describe the background, judgment criteria and other details of the prediction event"
            class="form-textarea"
            rows="4"
            required
          ></textarea>
          <p class="help-text">Ensure the description is clear, results are verifiable and have clear resolution criteria</p>
        </div>

        <!-- End Time -->
        <div class="form-group">
          <label for="closeTime">
            <span class="label-text">Market End Time</span>
            <span class="required">*</span>
          </label>
          <input
            id="closeTime"
            v-model="formData.closeTime"
            type="datetime-local"
            class="form-input"
            :min="minEndTime"
            required
          />
          <p class="help-text">After this time, the market will stop trading and await resolution</p>
        </div>

        <!-- Initial Token Stake -->
        <div class="form-group">
          <label for="baseLiquidity">
            <span class="label-text">Initial Liquidity Stake</span>
            <span class="required">*</span>
          </label>
          <div class="stake-input-wrapper">
            <input
              id="baseLiquidity"
              v-model.number="formData.baseLiquidity"
              type="number"
              placeholder="1000"
              class="form-input stake-input"
              min="100"
              step="100"
              required
            />
            <span class="token-symbol">IMKT</span>
          </div>
          <p class="help-text">Minimum stake 100 IMKT. Higher stake increases chances of passing final review</p>
          <div class="stake-tiers">
            <div class="tier-info">
              <span class="tier-label">Basic Stake:</span>
              <span class="tier-value">100-999 IMKT</span>
            </div>
            <div class="tier-info">
              <span class="tier-label">Priority Review:</span>
              <span class="tier-value">≥1000 IMKT</span>
            </div>
            <div class="tier-info">
              <span class="tier-label">Fast Track:</span>
              <span class="tier-value">≥5000 IMKT</span>
            </div>
          </div>
        </div>

        <!-- Resolution Method -->
        <div class="form-group">
          <label for="oracleSource">
            <span class="label-text">Resolution Method</span>
            <span class="required">*</span>
          </label>
          <textarea
            id="oracleSource"
            v-model="formData.oracleSource"
            placeholder="e.g., Based on Bitcoin price at CoinMarketCap on December 31, 2024 23:59:59 UTC"
            class="form-textarea"
            rows="3"
            required
          ></textarea>
          <p class="help-text">Must provide verifiable, objective data source or judgment criteria</p>
        </div>

        <!-- AI Selection -->
        <div class="form-group">
          <label>
            <span class="label-text">Select AI Models</span>
            <span class="required">*</span>
          </label>
          <div class="ai-selection-info">
            <p class="help-text">Select AI models to participate in market resolution. You must select at least <strong>3 AI models</strong> (odd number: 3, 5, 7, 9...).</p>
          </div>
          <div v-if="availableAI.length === 0" class="loading-message">
            Loading AI models...
          </div>
          <div v-else class="ai-grid">
            <label v-for="ai in availableAI" :key="ai.code" class="ai-checkbox-label" :class="{ 'ai-selected': isAISelected(ai.code) }">
              <input
                type="checkbox"
                :value="ai.code"
                :checked="isAISelected(ai.code)"
                @change="handleAICheck(ai.code, $event.target.checked)"
                class="ai-checkbox"
              />
              <span class="ai-checkbox-custom" :class="{ 'checked': isAISelected(ai.code) }"></span>
              <span class="ai-name">{{ ai.desc }}</span>
            </label>
          </div>
        </div>

        <!-- Tags -->
        <div class="form-group">
          <label>
            <span class="label-text">Tags</span>
            <span class="required">*</span>
          </label>
          <p class="help-text">Select up to 5 tags that best describe your market</p>
          <div v-if="isLoadingOptions" class="loading-message">
            Loading tags...
          </div>
          <div v-else class="tags-grid">
            <label
              v-for="tag in availableTags"
              :key="tag.code"
              class="tag-checkbox-label"
              :class="{ 'tag-selected': isTagSelected(tag.code) }"
            >
              <input
                type="checkbox"
                :value="tag.code"
                :checked="isTagSelected(tag.code)"
                @change="handleTagCheck(tag.code, $event.target.checked)"
                class="tag-checkbox"
              />
              <span class="tag-checkbox-custom" :class="{ 'checked': isTagSelected(tag.code) }"></span>
              <span class="tag-name">{{ tag.desc }}</span>
            </label>
          </div>
        </div>

        <!-- Information Box -->
        <div class="info-box">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <circle cx="10" cy="10" r="8" stroke="#8B5CF6" stroke-width="2"/>
            <path d="M10 7V11M10 14H10.01" stroke="#8B5CF6" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <div class="info-content">
            <div class="info-title">Creator Benefits</div>
            <ul class="info-list">
              <li>Staked tokens become the base liquidity source for the market</li>
              <li>As market admin, you are responsible for future event resolution</li>
              <li>After market ends, you'll receive liquidity rewards (X% of trading fees)</li>
              <li>If multiple applications for same topic, higher stake advances to final review</li>
            </ul>
          </div>
        </div>

        <!-- Submit Buttons -->
        <div class="form-actions">
          <button type="button" @click="handleCancel" class="btn btn-secondary">
            Cancel
          </button>
          <button
            type="submit"
            class="btn btn-primary"
            :disabled="isSubmitting || duplicateMarket?.status === 'approved'"
          >
            {{ isSubmitting ? 'Submitting...' : 'Submit for Review' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onErrorCaptured } from 'vue';
import { useRouter } from 'vue-router';
import { mockMarkets } from '../data/markets';
import { getOptions, createMarket } from '../utils/api';
import { useUserStore } from '../stores/user';

export default {
  name: 'CreateMarket',
  setup() {
    const router = useRouter();
    const userStore = useUserStore();

    // 捕获组件渲染错误
    onErrorCaptured((err, instance, info) => {
      console.error('=== Vue Render Error ===');
      console.error('Error:', err);
      console.error('Component:', instance);
      console.error('Info:', info);
      console.error('========================');
      // 返回false阻止错误继续传播
      return false;
    });

    // 检查登录状态
    const isConnected = computed(() => !!userStore.user?.walletAddress);
    const isLoading = ref(false);

    // 连接钱包
    const handleConnect = async () => {
      try {
        isLoading.value = true;
        // 触发Header中的连接钱包事件
        const event = new CustomEvent('connect-wallet');
        window.dispatchEvent(event);
      } catch (error) {
        console.error('Failed to connect wallet:', error);
      } finally {
        isLoading.value = false;
      }
    };

    const formData = ref({
      title: '',
      description: '',
      closeTime: '',   // ISO 8601格式
      baseLiquidity: 1000,
      oracleSource: '',
      aiModel: '',     // 逗号分隔的数字code字符串
      tags: ''         // 逗号分隔的数字code字符串
    });

    // 临时存储选中的 AI 和 tags（用于UI交互）
    const selectedAIList = ref([]);
    const selectedTagsList = ref([]);

    // 辅助函数：检查是否被选中
    const isAISelected = (code) => {
      try {
        if (code === null || code === undefined || code === '') {
          return false;
        }
        const numCode = Number(code);
        if (isNaN(numCode) || !isFinite(numCode)) {
          return false;
        }
        return selectedAIList.value.includes(numCode);
      } catch (error) {
        console.error('Error in isAISelected:', error, 'code:', code);
        return false;
      }
    };

    const isTagSelected = (code) => {
      try {
        if (code === null || code === undefined || code === '') {
          return false;
        }
        const numCode = Number(code);
        if (isNaN(numCode) || !isFinite(numCode)) {
          return false;
        }
        return selectedTagsList.value.includes(numCode);
      } catch (error) {
        console.error('Error in isTagSelected:', error, 'code:', code);
        return false;
      }
    };

    // 手动处理checkbox变化
    const handleAICheck = (aiCode, checked) => {
      try {
        const numCode = Number(aiCode);

        if (checked) {
          // 检查是否已存在
          if (!selectedAIList.value.includes(numCode)) {
            selectedAIList.value = [...selectedAIList.value, numCode];
            console.log('AI added:', numCode, 'Total:', selectedAIList.value.length);
          }
        } else {
          selectedAIList.value = selectedAIList.value.filter(code => code === numCode);
          console.log('AI removed:', numCode, 'Total:', selectedAIList.value.length);
        }
      } catch (error) {
        console.error('Error in handleAICheck:', error);
      }
    };

    const handleTagCheck = (tagCode, checked) => {
      try {
        const numCode = Number(tagCode);

        if (checked) {
          // 检查是否已存在
          if (!selectedTagsList.value.includes(numCode)) {
            selectedTagsList.value = [...selectedTagsList.value, numCode];
            console.log('Tag added:', numCode, 'Total:', selectedTagsList.value.length);
          }
        } else {
          selectedTagsList.value = selectedTagsList.value.filter(code => code === numCode);
          console.log('Tag removed:', numCode, 'Total:', selectedTagsList.value.length);
        }
      } catch (error) {
        console.error('Error in handleTagCheck:', error);
      }
    };

    // 从 API 获取的选项
    const availableTags = ref([]);
    const availableAI = ref([]);
    const isLoadingOptions = ref(false);

    // 默认的 tags（作为降级方案）- code 与后端保持一致（Integer）
    const defaultTags = [
      { code: 1, desc: 'Crypto' },
      { code: 2, desc: 'Technology' },
      { code: 3, desc: 'Politics' },
      { code: 4, desc: 'Sports' },
      { code: 5, desc: 'Finance' },
      { code: 6, desc: 'Entertainment' },
      { code: 7, desc: 'Other' }
    ];

    // 默认的 AI（作为降级方案）- code 与后端保持一致（Integer）
    const defaultAI = [
      { code: 1, desc: 'ChatGpt' },
      { code: 2, desc: 'Claude' },
      { code: 3, desc: 'Gemini' },
      { code: 4, desc: 'Preplexity' },
      { code: 5, desc: 'Grok' },
      { code: 6, desc: '文心一言' },
      { code: 7, desc: '通义千问' },
      { code: 8, desc: '智普清言' },
      { code: 9, desc: 'Kimi' },
      { code: 10, desc: '讯飞星火' }
    ];

    const isSubmitting = ref(false);
    const duplicateMarket = ref(null);
    const allMarkets = ref([]);

    // 加载配置选项
    const loadOptions = async () => {
      try {
        isLoadingOptions.value = true;

        const response = await getOptions('tag,ai');

        if (response && response.data) {
          if (response.data.tag && Array.isArray(response.data.tag)) {
            // 确保 code 是数字类型
            availableTags.value = response.data.tag.map(tag => ({
              ...tag,
              code: Number(tag.code)
            }));
          } else {
            availableTags.value = defaultTags;
          }

          if (response.data.ai && Array.isArray(response.data.ai)) {
            // 确保 code 是数字类型
            availableAI.value = response.data.ai.map(ai => ({
              ...ai,
              code: Number(ai.code)
            }));
          } else {
            availableAI.value = defaultAI;
          }
        } else {
          availableTags.value = defaultTags;
          availableAI.value = defaultAI;
        }
      } catch (err) {
        console.error('Failed to load options:', err);
        availableTags.value = defaultTags;
        availableAI.value = defaultAI;
      } finally {
        isLoadingOptions.value = false;
      }
    };

    // 获取最早结束时间（当前时间+24小时）
    const minEndTime = computed(() => {
      const now = new Date();
      now.setHours(now.getHours() + 24);
      return now.toISOString().slice(0, 16);
    });

    // 加载所有市场数据（包括待审核的）
    onMounted(async () => {
      // 加载配置选项
      await loadOptions();

      allMarkets.value = [...mockMarkets];
      // 这里后续会从localStorage或API加载用户创建的市场
      const userCreated = localStorage.getItem('userCreatedMarkets');
      if (userCreated) {
        allMarkets.value.push(...JSON.parse(userCreated));
      }
    });

    // 检查重复话题
    const checkDuplicate = () => {
      if (!formData.value.title.trim()) {
        duplicateMarket.value = null;
        return;
      }

      // 简单相似度检查（后续可以优化为模糊匹配）
      const duplicate = allMarkets.value.find(market => {
        const title1 = market.title.toLowerCase().trim();
        const title2 = formData.value.title.toLowerCase().trim();
        return title1 === title2 || title1.includes(title2) || title2.includes(title1);
      });

      if (duplicate) {
        duplicateMarket.value = {
          id: duplicate.id,
          status: duplicate.status || 'approved'
        };
      } else {
        duplicateMarket.value = null;
      }
    };

    // 跳转到重复的市场
    const goToMarket = () => {
      if (duplicateMarket.value) {
        router.push(`/market/${duplicateMarket.value.id}`);
      }
    };

    // 提交表单
    const handleSubmit = async () => {
      if (isSubmitting.value) return;
      if (duplicateMarket.value?.status === 'approved') {
        alert('This topic already exists. Cannot submit duplicate.');
        return;
      }

      // 验证 AI 选择
      if (!selectedAIList.value || selectedAIList.value.length === 0) {
        alert('Please select at least 3 AI models (must be odd number)');
        return;
      }
      if (selectedAIList.value.length < 3) {
        alert(`You selected ${selectedAIList.value.length} AI model(s). Please select at least 3 AI models`);
        return;
      }
      if (selectedAIList.value.length % 2 === 0) {
        alert(`You selected ${selectedAIList.value.length} AI model(s). Please select an odd number (3, 5, 7, 9)`);
        return;
      }

      // 验证 Tags 选择
      if (!selectedTagsList.value || selectedTagsList.value.length === 0) {
        alert('Please select at least one tag');
        return;
      }
      if (selectedTagsList.value.length > 5) {
        alert('Please select no more than 5 tags');
        return;
      }

      // 从 localStorage 获取 userId
      const userInfoStr = localStorage.getItem('userInfo');
      if (!userInfoStr) {
        alert('Please login first');
        router.push('/markets');
        return;
      }
      const userInfo = JSON.parse(userInfoStr);
      const userId = userInfo.userId;

      isSubmitting.value = true;

      try {
        // 将 closeTime 转换为 ISO 8601 格式
        const closeTimeISO = new Date(formData.value.closeTime).toISOString();

        // 准备提交数据（字段名与后端数据库表一致）
        const submitData = {
          title: formData.value.title,
          description: formData.value.description,
          closeTime: closeTimeISO,
          baseLiquidity: formData.value.baseLiquidity,
          oracleSource: formData.value.oracleSource,
          aiModel: selectedAIList.value.join(','),
          tags: selectedTagsList.value.join(',')
        };

        console.log('[CreateMarket] Submitting data:', submitData);
        console.log('[CreateMarket] User ID:', userId);

        // 调用后端 API
        const response = await createMarket(submitData, userId);
        console.log('[CreateMarket] API Response:', response);

        if (response && response.data) {
          // 重置表单
          formData.value = {
            title: '',
            description: '',
            closeTime: '',
            baseLiquidity: 1000,
            oracleSource: '',
            aiModel: '',
            tags: ''
          };
          selectedAIList.value = [];
          selectedTagsList.value = [];
          duplicateMarket.value = null;

          // 跳转到我的市场页面
          router.push({ path: '/markets', query: { filter: 'my' } });
        } else {
          throw new Error('Invalid response');
        }

      } catch (error) {
        console.error('Failed to create market:', error);
        alert(`Failed to create market: ${error.message || 'Unknown error'}`);
      } finally {
        isSubmitting.value = false;
      }
    };

    // 取消创建
    const handleCancel = () => {
      if (confirm('Are you sure you want to cancel? All entered data will be lost.')) {
        router.push('/markets');
      }
    };

    return {
      formData,
      selectedAIList,
      selectedTagsList,
      isSubmitting,
      duplicateMarket,
      minEndTime,
      availableTags,
      availableAI,
      isLoadingOptions,
      isAISelected,
      isTagSelected,
      handleAICheck,
      handleTagCheck,
      checkDuplicate,
      goToMarket,
      handleSubmit,
      handleCancel,
      isConnected,
      isLoading,
      handleConnect
    };
  }
};
</script>

<style scoped>
.create-market-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px 80px 20px;
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  box-sizing: border-box;
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
}

/* 未登录提示 */
.login-prompt {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  padding: 60px 20px;
}

.login-prompt-content {
  text-align: center;
  max-width: 480px;
}

.login-icon {
  color: var(--accent-light);
  margin-bottom: 24px;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 12px 0;
}

.login-description {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0 0 32px 0;
  line-height: 1.5;
}

.btn-connect-large {
  width: 100%;
  max-width: 280px;
  padding: 16px 24px;
  background: var(--accent-light);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-connect-large:hover:not(:disabled) {
  background: #4F46E5;
  transform: translateY(-1px);
}

.btn-connect-large:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 自定义滚动条样式 */
.create-market-container::-webkit-scrollbar {
  width: 8px;
}

.create-market-container::-webkit-scrollbar-track {
  background: var(--bg-secondary);
  border-radius: 4px;
}

.create-market-container::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 4px;
}

.create-market-container::-webkit-scrollbar-thumb:hover {
  background: var(--border-hover);
}

.create-market-card {
  background: var(--card-bg);
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  display: block !important;
  visibility: visible !important;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: var(--text-primary);
}

.page-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 32px 0;
  line-height: 1.5;
}

.market-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
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

.form-input,
.form-textarea,
.form-select {
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

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  outline: none;
  border-color: var(--accent-color);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.help-text {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0;
}

/* 重复警告 */
.duplicate-warning {
  margin-top: 8px;
  padding: 12px 16px;
  background: rgba(245, 158, 11, 0.1);
  border: 1px solid #F59E0B;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.warning-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex: 1;
}

.warning-text {
  flex: 1;
}

.warning-title {
  font-size: 14px;
  font-weight: 600;
  color: #F59E0B;
  margin-bottom: 4px;
}

.warning-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.view-market-btn {
  padding: 6px 12px;
  background: #F59E0B;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s;
}

.view-market-btn:hover {
  background: #D97706;
}

/* 质押输入 */
.stake-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.stake-input {
  padding-right: 80px !important;
}

.token-symbol {
  position: absolute;
  right: 16px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
}

.stake-tiers {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 8px;
  padding: 12px;
  background: rgba(99, 102, 241, 0.1);
  border-radius: 8px;
}

.tier-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}

.tier-label {
  color: var(--text-secondary);
}

.tier-value {
  color: var(--accent-light);
  font-weight: 600;
}

/* 标签 */
.loading-message {
  padding: 12px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 14px;
}

.tags-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
  margin-top: 8px;
}

.tag-checkbox-label {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.tag-checkbox-label:hover {
  background: rgba(99, 102, 241, 0.05);
  border-color: var(--accent-light);
}

.tag-checkbox-label.tag-selected {
  background: rgba(99, 102, 241, 0.1);
  border-color: var(--accent-light);
}

.tag-checkbox {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.tag-checkbox-custom {
  position: relative;
  width: 20px;
  height: 20px;
  border: 2px solid var(--border-color);
  border-radius: 4px;
  background: var(--input-bg);
  flex-shrink: 0;
  transition: all 0.2s;
}

.tag-checkbox-custom.checked {
  background: var(--accent-light);
  border-color: var(--accent-light);
}

.tag-checkbox-custom.checked::after {
  content: '';
  position: absolute;
  left: 6px;
  top: 2px;
  width: 5px;
  height: 10px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.tag-name {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

/* 信息框 */
.info-box {
  padding: 16px;
  background: rgba(99, 102, 241, 0.1);
  border: 1px solid var(--accent-light);
  border-radius: 8px;
  display: flex;
  gap: 12px;
}

/* AI选择 */
.ai-selection-info {
  margin-bottom: 12px;
}

.ai-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.ai-checkbox-label {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.ai-checkbox-label:hover {
  background: rgba(99, 102, 241, 0.05);
  border-color: var(--accent-light);
}

.ai-checkbox {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.ai-checkbox-custom {
  position: relative;
  width: 20px;
  height: 20px;
  border: 2px solid var(--border-color);
  border-radius: 4px;
  background: var(--input-bg);
  flex-shrink: 0;
  transition: all 0.2s;
}

.ai-checkbox-custom.checked {
  background: var(--accent-light);
  border-color: var(--accent-light);
}

.ai-checkbox-custom.checked::after {
  content: '';
  position: absolute;
  left: 6px;
  top: 2px;
  width: 5px;
  height: 10px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.ai-checkbox-label:has(.ai-checkbox:checked),
.ai-checkbox-label.ai-selected {
  background: rgba(99, 102, 241, 0.1);
  border-color: var(--accent-light);
}

.ai-name {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.validation-error {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid #EF4444;
  border-radius: 8px;
  font-size: 13px;
  color: #EF4444;
  margin-top: 8px;
}

.validation-success {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid #10B981;
  border-radius: 8px;
  font-size: 13px;
  color: #10B981;
  margin-top: 8px;
}


.info-content {
  flex: 1;
}

.info-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--accent-light);
  margin-bottom: 8px;
}

.info-list {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.info-list li {
  margin-bottom: 4px;
}

/* 按钮组 */
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
  opacity: 0.5;
  cursor: not-allowed;
}

/* 响应式 */
@media (max-width: 768px) {
  .create-market-card {
    padding: 24px;
  }

  .page-title {
    font-size: 24px;
  }

  .ai-grid {
    grid-template-columns: 1fr;
  }

  .tags-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  }

  .duplicate-warning {
    flex-direction: column;
    align-items: stretch;
  }

  .view-market-btn {
    width: 100%;
    text-align: center;
  }

  .form-actions {
    flex-direction: column-reverse;
  }
}
</style>
