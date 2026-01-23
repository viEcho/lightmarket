<template>
  <div class="admin-dashboard-container">
    <!-- 顶部统计卡片 -->
    <div class="stats-section">
      <div class="stat-card total">
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
            <path d="M3 9H21M9 21V9" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ totalMarkets }}</div>
          <div class="stat-label">Total Markets</div>
        </div>
      </div>

      <div class="stat-card active">
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ activeMarkets }}</div>
          <div class="stat-label">Active Markets</div>
        </div>
      </div>

      <div class="stat-card pending">
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/>
            <path d="M12 8V12L15 15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ pendingMarkets }}</div>
          <div class="stat-label">Pending Review</div>
        </div>
      </div>

      <div class="stat-card liquidity">
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(totalLiquidity) }}</div>
          <div class="stat-label">Total Liquidity (IMKT)</div>
        </div>
      </div>
    </div>

    <!-- Tab导航 -->
    <div class="dashboard-tabs">
      <button
        :class="['tab-button', { active: activeTab === 'managed' }]"
        @click="activeTab = 'managed'"
      >
        My Managed Markets
      </button>
      <button
        :class="['tab-button', { active: activeTab === 'settled' }]"
        @click="activeTab = 'settled'"
      >
        Settled Markets
      </button>
      <button
        :class="['tab-button', { active: activeTab === 'users' }]"
        @click="activeTab = 'users'"
      >
        👥 User Management
      </button>
    </div>

    <!-- 市场详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2 v-if="selectedMarket" class="modal-title">{{ selectedMarket.title }}</h2>
          <button @click="closeModal" class="modal-close">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
        <div v-if="selectedMarket" class="modal-body">
          <div class="detail-section">
            <p class="detail-description">{{ selectedMarket.description }}</p>
          </div>

          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">Status</span>
              <span :class="['detail-value', 'status-badge', getMarketStatusClass(selectedMarket.marketStatus)]">
                {{ getMarketStatusText(selectedMarket.marketStatus) }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Creator</span>
              <span class="detail-value">{{ selectedMarket.creator || 'Unknown' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Created Time</span>
              <span class="detail-value">{{ formatDate(selectedMarket.createdTime) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Close Time</span>
              <span class="detail-value">{{ formatDate(selectedMarket.closeTime) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Base Liquidity</span>
              <span class="detail-value">{{ formatNumber(selectedMarket.baseLiquidity) }} IMKT</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Total Volume</span>
              <span class="detail-value">{{ formatNumber(selectedMarket.totalVolume) }} IMKT</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Oracle Source</span>
              <span class="detail-value">{{ selectedMarket.oracleSource || 'N/A' }}</span>
            </div>
          </div>

          <div v-if="selectedMarket.tags && selectedMarket.tags.length > 0" class="detail-section">
            <h4 class="detail-subtitle">Tags</h4>
            <div class="tags-list">
              <span v-for="tag in selectedMarket.tags" :key="tag.code" class="detail-tag">
                {{ tag.desc }}
              </span>
            </div>
          </div>

          <div v-if="selectedMarket.aiModels && selectedMarket.aiModels.length > 0" class="detail-section">
            <h4 class="detail-subtitle">AI Models</h4>
            <div class="tags-list">
              <span v-for="ai in selectedMarket.aiModels" :key="ai.code" class="detail-tag">
                {{ ai.desc }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 管理的市场列表 -->
    <div v-if="activeTab === 'managed'" class="managed-markets-section">
      <h2 class="section-title">My Managed Markets</h2>
      <p class="section-desc">As admin, you are responsible for settling these markets</p>

      <div v-if="managedMarkets.length > 0" class="markets-grid">
        <div
          v-for="market in managedMarkets"
          :key="market.marketId"
          class="market-card"
        >
          <div class="market-header">
            <span class="market-category">{{ formatTags(market.tags) || '其他' }}</span>
            <span :class="['market-status', getMarketStatusClass(market.marketStatus, true)]">
              {{ getMarketStatusText(market.marketStatus) }}
            </span>
          </div>
          <h3 class="market-title">{{ market.title }}</h3>
          <div class="market-stats">
            <div class="stat">
              <span class="stat-label">流动性</span>
              <span class="stat-value">{{ formatNumber(market.baseLiquidity) }} IMKT</span>
            </div>
            <div class="stat">
              <span class="stat-label">交易量</span>
              <span class="stat-value">{{ formatNumber(market.totalVolume) }} IMKT</span>
            </div>
            <div class="stat">
              <span class="stat-label">剩余时间</span>
              <span class="stat-value">{{ getTimeRemaining(market.closeTime) }}</span>
            </div>
          </div>
          <div class="market-actions">
            <button
              @click="viewMarket(market)"
              class="action-btn view"
            >
              View Details
            </button>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
          <rect x="8" y="8" width="48" height="48" rx="4" stroke="var(--border-color)" stroke-width="2"/>
          <path d="M24 32H40M32 24V40" stroke="var(--border-color)" stroke-width="2"/>
        </svg>
        <div class="empty-title">No managed markets yet</div>
        <div class="empty-desc">Create your first prediction market!</div>
        <button @click="createMarket" class="create-btn">
          Create Market
        </button>
      </div>
    </div>

    <!-- Settled Markets -->
    <div v-if="activeTab === 'settled'" class="settled-section">
      <h2 class="section-title">Settled Markets</h2>
      <p class="section-desc">View markets which were settled</p>

      <div v-if="settledMarkets.length > 0" class="settled-list">
        <div
          v-for="market in settledMarkets"
          :key="market.marketId"
          class="settled-card"
        >
          <div class="settled-info">
            <div class="settled-tags">
              <template v-if="market.tags && market.tags.length > 0">
                <span v-for="tag in market.tags" :key="tag.code" class="settled-tag">
                  {{ tag.desc }}
                </span>
              </template>
              <span v-else class="settled-tag">其他</span>
            </div>
            <h3 class="settled-title">{{ market.title }}</h3>
            <div class="settled-time">
              Settled {{ formatDate(market.updatedTime) }}
            </div>
          </div>
          <div class="settled-stats">
            <div class="settled-stat">
              <span class="stat-label">交易量</span>
              <span class="stat-value">{{ formatNumber(market.totalVolume) }} IMKT</span>
            </div>
            <div class="settled-stat">
              <span class="stat-label">流动性</span>
              <span class="stat-value">{{ formatNumber(market.baseLiquidity) }} IMKT</span>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
          <circle cx="32" cy="32" r="24" stroke="var(--border-color)" stroke-width="2"/>
          <path d="M32 16V32L40 40" stroke="var(--border-color)" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <div class="empty-title">No settled markets yet</div>
        <div class="empty-desc">Settle markets promptly after they end to receive rewards</div>
      </div>
    </div>

    <!-- User Management -->
    <div v-if="activeTab === 'users'" class="users-section">
      <UserManagement />
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getAdminStatistics, getAdminApproveList } from '../utils/api';
import { getMarketStatusText, getMarketStatusClass } from '../constants/marketStatus';
import UserManagement from './UserManagement.vue';

export default {
  name: 'AdminDashboard',
  setup() {
    const router = useRouter();
    const activeTab = ref('managed');
    const managedMarkets = ref([]);
    const settledMarkets = ref([]);
    const showDetailModal = ref(false);
    const selectedMarket = ref(null);

    // 统计数据
    const totalMarkets = ref(0);
    const activeMarkets = ref(0);
    const pendingMarkets = ref(0);
    const totalLiquidity = ref(0);

    onMounted(() => {
      loadStatistics();
      loadManagedMarkets();
    });

    // 监听tab切换，只在切换到settled时才加载数据
    watch(activeTab, (newTab) => {
      if (newTab === 'settled' && settledMarkets.value.length === 0) {
        loadSettledMarkets();
      }
    });

    // 加载统计数据
    const loadStatistics = async () => {
      try {
        const response = await getAdminStatistics();
        if (response.success && response.data) {
          totalMarkets.value = response.data.totalMarkets || 0;
          activeMarkets.value = response.data.activeMarkets || 0;
          pendingMarkets.value = response.data.pendingReview || 0;
          totalLiquidity.value = response.data.totalLiquidity || 0;
        }
      } catch (error) {
        console.error('Failed to load statistics:', error);
      }
    };

    // 加载管理的市场（非审批拒绝的市场）
    const loadManagedMarkets = async () => {
      try {
        const response = await getAdminApproveList({
          num: 1,
          size: 10,
          excludeRejected: true
        });
        if (response.success && response.data) {
          managedMarkets.value = response.data.list || [];
        }
      } catch (error) {
        console.error('Failed to load managed markets:', error);
      }
    };

    // 加载已结算的市场
    const loadSettledMarkets = async () => {
      try {
        const response = await getAdminApproveList({
          num: 1,
          size: 10,
          marketStatus: 8
        });
        if (response.success && response.data) {
          settledMarkets.value = response.data.list || [];
        }
      } catch (error) {
        console.error('Failed to load settled markets:', error);
      }
    };

    // 辅助方法
    const formatNumber = (num) => {
      if (!num) return '0';
      return num.toLocaleString('en-US', { maximumFractionDigits: 0 });
    };

    const formatTags = (tags) => {
      if (!tags || !Array.isArray(tags)) return '';
      return tags.map(tag => tag.desc).join(', ');
    };

    const getCategoryName = (category) => {
      const categories = {
        crypto: '加密货币',
        technology: '科技',
        politics: '政治',
        sports: '体育',
        finance: '金融',
        entertainment: '娱乐',
        other: '其他'
      };
      return categories[category] || category;
    };

    const formatDate = (timestamp) => {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      });
    };

    const getTimeRemaining = (endTime) => {
      if (!endTime) return '--';
      const now = Date.now();
      const diff = endTime - now;

      if (diff <= 0) return '已结束';

      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      if (days > 0) return `${days}天`;
      return '即将结束';
    };

    const canSettle = (market) => {
      return market.closeTime && Date.now() > new Date(market.closeTime).getTime() && market.marketStatus === 4;
    };

    // 操作方法
    const viewMarket = (market) => {
      selectedMarket.value = market;
      showDetailModal.value = true;
    };

    const closeModal = () => {
      showDetailModal.value = false;
      selectedMarket.value = null;
    };

    const createMarket = () => {
      router.push('/create-market');
    };

    const settleMarket = (market) => {
      const outcome = confirm(`Settle Market：${market.title}\n\nPlease select result. Confirm = YES, Cancel = NO`);

      if (outcome !== null) {
        // TODO: 调用结算 API
        alert('Market settlement feature coming soon!');
      }
    };

    const getOutcomeClass = (outcome) => {
      if (outcome === 1) return 'yes';
      if (outcome === 2) return 'no';
      return 'invalid';
    };

    const getOutcomeText = (outcome) => {
      if (outcome === 1) return 'YES';
      if (outcome === 2) return 'NO';
      return 'Invalid';
    };

    return {
      activeTab,
      managedMarkets,
      settledMarkets,
      showDetailModal,
      selectedMarket,
      totalMarkets,
      activeMarkets,
      pendingMarkets,
      totalLiquidity,
      formatNumber,
      formatTags,
      getCategoryName,
      getMarketStatusText,
      getMarketStatusClass,
      getOutcomeClass,
      getOutcomeText,
      formatDate,
      getTimeRemaining,
      canSettle,
      viewMarket,
      closeModal,
      createMarket,
      settleMarket
    };
  },
  components: {
    UserManagement
  }
};
</script>

<style scoped>
.admin-dashboard-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 20px;
}

/* 统计卡片 */
.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-card.total .stat-icon { color: #8B5CF6; }
.stat-card.active .stat-icon { color: #22C55E; }
.stat-card.pending .stat-icon { color: #F59E0B; }
.stat-card.liquidity .stat-icon { color: #3B82F6; }

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(139, 92, 246, 0.1);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}

/* Tab导航 */
.dashboard-tabs {
  display: flex;
  gap: 8px;
  background: var(--card-bg);
  padding: 8px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  margin-bottom: 32px;
}

.tab-button {
  flex: 1;
  padding: 12px 20px;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-button:hover {
  background: var(--input-bg);
}

.tab-button.active {
  background: var(--accent-light);
  color: white;
}

/* 区块 */
.section-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.section-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 24px 0;
}

/* 管理的市场 */
.markets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.market-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.market-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.market-category {
  padding: 4px 12px;
  background: var(--accent-light);
  color: white;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.market-status {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.market-status.pending {
  background: rgba(245, 158, 11, 0.1);
  color: #F59E0B;
}

.market-status.rejected {
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
}

.market-status.review {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.market-status.approved {
  background: rgba(34, 197, 94, 0.1);
  color: #22C55E;
}

.market-status.active {
  background: rgba(34, 197, 94, 0.1);
  color: #22C55E;
}

.market-status.closed {
  background: rgba(107, 114, 128, 0.1);
  color: #6B7280;
}

.market-status.resolving {
  background: rgba(168, 85, 247, 0.1);
  color: #A855F7;
}

.market-status.challenging {
  background: rgba(236, 72, 153, 0.1);
  color: #EC4899;
}

.market-status.settled {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.market-status.preliminary {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.market-status.final {
  background: rgba(34, 197, 94, 0.1);
  color: #22C55E;
}

.market-status.deploying {
  background: rgba(168, 85, 247, 0.1);
  color: #A855F7;
}

.market-status.arbitrating {
  background: rgba(168, 85, 247, 0.1);
  color: #A855F7;
}

.market-status.final-arbitrated {
  background: rgba(236, 72, 153, 0.1);
  color: #EC4899;
}

.market-status.settling {
  background: rgba(245, 158, 11, 0.1);
  color: #F59E0B;
}

.market-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.market-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: var(--input-bg);
  border-radius: 8px;
}

.stat {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.stat-label {
  color: var(--text-secondary);
}

.stat-value {
  font-weight: 600;
  color: var(--text-primary);
}

.market-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  flex: 1;
  padding: 10px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.action-btn.view {
  background: transparent;
  color: var(--accent-light);
  border: 1px solid var(--accent-light);
}

.action-btn.view:hover {
  background: var(--accent-light);
  color: white;
}

.action-btn.settle {
  background: var(--accent-light);
  color: white;
}

.action-btn.settle:hover {
  background: #7C3AED;
}

/* Settled Markets */
.settled-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.settled-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 60px;
  align-items: center;
}

.settled-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.settled-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.settled-tag {
  padding: 4px 12px;
  background: var(--accent-light);
  color: white;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.settled-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.4;
}

.settled-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.settled-stat {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.settled-time {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: var(--card-bg);
  border: 1px dashed var(--border-color);
  border-radius: 12px;
}

.empty-state svg {
  margin-bottom: 16px;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.create-btn {
  padding: 12px 24px;
  background: var(--accent-light);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.create-btn:hover {
  background: #7C3AED;
  transform: translateY(-1px);
}

/* Modal 弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: var(--card-bg);
  border-radius: 16px;
  max-width: 600px;
  width: 100%;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid var(--border-color);
}

.modal-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.modal-close {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
}

.modal-close:hover {
  color: var(--text-primary);
}

.modal-body {
  padding: 24px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-category {
  display: inline-block;
  padding: 4px 12px;
  background: var(--accent-light);
  color: white;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 12px;
}

.detail-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.detail-description {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}

.detail-subtitle {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 12px 0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
}

.detail-value {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 600;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
  width: fit-content;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-tag {
  padding: 6px 12px;
  background: var(--accent-light);
  color: white;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-section {
    grid-template-columns: 1fr;
  }

  .markets-grid {
    grid-template-columns: 1fr;
  }

  .settled-card {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .modal-content {
    max-height: 90vh;
  }
}

/* User Management Section */
.users-section {
  padding: 20px;
}
</style>
