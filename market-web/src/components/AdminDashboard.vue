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
        :class="['tab-button', { active: activeTab === 'rewards' }]"
        @click="activeTab = 'rewards'"
      >
        Liquidity Rewards
      </button>
      <button
        :class="['tab-button', { active: activeTab === 'settled' }]"
        @click="activeTab = 'settled'"
      >
        Settled Markets
      </button>
    </div>

    <!-- 管理的市场列表 -->
    <div v-if="activeTab === 'managed'" class="managed-markets-section">
      <h2 class="section-title">My Managed Markets</h2>
      <p class="section-desc">As creator, you are responsible for settling these markets</p>

      <div v-if="managedMarkets.length > 0" class="markets-grid">
        <div
          v-for="market in managedMarkets"
          :key="market.id"
          class="market-card"
        >
          <div class="market-header">
            <span class="market-category">{{ getCategoryName(market.category) }}</span>
            <span :class="['market-status', market.status]">
              {{ getStatusText(market.status) }}
            </span>
          </div>
          <h3 class="market-title">{{ market.title }}</h3>
          <div class="market-stats">
            <div class="stat">
              <span class="stat-label">流动性</span>
              <span class="stat-value">{{ formatNumber(market.liquidity) }} IMKT</span>
            </div>
            <div class="stat">
              <span class="stat-label">交易量</span>
              <span class="stat-value">{{ formatNumber(market.volume) }} IMKT</span>
            </div>
            <div class="stat">
              <span class="stat-label">剩余时间</span>
              <span class="stat-value">{{ getTimeRemaining(market.endTime) }}</span>
            </div>
          </div>
          <div class="market-rewards">
            <div class="reward-item">
              <span class="reward-label">Estimated Reward</span>
              <span class="reward-value">{{ calculateReward(market) }} IMKT</span>
            </div>
          </div>
          <div class="market-actions">
            <button
              @click="viewMarket(market.id)"
              class="action-btn view"
            >
              View Details
            </button>
            <button
              v-if="canSettle(market)"
              @click="settleMarket(market)"
              class="action-btn settle"
            >
              Settle Market
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

    <!-- Liquidity Rewards -->
    <div v-if="activeTab === 'rewards'" class="rewards-section">
      <h2 class="section-title">Liquidity Rewards</h2>
      <p class="section-desc">After market settles, you receive trading fee rewards</p>

      <div v-if="rewardHistory.length > 0" class="rewards-list">
        <div class="reward-summary-card">
          <div class="summary-item">
            <span class="summary-label">Total Rewards Earned</span>
            <span class="summary-value">{{ totalRewards }} IMKT</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">Pending Rewards</span>
            <span class="summary-value pending">{{ pendingRewards }} IMKT</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">Claimed Rewards</span>
            <span class="summary-value">{{ claimedRewards }} IMKT</span>
          </div>
        </div>

        <div class="reward-history">
          <h3 class="history-title">Reward History</h3>
          <div class="history-list">
            <div
              v-for="reward in rewardHistory"
              :key="reward.id"
              class="history-item"
            >
              <div class="history-info">
                <div class="history-market">{{ reward.marketTitle }}</div>
                <div class="history-date">{{ formatDate(reward.settledAt) }}</div>
              </div>
              <div :class="['history-amount', reward.status]">
                +{{ reward.amount }} IMKT
              </div>
              <div class="history-status">
                {{ reward.status === 'claimed' ? '已领取' : '待领取' }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
          <path d="M32 4L16 20V56H48V20L32 4Z" stroke="var(--border-color)" stroke-width="2" stroke-linejoin="round"/>
          <path d="M32 24V40M32 48H32.01" stroke="var(--border-color)" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <div class="empty-title">No reward records yet</div>
        <div class="empty-desc">成功Settle Market后将获得Liquidity Rewards</div>
      </div>
    </div>

    <!-- Settled Markets -->
    <div v-if="activeTab === 'settled'" class="settled-section">
      <h2 class="section-title">Settled Markets</h2>
      <p class="section-desc">View markets you have settled</p>

      <div v-if="settledMarkets.length > 0" class="settled-list">
        <div
          v-for="market in settledMarkets"
          :key="market.id"
          class="settled-card"
        >
          <div class="settled-info">
            <div class="settled-category">{{ getCategoryName(market.category) }}</div>
            <h3 class="settled-title">{{ market.title }}</h3>
            <div class="settled-result">
              <span class="result-label">Resolution Result：</span>
              <span :class="['result-value', market.outcome]">
                {{ market.outcome === 'yes' ? 'YES' : 'NO' }}
              </span>
            </div>
          </div>
          <div class="settled-stats">
            <div class="settled-stat">
              <span class="stat-label">交易量</span>
              <span class="stat-value">{{ formatNumber(market.volume) }} IMKT</span>
            </div>
            <div class="settled-stat">
              <span class="stat-label">Participants</span>
              <span class="stat-value">{{ market.participants || 0 }}</span>
            </div>
            <div class="settled-stat">
              <span class="stat-label">奖励</span>
              <span class="stat-value reward">{{ calculateReward(market) }} IMKT</span>
            </div>
          </div>
          <div class="settled-time">
            Settled {{ formatDate(market.settledAt) }}
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
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { mockMarkets } from '../data/markets';

export default {
  name: 'AdminDashboard',
  setup() {
    const router = useRouter();
    const activeTab = ref('managed');
    const managedMarkets = ref([]);
    const rewardHistory = ref([]);
    const settledMarkets = ref([]);

    onMounted(() => {
      loadData();
    });

    const loadData = () => {
      // 加载用户创建的市场
      const userCreated = JSON.parse(localStorage.getItem('userCreatedMarkets') || '[]');
      managedMarkets.value = userCreated;

      // 加载已结算的市场
      settledMarkets.value = userCreated.filter(m => m.status === 'settled');

      // 加载Reward History（模拟数据）
      const savedRewards = JSON.parse(localStorage.getItem('rewardHistory') || '[]');
      if (savedRewards.length > 0) {
        rewardHistory.value = savedRewards;
      } else {
        // 创建一些模拟奖励数据
        rewardHistory.value = [
          {
            id: 'reward-1',
            marketId: 'market-1',
            marketTitle: '比特币价格在2024年12月31日会超过10万美元吗？',
            amount: '125.50',
            status: 'claimed',
            settledAt: Date.now() - 86400000 * 7
          },
          {
            id: 'reward-2',
            marketId: 'market-2',
            marketTitle: '以太坊将在2024年通过POS转换吗？',
            amount: '89.30',
            status: 'pending',
            settledAt: Date.now() - 86400000 * 3
          }
        ];
        localStorage.setItem('rewardHistory', JSON.stringify(rewardHistory.value));
      }
    };

    // 统计数据
    const totalMarkets = computed(() => {
      return mockMarkets.length + managedMarkets.value.length;
    });

    const activeMarkets = computed(() => {
      const mockApproved = mockMarkets.filter(m => m.status === 'approved' || !m.status).length;
      const userApproved = managedMarkets.value.filter(m => m.status === 'approved').length;
      return mockApproved + userApproved;
    });

    const pendingMarkets = computed(() => {
      return managedMarkets.value.filter(m => m.status === 'pending').length;
    });

    const totalLiquidity = computed(() => {
      const mockLiquidity = mockMarkets.reduce((sum, m) => sum + (m.liquidity || 0), 0);
      const userLiquidity = managedMarkets.value.reduce((sum, m) => sum + (m.liquidity || 0), 0);
      return mockLiquidity + userLiquidity;
    });

    const totalRewards = computed(() => {
      return rewardHistory.value.reduce((sum, r) => sum + parseFloat(r.amount), 0).toFixed(2);
    });

    const pendingRewards = computed(() => {
      return rewardHistory.value
        .filter(r => r.status === 'pending')
        .reduce((sum, r) => sum + parseFloat(r.amount), 0)
        .toFixed(2);
    });

    const claimedRewards = computed(() => {
      return rewardHistory.value
        .filter(r => r.status === 'claimed')
        .reduce((sum, r) => sum + parseFloat(r.amount), 0)
        .toFixed(2);
    });

    // 辅助方法
    const formatNumber = (num) => {
      if (!num) return '0';
      return num.toLocaleString('en-US', { maximumFractionDigits: 0 });
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

    const getStatusText = (status) => {
      const statusMap = {
        'pending': '审核中',
        'approved': '活跃',
        'rejected': '已拒绝',
        'settled': '已结算'
      };
      return statusMap[status] || status;
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

    const calculateReward = (market) => {
      // 假设奖励是交易量的10%
      if (!market.volume) return '0';
      return (market.volume * 0.1).toFixed(2);
    };

    const canSettle = (market) => {
      return market.endTime && Date.now() > market.endTime && market.status === 'approved';
    };

    // 操作方法
    const viewMarket = (marketId) => {
      router.push(`/market/${marketId}`);
    };

    const createMarket = () => {
      router.push('/create-market');
    };

    const settleMarket = (market) => {
      const outcome = confirm(`Settle Market：${market.title}\n\nPlease select result. Confirm = YES, Cancel = NO`);

      if (outcome !== null) {
        market.outcome = outcome ? 'yes' : 'no';
        market.status = 'settled';
        market.settledAt = Date.now();

        // 保存更改
        const userCreated = JSON.parse(localStorage.getItem('userCreatedMarkets') || '[]');
        const index = userCreated.findIndex(m => m.id === market.id);
        if (index !== -1) {
          userCreated[index] = market;
          localStorage.setItem('userCreatedMarkets', JSON.stringify(userCreated));
        }

        // 添加奖励记录
        const reward = {
          id: `reward-${Date.now()}`,
          marketId: market.id,
          marketTitle: market.title,
          amount: calculateReward(market),
          status: 'pending',
          settledAt: Date.now()
        };
        rewardHistory.value.unshift(reward);
        localStorage.setItem('rewardHistory', JSON.stringify(rewardHistory.value));

        alert('Market settled successfully! Rewards will be distributed within 24 hours。');
        loadData();
      }
    };

    return {
      activeTab,
      managedMarkets,
      settledMarkets,
      rewardHistory,
      totalMarkets,
      activeMarkets,
      pendingMarkets,
      totalLiquidity,
      totalRewards,
      pendingRewards,
      claimedRewards,
      formatNumber,
      getCategoryName,
      getStatusText,
      formatDate,
      getTimeRemaining,
      calculateReward,
      canSettle,
      viewMarket,
      createMarket,
      settleMarket
    };
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

.market-status.approved {
  background: rgba(34, 197, 94, 0.1);
  color: #22C55E;
}

.market-status.settled {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
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

.market-rewards {
  padding: 12px;
  background: rgba(139, 92, 246, 0.1);
  border-radius: 8px;
}

.reward-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.reward-label {
  color: var(--text-secondary);
}

.reward-value {
  font-weight: 700;
  color: var(--accent-light);
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

/* 奖励区块 */
.reward-summary-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 24px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.summary-item {
  text-align: center;
}

.summary-label {
  display: block;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.summary-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.summary-value.pending {
  color: #F59E0B;
}

.history-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 16px 0;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 16px;
  display: grid;
  grid-template-columns: 2fr auto auto;
  gap: 16px;
  align-items: center;
}

.history-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.history-market {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.history-date {
  font-size: 12px;
  color: var(--text-secondary);
}

.history-amount {
  font-size: 18px;
  font-weight: 700;
  text-align: right;
}

.history-amount.claimed {
  color: #22C55E;
}

.history-amount.pending {
  color: #F59E0B;
}

.history-status {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
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
  grid-template-columns: 2fr 1fr auto;
  gap: 20px;
  align-items: center;
}

.settled-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.settled-category {
  display: inline-block;
  padding: 4px 12px;
  background: var(--input-bg);
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  width: fit-content;
}

.settled-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.4;
}

.settled-result {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.result-label {
  color: var(--text-secondary);
}

.result-value {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 700;
}

.result-value.yes {
  background: rgba(34, 197, 94, 0.1);
  color: #22C55E;
}

.result-value.no {
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
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

.settled-stat .stat-value.reward {
  color: var(--accent-light);
  font-weight: 700;
}

.settled-time {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
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

/* 响应式 */
@media (max-width: 768px) {
  .stats-section {
    grid-template-columns: 1fr;
  }

  .markets-grid {
    grid-template-columns: 1fr;
  }

  .reward-summary-card {
    grid-template-columns: 1fr;
  }

  .history-item {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .settled-card {
    grid-template-columns: 1fr;
  }
}
</style>
