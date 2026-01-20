<template>
  <div class="leaderboard-page">
    <div class="page-header">
      <p class="page-subtitle">Top performing traders on iMarket</p>
    </div>

    <div class="leaderboard-table">
      <table>
        <thead>
          <tr>
            <th>Rank</th>
            <th>Trader</th>
            <th>Profit</th>
            <th>Win Rate</th>
            <th>Trades</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(trader, index) in traders" :key="trader.address">
            <td class="rank">
              <span v-if="index < 3" class="medal">
                {{ index === 0 ? '🥇' : index === 1 ? '🥈' : '🥉' }}
              </span>
              <span v-else class="rank-number">#{{ index + 1 }}</span>
            </td>
            <td class="trader">
              <div class="trader-avatar">
                <img :src="trader.avatar" :alt="trader.name" />
              </div>
              <div class="trader-info">
                <div class="trader-name">{{ trader.name }}</div>
                <div class="trader-address">{{ shortenAddress(trader.address) }}</div>
              </div>
            </td>
            <td class="profit">+${{ trader.profit.toLocaleString() }}</td>
            <td class="win-rate">{{ trader.winRate }}%</td>
            <td class="trades">{{ trader.trades.toLocaleString() }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const traders = ref([
  {
    address: '0x1234...5678',
    name: 'CryptoWhale',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=whale',
    profit: 528000,
    winRate: 78,
    trades: 1243
  },
  {
    address: '0x9876...4321',
    name: 'DiamondHands',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=diamond',
    profit: 389000,
    winRate: 72,
    trades: 892
  },
  {
    address: '0x2468...1357',
    name: 'PredictionPro',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=pro',
    profit: 256000,
    winRate: 68,
    trades: 645
  },
  {
    address: '0x1357...2468',
    name: 'MarketMaster',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=master',
    profit: 198000,
    winRate: 65,
    trades: 523
  },
  {
    address: '0x8642...9753',
    name: 'AlphaSeeker',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=alpha',
    profit: 156000,
    winRate: 62,
    trades: 412
  }
])

const shortenAddress = (address) => {
  return address.slice(0, 6) + '...' + address.slice(-4)
}
</script>

<style scoped>
.leaderboard-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 3rem 2rem;
  width: 100%;
  box-sizing: border-box;
}

.page-header {
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  letter-spacing: -0.025em;
  margin-bottom: 0.75rem;
  color: var(--text-primary);
}

.page-subtitle {
  font-size: 1.125rem;
  color: var(--text-secondary);
  font-weight: 400;
}

.leaderboard-table {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

th {
  padding: 1rem 1.5rem;
  text-align: left;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-tertiary);
}

tbody tr {
  border-bottom: 1px solid var(--border-color);
  transition: background 0.15s ease;
}

tbody tr:last-child {
  border-bottom: none;
}

tbody tr:hover {
  background: var(--bg-secondary);
}

td {
  padding: 1.25rem 1.5rem;
}

.rank {
  font-size: 1.5rem;
  font-weight: 700;
}

.medal {
  font-size: 2rem;
}

.rank-number {
  color: var(--text-tertiary);
}

.trader {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.trader-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--bg-tertiary);
  flex-shrink: 0;
}

.trader-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.trader-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 0.875rem;
}

.trader-address {
  font-size: 0.75rem;
  color: var(--text-tertiary);
  margin-top: 0.125rem;
}

.profit {
  font-weight: 600;
  font-size: 0.875rem;
  color: var(--success);
}

.win-rate {
  font-weight: 600;
  font-size: 0.875rem;
  color: var(--text-primary);
}

.trades {
  font-weight: 500;
  font-size: 0.875rem;
  color: var(--text-secondary);
}
</style>
