<template>
  <div class="market-detail-wrapper">
    <!-- 左侧：状态进度区域 -->
    <div v-if="market" class="left-sidebar">
      <div class="sidebar-title">Market Progress</div>
      <div class="timeline-vertical">
        <div
          v-for="(stage, index) in lifecycleStages"
          :key="index"
          :class="['timeline-item-vertical', { active: stage.status === market.marketStatus, completed: isStageCompleted(stage.status) }]"
        >
          <div class="timeline-marker-vertical">
            <div class="timeline-dot-vertical"></div>
            <div v-if="index < lifecycleStages.length - 1" class="timeline-line-vertical"></div>
          </div>
          <div class="timeline-content-vertical">
            <div class="timeline-status-vertical">{{ stage.label }}</div>
            <div class="timeline-time-vertical">{{ stage.time }}</div>
            <div v-if="stage.description" class="timeline-desc-vertical">{{ stage.description }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 中间：趋势盘口区域 -->
    <div class="center-content">
      <!-- 审核状态横幅 -->
      <div v-if="market" :class="['status-banner', getBannerClass(market.marketStatus)]">
        <div class="banner-content">
          <div class="banner-icon">
            <svg v-if="[0, 2, 3].includes(market.marketStatus)" width="24" height="24" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/>
              <path d="M12 8V12L15 15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <svg v-else-if="[4, 5].includes(market.marketStatus)" width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M9 12L11 14L15 10M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="9" stroke="#EF4444" stroke-width="2"/>
              <path d="M12 8V12M12 16H12.01" stroke="#EF4444" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <div class="banner-text">
            <div class="banner-title">
              {{ getStatusTitle(market.marketStatus) }}
            </div>
            <div class="banner-desc">
              {{ getStatusDesc(market.marketStatus) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 市场信息 -->
      <div v-if="market" class="market-content">
      <!-- 基本信息 -->
      <div class="market-header">
        <h1 class="market-title">{{ market.title }}</h1>
        <p class="market-description">{{ market.description }}</p>

        <!-- 标签 -->
        <div v-if="market.tags && market.tags.length > 0" class="market-tags">
          <span v-for="(tag, index) in market.tags" :key="index" class="tag">
            {{ typeof tag === 'object' ? tag.desc : tag }}
          </span>
        </div>
      </div>

      <!-- 市场数据卡片 -->
      <div class="market-stats-grid">
        <!-- Current Probability -->
        <div class="stat-card">
          <div class="stat-label">Current Probability</div>
          <div class="stat-value probability">
            {{ (market.currentProbability * 100).toFixed(1) }}%
          </div>
          <div class="stat-change positive">+2.3%</div>
        </div>

        <!-- Volume -->
        <div class="stat-card">
          <div class="stat-label">Volume</div>
          <div class="stat-value">{{ formatNumber(market.volume) }} IMKT</div>
          <div class="stat-change">Total Volume</div>
        </div>

        <!-- Liquidity -->
        <div class="stat-card liquidity">
          <div class="stat-label">Liquidity</div>
          <div class="stat-value">{{ formatNumber(market.liquidity) }} IMKT</div>
          <div class="stat-change">From {{ market.liquidityProviders || 1 }} providers</div>
        </div>

        <!-- Time Remaining -->
        <div class="stat-card">
          <div class="stat-label">Time Remaining</div>
          <div class="stat-value">{{ getTimeRemaining(market.endTime) }}</div>
          <div class="stat-change">{{ formatDate(market.endTime) }} Ends</div>
        </div>
      </div>

      <!-- Trading Charts and Orderbook -->
      <div class="trading-charts-section">
        <div class="charts-row">
          <!-- Price Chart -->
          <PriceChart :marketId="marketId" class="price-chart-full" />
        </div>

        <div class="orderbook-trades-row">
          <!-- Order Book -->
          <OrderBook :marketId="marketId" class="orderbook" />
          <!-- Recent Trades -->
          <RecentTrades :marketId="marketId" class="recent-trades" />
        </div>
      </div>
      </div>
    </div>

    <!-- 右侧：市场信息区域 -->
    <div v-if="market" class="right-sidebar">
      <!-- 结算信息 -->
      <div class="resolution-section">
        <h3 class="section-title">Resolution Method</h3>
        <div class="resolution-content">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M10 2L3 7V17H17V7L10 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            <path d="M10 11V14M10 6V10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <p>{{ market.resolutionSource }}</p>
        </div>
      </div>

      <!-- Trading操作 -->
      <div v-if="[4, 5].includes(market.marketStatus)" class="trading-section">
        <h3 class="section-title">Trading</h3>
        <div class="trading-panel">
          <div class="trading-options">
            <button
              :class="['trade-button', 'yes', { active: selectedOption === 'yes' }]"
              @click="selectOption('yes')"
            >
              <div class="option-label">YES</div>
              <div class="option-price">{{ (market.yesPrice * 100).toFixed(1) }}¢</div>
            </button>
            <button
              :class="['trade-button', 'no', { active: selectedOption === 'no' }]"
              @click="selectOption('no')"
            >
              <div class="option-label">NO</div>
              <div class="option-price">{{ (market.noPrice * 100).toFixed(1) }}¢</div>
            </button>
          </div>
          <div class="trading-input-group">
            <label>Investment Amount (IMKT)</label>
            <input
              v-model.number="tradeAmount"
              type="number"
              placeholder="Enter amount"
              class="trade-input"
              min="1"
            />
            <div class="trade-preview">
              <div class="preview-row">
                <span>Expected Return</span>
                <span class="preview-value">{{ calculateExpectedReturn() }} IMKT</span>
              </div>
              <div class="preview-row">
                <span>Average Price</span>
                <span class="preview-value">{{ getAveragePrice() }}¢</span>
              </div>
            </div>
          </div>
          <button class="trade-submit-btn" :disabled="!selectedOption || !tradeAmount">
            {{ selectedOption ? `${selectedOption.toUpperCase()} ${tradeAmount || 0} IMKT` : 'Select Direction' }}
          </button>
        </div>
      </div>

      <!-- Liquidity信息 -->
      <div class="liquidity-section">
        <h3 class="section-title">Liquidity信息</h3>
        <div class="liquidity-grid">
          <div class="liquidity-item">
            <div class="item-label">Initial Stake</div>
            <div class="item-value">{{ formatNumber(market.stakeAmount) }} IMKT</div>
          </div>
          <div class="liquidity-item">
            <div class="item-label">当前Liquidity</div>
            <div class="item-value">{{ formatNumber(market.liquidity) }} IMKT</div>
          </div>
          <div class="liquidity-item">
            <div class="item-label">Creator Reward</div>
            <div class="item-value pending">Pending Resolution</div>
          </div>
          <div class="liquidity-item">
            <div class="item-label">Liquidity占比</div>
            <div class="item-value">
              {{ ((market.stakeAmount / market.liquidity) * 100).toFixed(1) }}%
            </div>
          </div>
        </div>
        <div class="creator-info">
          <div class="creator-avatar">
            {{ (market.creator || 'Unknown')[0] }}
          </div>
          <div class="creator-details">
            <div class="creator-label">Market Creator / Admin</div>
            <div class="creator-name">{{ market.creator || 'Unknown' }}</div>
            <div class="creator-reward">
              市场Ends后将获得Trading手续费的 10% 作为Liquidity奖励
            </div>
          </div>
        </div>
      </div>

      <!-- Creator Information -->
      <div class="creator-section">
        <h3 class="section-title">Creator Information</h3>
        <div class="creator-card">
          <div class="creator-header">
            <div class="creator-icon">
              {{ (market.creator || 'Unknown')[0] }}
            </div>
            <div class="creator-info">
              <div class="creator-name">{{ market.creator || 'Unknown' }}</div>
              <div class="creator-time">Created {{ formatDate(market.createTime) }}</div>
            </div>
          </div>
          <div class="creator-responsibilities">
            <div class="responsibility-title">Creator Responsibilities：</div>
            <ul class="responsibility-list">
              <li>Ensure prediction event results are verifiable</li>
              <li>在市场Ends后及时结算</li>
              <li>Provide accurate data sources</li>
              <li>获得Liquidity奖励作为回报</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Loading...</p>
    </div>

    <!-- 未找到数据 -->
    <div v-else-if="!market" class="loading-state">
      <p>Market not found</p>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { mockMarkets } from '../data/markets';
import { useUserStore } from '../stores/user';
import { useMarkets } from '../composables/useMarkets';
import { getMarketStatusText, getMarketStatusClass } from '../constants/marketStatus';
import PriceChart from './PriceChart.vue';
import OrderBook from './OrderBook.vue';
import RecentTrades from './RecentTrades.vue';

export default {
  name: 'MarketDetail',
  components: {
    PriceChart,
    OrderBook,
    RecentTrades
  },
  props: {
    marketId: {
      type: String,
      required: true
    }
  },
  setup(props) {
    const router = useRouter();
    const userStore = useUserStore();
    const market = ref(null);
    const selectedOption = ref(null);
    const tradeAmount = ref(null);
    const isLoading = ref(true);

    // 使用 useMarkets 获取 findMarketById 方法
    const { findMarketById } = useMarkets();

    const goBack = () => {
      router.push('/markets');
    };

    const getStatusTitle = (marketStatus) => {
      if (marketStatus === 0) {
        return 'In Pre-review';
      } else if (marketStatus === 2) {
        return 'Preliminary Approved';
      } else if (marketStatus === 3) {
        return 'Final Approved';
      } else if (marketStatus === 4) {
        return 'Deploying';
      } else if (marketStatus === 5) {
        return 'Open for Trading';
      } else if (marketStatus === 1) {
        return 'Rejected';
      } else if (marketStatus === 6) {
        return 'Closed';
      } else if (marketStatus === 99) {
        return 'Settled';
      } else {
        return 'Unknown Status';
      }
    };

    const getStatusDesc = (marketStatus) => {
      if (marketStatus === 0) {
        return 'Market is in pre-review, estimated 1-2 business days';
      } else if (marketStatus === 2) {
        return 'Market has passed preliminary review, waiting for final review';
      } else if (marketStatus === 3) {
        return 'Market has passed final review, waiting for deployment';
      } else if (marketStatus === 4) {
        return 'Market is being deployed to blockchain';
      } else if (marketStatus === 5) {
        return '市场已通过审核并开放Trading';
      } else if (marketStatus === 1) {
        return 'Market did not pass review';
      } else if (marketStatus === 6) {
        return 'Market has been closed';
      } else if (marketStatus === 99) {
        return 'Market has been settled';
      } else {
        return '';
      }
    };

    const getStageName = (marketStatus) => {
      if (marketStatus === 0) return '预审';
      if (marketStatus === 2) return '初审通过';
      if (marketStatus === 3) return '终审通过';
      return '未知';
    };

    const getCategoryName = (category) => {
      const categories = {
        crypto: 'Crypto',
        technology: 'Technology',
        politics: 'Politics',
        sports: 'Sports',
        finance: 'Finance',
        entertainment: 'Entertainment',
        other: 'Other'
      };
      return categories[category] || category;
    };

    const formatNumber = (num) => {
      if (!num) return '0';
      return num.toLocaleString('en-US', { maximumFractionDigits: 0 });
    };

    const formatDate = (timestamp) => {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    };

    const getTimeRemaining = (endTime) => {
      if (!endTime) return '--';

      // 如果是字符串，转换为时间戳
      let endTimeMs = endTime;
      if (typeof endTime === 'string') {
        endTimeMs = new Date(endTime).getTime();
        if (isNaN(endTimeMs)) return '--';
      }

      const now = Date.now();
      const diff = endTimeMs - now;

      if (diff <= 0) return '已Ends';

      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));

      if (days > 0) {
        return `${days}day ${hours}hour`;
      } else {
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        return `${hours}hour ${minutes}minute`;
      }
    };

    const selectOption = async (option) => {
      // Check if wallet is connected
      if (!userStore.isConnected) {
        const shouldConnect = confirm(
          'You need to connect your wallet to trade. Would you like to connect your MetaMask wallet now?'
        );

        if (shouldConnect) {
          const success = await userStore.connectWallet();
          if (success) {
            selectedOption.value = option;
          } else {
            alert('Failed to connect wallet. Please make sure MetaMask is installed and unlocked.');
          }
        }
      } else {
        selectedOption.value = option;
      }
    };

    const calculateExpectedReturn = () => {
      if (!tradeAmount.value || !selectedOption.value) return '0';

      const price = selectedOption.value === 'yes' ? market.value.yesPrice : market.value.noPrice;
      const potentialReturn = tradeAmount.value / price;
      return (potentialReturn - tradeAmount.value).toFixed(2);
    };

    const getAveragePrice = () => {
      if (!selectedOption.value) return '0.0';
      const price = selectedOption.value === 'yes' ? market.value.yesPrice : market.value.noPrice;
      return (price * 100).toFixed(1);
    };

    const getBannerClass = (marketStatus) => {
      if ([4, 5].includes(marketStatus)) return 'approved';
      if ([0, 2, 3].includes(marketStatus)) return 'pending';
      if (marketStatus === 1) return 'rejected';
      if (marketStatus === 6) return 'closed';
      if (marketStatus === 99) return 'settled';
      return 'pending';
    };

    // 计算生命周期阶段
    const lifecycleStages = ref([]);

    const calculateLifecycleStages = () => {
      if (!market.value || !market.value.endTime) return [];

      const endTime = new Date(market.value.endTime).getTime();

      const stages = [
        {
          status: 5,
          label: '已发布',
          time: '起始状态',
          description: '市场已上链并开放交易'
        },
        {
          status: 6,
          label: '已关闭',
          time: formatDate(endTime),
          description: '市场交易结束，等待裁决'
        },
        {
          status: 7,
          label: '裁决中',
          time: formatDate(endTime + 6 * 60 * 60 * 1000),
          description: 'Creator提交裁决结果（6小时）'
        },
        {
          status: 8,
          label: '挑战中',
          time: formatDate(endTime + 12 * 60 * 60 * 1000),
          description: '挑战期，可对裁决提出异议（6小时）'
        },
        {
          status: 9,
          label: '已终裁',
          time: formatDate(endTime + 13 * 60 * 60 * 1000),
          description: '最终裁决完成（1小时）'
        },
        {
          status: 10,
          label: '结算中',
          time: formatDate(endTime + 23 * 60 * 60 * 1000),
          description: '系统进行资产结算'
        },
        {
          status: 99,
          label: '已结算',
          time: formatDate(endTime + 24 * 60 * 60 * 1000),
          description: '市场完成结算，资金已分发'
        }
      ];

      return stages;
    };

    const isStageCompleted = (stageStatus) => {
      if (!market.value) return false;

      // 状态顺序: 5 -> 6 -> 7 -> 8 -> 9 -> 10 -> 99
      const statusOrder = [5, 6, 7, 8, 9, 10, 99];
      const currentStatusIndex = statusOrder.indexOf(market.value.marketStatus);
      const stageStatusIndex = statusOrder.indexOf(stageStatus);

      return stageStatusIndex < currentStatusIndex;
    };

    // 监听市场数据变化，更新生命周期阶段
    const updateLifecycleStages = () => {
      lifecycleStages.value = calculateLifecycleStages();
    };

    // 在市场数据加载后更新生命周期阶段
    onMounted(() => {
      try {
        isLoading.value = true;

        // 先从已加载的 markets 中查找
        let found = findMarketById(props.marketId);

        // 如果没找到，从mock数据查找
        if (!found) {
          found = mockMarkets.find(m => m.id == props.marketId);
        }

        // 如果还没找到，从localStorage查找用户创建的市场
        if (!found) {
          const userCreated = JSON.parse(localStorage.getItem('userCreatedMarkets') || '[]');
          found = userCreated.find(m => m.id == props.marketId);
        }

        if (found) {
          market.value = found;
          updateLifecycleStages();
        }
      } finally {
        isLoading.value = false;
      }
    });

    return {
      market,
      selectedOption,
      tradeAmount,
      goBack,
      getStatusTitle,
      getStatusDesc,
      getStageName,
      getCategoryName,
      formatNumber,
      formatDate,
      getTimeRemaining,
      selectOption,
      calculateExpectedReturn,
      getAveragePrice,
      getBannerClass,
      getMarketStatusText,
      getMarketStatusClass,
      lifecycleStages,
      isStageCompleted
    };
  }
};
</script>

<style scoped>
.market-detail-wrapper {
  display: flex;
  gap: 20px;
  height: calc(108vh - 72px);
  overflow: hidden;
  padding: 76px 20px 0 20px;
}

/* 左侧：状态进度区域 */
.left-sidebar {
  width: 260px;
  flex-shrink: 0;
  overflow-y: auto;
  padding: 20px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
}

.sidebar-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--accent-light);
}

/* 垂直时间轴样式 */
.timeline-vertical {
  position: relative;
}

.timeline-item-vertical {
  position: relative;
  padding-left: 30px;
  padding-bottom: 24px;
}

.timeline-item-vertical:last-child {
  padding-bottom: 0;
}

.timeline-marker-vertical {
  position: absolute;
  left: 0;
  top: 0;
  width: 16px;
  height: 100%;
}

.timeline-dot-vertical {
  position: absolute;
  left: 0;
  top: 2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--border-color);
  border: 2px solid var(--card-bg);
  z-index: 2;
  transition: all 0.3s ease;
}

.timeline-line-vertical {
  position: absolute;
  left: 5px;
  top: 14px;
  width: 2px;
  height: calc(100% - 12px);
  background: var(--border-color);
  z-index: 1;
}

/* 激活状态 */
.timeline-item-vertical.active .timeline-dot-vertical {
  background: var(--accent-light);
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.2);
  transform: scale(1.2);
}

.timeline-item-vertical.active .timeline-line-vertical {
  background: linear-gradient(to bottom, var(--accent-light), var(--border-color));
}

.timeline-item-vertical.active .timeline-status-vertical {
  color: var(--accent-light);
  font-weight: 700;
}

/* 已完成状态 */
.timeline-item-vertical.completed .timeline-dot-vertical {
  background: #22C55E;
}

.timeline-item-vertical.completed .timeline-line-vertical {
  background: #22C55E;
}

.timeline-item-vertical.completed .timeline-status-vertical {
  color: #22C55E;
}

.timeline-content-vertical {
  padding-left: 4px;
}

.timeline-status-vertical {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.timeline-time-vertical {
  font-size: 11px;
  color: var(--text-secondary);
  margin-bottom: 2px;
  line-height: 1.3;
}

.timeline-desc-vertical {
  font-size: 10px;
  color: var(--text-secondary);
  line-height: 1.4;
  opacity: 0.8;
}

/* 中间：趋势盘口区域 */
.center-content {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  padding-right: 10px;
}

/* 右侧：市场信息区域 */
.right-sidebar {
  width: 320px;
  flex-shrink: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px 20px 0 20px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  font-size: 13px;
  word-wrap: break-word;
  word-break: break-word;
}

.market-detail-container {
  width: 100%;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  margin-bottom: 24px;
  transition: all 0.2s;
}

.back-btn:hover {
  background: var(--input-bg);
  color: var(--text-primary);
  border-color: var(--accent-light);
}

/* 状态横幅 */
.status-banner {
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 24px;
}

.status-banner.pending {
  background: rgba(245, 158, 11, 0.1);
  border: 1px solid #F59E0B;
}

.status-banner.approved {
  background: rgba(34, 197, 94, 0.1);
  border: 1px solid #22C55E;
}

.status-banner.rejected {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid #EF4444;
}

.banner-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.banner-icon {
  color: inherit;
}

.banner-text.pending .banner-title,
.banner-text.pending .banner-desc {
  color: #F59E0B;
}

.banner-text.approved .banner-title,
.banner-text.approved .banner-desc {
  color: #22C55E;
}

.banner-text.rejected .banner-title,
.banner-text.rejected .banner-desc {
  color: #EF4444;
}

.banner-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 4px;
}

.banner-desc {
  font-size: 13px;
  opacity: 0.9;
}

/* 市场内容 */
.market-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.market-header {
  text-align: center;
}

/* .market-category {
  display: inline-block;
  padding: 6px 16px;
  background: var(--accent-light);
  color: white;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 16px;
} */

.market-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 16px 0;
  color: var(--text-primary);
  line-height: 1.4;
}

.market-description {
  font-size: 16px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0 0 16px 0;
}

.market-tags {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.tag {
  padding: 4px 12px;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}

/* 统计卡片 */
.market-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 16px;
}

.stat-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  text-align: center;
}

.stat-card.liquidity {
  border-color: var(--accent-light);
  background: rgba(139, 92, 246, 0.05);
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.stat-value.probability {
  color: var(--accent-light);
}

.stat-change {
  font-size: 12px;
  color: var(--text-secondary);
}

.stat-change.positive {
  color: #22C55E;
}

/* 区块 */
.resolution-section,
.liquidity-section,
.creator-section,
.trading-section {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 24px;
}

.section-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 12px 0;
  word-wrap: break-word;
  word-break: break-word;
}

.resolution-content {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.resolution-content p {
  flex: 1;
  margin: 0;
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
  word-wrap: break-word;
  word-break: break-word;
}

.resolution-content svg {
  color: var(--accent-light);
  flex-shrink: 0;
  margin-top: 2px;
}

.liquidity-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.liquidity-item {
  padding: 10px;
  background: var(--input-bg);
  border-radius: 8px;
}

.item-label {
  font-size: 11px;
  color: var(--text-secondary);
  margin-bottom: 4px;
  word-wrap: break-word;
  word-break: break-word;
}

.item-value {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
  word-wrap: break-word;
  word-break: break-word;
}

.item-value.pending {
  color: #F59E0B;
}

.creator-info {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: rgba(139, 92, 246, 0.1);
  border-radius: 8px;
}

.creator-avatar {
  width: 40px;
  height: 40px;
  background: var(--accent-light);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
}

.creator-details {
  flex: 1;
  min-width: 0;
}

.creator-label {
  font-size: 11px;
  color: var(--text-secondary);
  margin-bottom: 2px;
  word-wrap: break-word;
  word-break: break-word;
}

.creator-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
  word-wrap: break-word;
  word-break: break-word;
}

.creator-reward {
  font-size: 11px;
  color: var(--accent-light);
  line-height: 1.4;
  word-wrap: break-word;
  word-break: break-word;
}

/* 创建者卡片 */
.creator-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.creator-header {
  display: flex;
  gap: 12px;
  align-items: center;
}

.creator-icon {
  width: 36px;
  height: 36px;
  background: var(--accent-light);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}

.creator-info {
  flex: 1;
  min-width: 0;
}

.creator-time {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 2px;
  word-wrap: break-word;
  word-break: break-word;
}

.creator-responsibilities {
  padding: 12px;
  background: var(--input-bg);
  border-radius: 8px;
}

.responsibility-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  word-wrap: break-word;
  word-break: break-word;
}

.responsibility-list {
  margin: 0;
  padding-left: 20px;
  font-size: 11px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.responsibility-list li {
  margin-bottom: 4px;
  word-wrap: break-word;
  word-break: break-word;
}

/* Trading面板 */
.trading-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.trading-options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.trade-button {
  padding: 16px;
  border: 2px solid var(--border-color);
  border-radius: 12px;
  background: var(--input-bg);
  cursor: pointer;
  transition: all 0.2s;
}

.trade-button.yes:hover,
.trade-button.yes.active {
  border-color: #22C55E;
  background: rgba(34, 197, 94, 0.1);
}

.trade-button.no:hover,
.trade-button.no.active {
  border-color: #EF4444;
  background: rgba(239, 68, 68, 0.1);
}

.option-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.option-price {
  font-size: 24px;
  font-weight: 700;
}

.trade-button.yes .option-price {
  color: #22C55E;
}

.trade-button.no .option-price {
  color: #EF4444;
}

.trading-input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.trading-input-group label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-primary);
  word-wrap: break-word;
  word-break: break-word;
}

.trade-input {
  width: 100%;
  padding: 10px 12px;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 14px;
  box-sizing: border-box;
}

.trade-preview {
  padding: 10px;
  background: var(--card-bg);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.preview-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}

.preview-row span:first-child {
  color: var(--text-secondary);
}

.preview-value {
  font-weight: 600;
  color: var(--text-primary);
}

.trade-submit-btn {
  width: 100%;
  padding: 12px;
  background: var(--accent-light);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  box-sizing: border-box;
}

.trade-submit-btn:hover:not(:disabled) {
  background: #7C3AED;
  transform: translateY(-1px);
}

.trade-submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 待审核Tip */
.pending-notice {
  padding: 32px;
  background: rgba(245, 158, 11, 0.1);
  border: 1px solid #F59E0B;
  border-radius: 12px;
  text-align: center;
}

.notice-icon {
  margin-bottom: 16px;
}

.notice-title {
  font-size: 20px;
  font-weight: 700;
  color: #F59E0B;
  margin-bottom: 8px;
}

.notice-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 16px;
}

.notice-tip {
  text-align: left;
  padding: 16px;
  background: rgba(245, 158, 11, 0.05);
  border-radius: 8px;
  margin-top: 16px;
}

.tip-title {
  font-size: 13px;
  font-weight: 600;
  color: #F59E0B;
  margin-bottom: 8px;
}

.tip-list {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.tip-list li {
  margin-bottom: 4px;
}

/* Trading Charts Section */
.trading-charts-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 32px;
}

.charts-row {
  width: 100%;
}

.orderbook-trades-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.orderbook,
.recent-trades {
  min-height: 500px;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 60px 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--border-color);
  border-top-color: var(--accent-light);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 响应式 */
@media (max-width: 1400px) {
  .left-sidebar {
    width: 220px;
  }

  .right-sidebar {
    width: 280px;
  }
}

@media (max-width: 1200px) {
  .market-detail-wrapper {
    flex-wrap: wrap;
    overflow-y: auto;
  }

  .left-sidebar {
    width: 100%;
    max-height: 200px;
  }

  .center-content {
    width: 100%;
    overflow-y: visible;
  }

  .right-sidebar {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .market-detail-wrapper {
    padding: 76px 10px 10px 10px;
  }

  .market-title {
    font-size: 24px;
  }

  .market-stats-grid {
    grid-template-columns: 1fr 1fr;
  }

  .trading-options {
    grid-template-columns: 1fr;
  }

  .liquidity-grid {
    grid-template-columns: 1fr;
  }

  .orderbook-trades-row {
    grid-template-columns: 1fr;
  }

  .orderbook,
  .recent-trades {
    min-height: 400px;
  }

  .left-sidebar {
    max-height: 180px;
  }

  .timeline-item-vertical {
    padding-bottom: 16px;
  }

  .timeline-status-vertical {
    font-size: 13px;
  }

  .timeline-time-vertical {
    font-size: 10px;
  }

  .timeline-desc-vertical {
    display: none;
  }
}
</style>
