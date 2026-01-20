<template>
  <div class="market-transactions-container">
    <div class="transactions-header">
      <h2 class="transactions-title">Market Transactions</h2>
      <p class="transactions-subtitle">Track all trading activities for this market</p>
    </div>

    <!-- 统计摘要 -->
    <div class="stats-summary">
      <div class="summary-card">
        <div class="summary-label">Total Transactions</div>
        <div class="summary-value">{{ totalTransactions }}</div>
      </div>
      <div class="summary-card">
        <div class="summary-label">Total Volume</div>
        <div class="summary-value">{{ formatNumber(totalVolume) }} IMKT</div>
      </div>
      <div class="summary-card">
        <div class="summary-label">Unique Traders</div>
        <div class="summary-value">{{ uniqueTraders }}</div>
      </div>
      <div class="summary-card">
        <div class="summary-label">Current Price</div>
        <div class="summary-value price">{{ (currentPrice * 100).toFixed(1) }}¢</div>
      </div>
    </div>

    <!-- 筛选器 -->
    <div class="filters-bar">
      <div class="filter-group">
        <label>Filter by type:</label>
        <select v-model="filterType" class="filter-select">
          <option value="all">All Transactions</option>
          <option value="buy">Buy YES</option>
          <option value="sell">Sell YES</option>
        </select>
      </div>
      <div class="filter-group">
        <label>Sort by:</label>
        <select v-model="sortBy" class="filter-select">
          <option value="time">Time</option>
          <option value="amount">Amount</option>
          <option value="price">Price</option>
        </select>
      </div>
    </div>

    <!-- 交易列表 -->
    <div v-if="filteredTransactions.length > 0" class="transactions-list">
      <div
        v-for="transaction in filteredTransactions"
        :key="transaction.id"
        class="transaction-item"
      >
        <div class="transaction-icon">
          <svg v-if="transaction.type === 'buy'" width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M12 5V19M5 12H19" stroke="#22C55E" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M5 12H19" stroke="#EF4444" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>

        <div class="transaction-info">
          <div class="transaction-type">
            {{ transaction.type === 'buy' ? 'Buy YES' : 'Sell YES' }}
          </div>
          <div class="transaction-trader">
            Trader: {{ transaction.trader }}
          </div>
          <div class="transaction-time">
            {{ formatTime(transaction.timestamp) }}
          </div>
        </div>

        <div class="transaction-details">
          <div class="transaction-amount">
            <span class="amount-value">{{ formatNumber(transaction.amount) }}</span>
            <span class="amount-currency">IMKT</span>
          </div>
          <div class="transaction-price">
            @ {{ (transaction.price * 100).toFixed(1) }}¢
          </div>
        </div>

        <div class="transaction-total">
          <div class="total-label">Total</div>
          <div class="total-value">{{ formatNumber(transaction.total) }} IMKT</div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
        <rect x="8" y="8" width="48" height="48" rx="8" stroke="var(--border-color)" stroke-width="2"/>
        <path d="M24 32H40M32 24V40" stroke="var(--border-color)" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <div class="empty-title">No transactions yet</div>
      <div class="empty-desc">This market has no trading activity</div>
    </div>

    <!-- 分页 -->
    <div v-if="filteredTransactions.length > 0" class="pagination">
      <button
        class="pagination-btn"
        :disabled="currentPage === 1"
        @click="currentPage--"
      >
        Previous
      </button>
      <span class="pagination-info">
        Page {{ currentPage }} of {{ totalPages }}
      </span>
      <button
        class="pagination-btn"
        :disabled="currentPage === totalPages"
        @click="currentPage++"
      >
        Next
      </button>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';

export default {
  name: 'MarketTransactions',
  props: {
    marketId: {
      type: String,
      required: true
    }
  },
  setup(props) {
    const filterType = ref('all');
    const sortBy = ref('time');
    const currentPage = ref(1);
    const pageSize = 10;

    // 模拟交易数据
    const transactions = ref([]);

    onMounted(() => {
      loadTransactions();
    });

    const loadTransactions = () => {
      // 从localStorage加载或生成模拟数据
      const saved = localStorage.getItem(`transactions-${props.marketId}`);
      if (saved) {
        transactions.value = JSON.parse(saved);
      } else {
        // 生成模拟交易数据
        transactions.value = generateMockTransactions();
        localStorage.setItem(`transactions-${props.marketId}`, JSON.stringify(transactions.value));
      }
    };

    const generateMockTransactions = () => {
      const types = ['buy', 'sell'];
      const traders = ['0x1234...5678', '0xabcd...efgh', '0x9876...5432', '0xfedc...ba98', '0x1111...2222'];
      const mockData = [];

      for (let i = 0; i < 25; i++) {
        const type = types[Math.floor(Math.random() * types.length)];
        const price = 0.3 + Math.random() * 0.4; // 0.3-0.7
        const amount = Math.floor(100 + Math.random() * 4900);

        mockData.push({
          id: `tx-${Date.now()}-${i}`,
          type,
          trader: traders[Math.floor(Math.random() * traders.length)],
          amount,
          price,
          total: amount * price,
          timestamp: Date.now() - (i * 3600000) // 每小时一笔
        });
      }

      return mockData;
    };

    const totalTransactions = computed(() => transactions.value.length);

    const totalVolume = computed(() => {
      return transactions.value.reduce((sum, tx) => sum + tx.total, 0);
    });

    const uniqueTraders = computed(() => {
      const traders = new Set(transactions.value.map(tx => tx.trader));
      return traders.size;
    });

    const currentPrice = computed(() => {
      if (transactions.value.length === 0) return 0.5;
      return transactions.value[0].price;
    });

    const filteredAndSortedTransactions = computed(() => {
      let result = [...transactions.value];

      // 类型筛选
      if (filterType.value !== 'all') {
        result = result.filter(tx => tx.type === filterType.value);
      }

      // 排序
      result.sort((a, b) => {
        if (sortBy.value === 'time') {
          return b.timestamp - a.timestamp;
        } else if (sortBy.value === 'amount') {
          return b.amount - a.amount;
        } else if (sortBy.value === 'price') {
          return b.price - a.price;
        }
        return 0;
      });

      return result;
    });

    const filteredTransactions = computed(() => {
      const start = (currentPage.value - 1) * pageSize;
      const end = start + pageSize;
      return filteredAndSortedTransactions.value.slice(start, end);
    });

    const totalPages = computed(() => {
      return Math.ceil(filteredAndSortedTransactions.value.length / pageSize);
    });

    const formatNumber = (num) => {
      if (!num) return '0';
      return num.toLocaleString('en-US', { maximumFractionDigits: 2 });
    };

    const formatTime = (timestamp) => {
      const date = new Date(timestamp);
      const now = new Date();
      const diff = now - date;

      if (diff < 60000) {
        return 'Just now';
      } else if (diff < 3600000) {
        const minutes = Math.floor(diff / 60000);
        return `${minutes}m ago`;
      } else if (diff < 86400000) {
        const hours = Math.floor(diff / 3600000);
        return `${hours}h ago`;
      } else {
        return date.toLocaleDateString('en-US', {
          month: 'short',
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        });
      }
    };

    return {
      filterType,
      sortBy,
      currentPage,
      filteredTransactions,
      totalTransactions,
      totalVolume,
      uniqueTraders,
      currentPrice,
      totalPages,
      formatNumber,
      formatTime
    };
  }
};
</script>

<style scoped>
.market-transactions-container {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 24px;
}

.transactions-header {
  margin-bottom: 24px;
}

.transactions-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.transactions-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.stats-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.summary-card {
  background: var(--input-bg);
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}

.summary-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.summary-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.summary-value.price {
  color: var(--accent-light);
}

.filters-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.filter-select {
  padding: 8px 12px;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  color: var(--text-primary);
  font-size: 13px;
  cursor: pointer;
}

.transactions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.transaction-item {
  display: grid;
  grid-template-columns: auto 2fr 1fr 1fr;
  gap: 16px;
  align-items: center;
  padding: 16px;
  background: var(--input-bg);
  border-radius: 8px;
  transition: background 0.2s;
}

.transaction-item:hover {
  background: var(--bg-secondary);
}

.transaction-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: white;
  border-radius: 8px;
  flex-shrink: 0;
}

.transaction-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.transaction-type {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.transaction-trader {
  font-size: 12px;
  color: var(--text-secondary);
  font-family: 'Monaco', 'Courier New', monospace;
}

.transaction-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.transaction-details {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.transaction-amount {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.amount-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.amount-currency {
  font-size: 12px;
  color: var(--text-secondary);
}

.transaction-price {
  font-size: 12px;
  color: var(--text-secondary);
}

.transaction-total {
  text-align: right;
}

.total-label {
  font-size: 11px;
  color: var(--text-secondary);
  margin-bottom: 2px;
}

.total-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--accent-light);
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
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
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color);
}

.pagination-btn {
  padding: 8px 16px;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.2s;
}

.pagination-btn:hover:not(:disabled) {
  background: var(--accent-light);
  border-color: var(--accent-light);
  color: white;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-info {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-summary {
    grid-template-columns: 1fr 1fr;
  }

  .transaction-item {
    grid-template-columns: auto 1fr;
    gap: 12px;
  }

  .transaction-details,
  .transaction-total {
    grid-column: 2;
  }

  .filters-bar {
    flex-direction: column;
  }

  .filter-group {
    width: 100%;
  }

  .filter-select {
    flex: 1;
  }
}
</style>
