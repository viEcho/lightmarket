<template>
  <div class="review-market-container">
    <div class="review-header">
      <h1 class="page-title">Market Review Management</h1>
      <p class="page-subtitle">Review user-submitted prediction markets, ensure quality and resolvability</p>
    </div>

    <!-- 筛选标签 -->
    <div class="filter-tabs">
      <button
        :class="['tab-button', { active: activeTab === 'pre-review' }]"
        @click="activeTab = 'pre-review'"
      >
        <span class="tab-label">Pre-review</span>
        <span class="tab-count">{{ getMarketsByStage('pre-review').length }}</span>
      </button>
      <button
        :class="['tab-button', { active: activeTab === 'final-review' }]"
        @click="activeTab = 'final-review'"
      >
        <span class="tab-label">Final-review</span>
        <span class="tab-count">{{ getMarketsByStage('final-review').length }}</span>
      </button>
      <button
        :class="['tab-button', { active: activeTab === 'approved' }]"
        @click="activeTab = 'approved'"
      >
        <span class="tab-label">Approved</span>
        <span class="tab-count">{{ getMarketsByStatus('approved').length }}</span>
      </button>
      <button
        :class="['tab-button', { active: activeTab === 'rejected' }]"
        @click="activeTab = 'rejected'"
      >
        <span class="tab-label">Rejected</span>
        <span class="tab-count">{{ getMarketsByStatus('rejected').length }}</span>
      </button>
    </div>

    <!-- Markets列表 -->
    <div v-if="filteredMarkets.length > 0" class="markets-list">
      <div
        v-for="market in filteredMarkets"
        :key="market.id"
        class="market-review-card"
      >
        <!-- Markets基本信息 -->
        <div class="market-info">
          <div class="market-category">{{ getCategoryName(market.category) }}</div>
          <h3 class="market-title">{{ market.title }}</h3>
          <p class="market-description">{{ market.description }}</p>

          <div class="applicant-info">
            <div class="applicant-card">
              <div class="applicant-icon">
                {{ (market.creator || 'Unknown')[0] }}
              </div>
              <div class="applicant-details">
                <div class="applicant-label">Applicant</div>
                <div class="applicant-name">{{ market.creator || 'Unknown' }}</div>
              </div>
            </div>
            <div class="stake-info">
              <div class="stake-label">Stake Amount</div>
              <div class="stake-value">{{ formatNumber(market.stakeAmount) }} IMKT</div>
              <div class="stake-tier">{{ getStakeTier(market.stakeAmount) }}</div>
            </div>
          </div>

          <!-- Markets元数据 -->
          <div class="market-metadata">
            <div class="metadata-item">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="2"/>
                <path d="M8 5V8L10 10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <span>{{ formatDate(market.createTime) }}</span>
            </div>
            <div class="metadata-item">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <rect x="3" y="3" width="10" height="10" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M5 8H11M8 5V11" stroke="currentColor" stroke-width="2"/>
              </svg>
              <span>{{ market.stakeAmount }} IMKT</span>
            </div>
            <div class="metadata-item">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 2L14 6V14H10V10H6V14H2V6L8 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              </svg>
              <span>{{ market.creator || 'Unknown' }}</span>
            </div>
          </div>

          <!-- 标签 -->
          <div v-if="market.tags && market.tags.length > 0" class="market-tags">
            <span v-for="(tag, index) in market.tags" :key="index" class="tag">
              {{ tag }}
            </span>
          </div>
        </div>

        <!-- 审核信息 -->
        <div class="review-info">
          <!-- Resolution Method -->
          <div class="resolution-section">
            <div class="section-label">Resolution Method</div>
            <div class="resolution-text">{{ market.resolutionSource }}</div>
          </div>

          <!-- End Time -->
          <div class="time-section">
            <div class="section-label">End Time</div>
            <div class="time-text">{{ formatDate(market.endTime) }}</div>
            <div class="time-remaining">{{ getTimeRemaining(market.endTime) }}</div>
          </div>

          <!-- 重复检查 -->
          <div v-if="market.duplicateCheck" class="duplicate-check">
            <div :class="['check-status', market.duplicateCheck.hasDuplicate ? 'warning' : 'success']">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path v-if="!market.duplicateCheck.hasDuplicate" d="M8 2L3 7L6.5 10.5L13 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle v-else cx="8" cy="8" r="6" stroke="currentColor" stroke-width="2"/>
                <path v-if="market.duplicateCheck.hasDuplicate" d="M8 5V8M8 11H8.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <span>{{ market.duplicateCheck.hasDuplicate ? 'Similar Topics Exist' : 'No Duplicate Topics' }}</span>
            </div>
            <div v-if="market.duplicateCheck.hasDuplicate" class="duplicate-markets">
              Similar Topics：
              <a
                v-for="dup in market.duplicateCheck.duplicates"
                :key="dup.id"
                href="#"
                @click.prevent="viewMarket(dup.id)"
                class="duplicate-link"
              >
                {{ dup.title }}
              </a>
            </div>
          </div>
        </div>

        <!-- 审核操作 -->
        <div class="review-actions">
          <!-- Pre-review操作 -->
          <div v-if="market.stage === 'pre-review'" class="action-section">
            <div class="action-title">Pre-review决策</div>
            <div class="action-buttons">
              <button
                @click="rejectMarket(market, 'Improper Format')"
                class="action-btn reject"
              >
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="2"/>
                  <path d="M8 5V11M8 5H5M8 5H11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                Reject
              </button>
              <button
                @click="promoteToFinalReview(market)"
                class="action-btn promote"
              >
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M8 2L12 6H9V11H7V6H4L8 2Z" fill="currentColor"/>
                </svg>
                进入Final-review
              </button>
            </div>
            <div class="quick-reject">
              <select
                v-model="market.quickRejectReason"
                class="reject-reason-select"
              >
                <option value="">快速Reject原因...</option>
                <option value="Improper Format">Improper Format</option>
                <option value="Unclear Description">Unclear Description</option>
                <option value="缺少Resolution Method">缺少Resolution Method</option>
                <option value="End Time不合理">End Time不合理</option>
                <option value="Inappropriate Topic">Inappropriate Topic</option>
              </select>
            </div>
          </div>

          <!-- Final-review操作 -->
          <div v-else-if="market.stage === 'final-review'" class="action-section">
            <div class="action-title">Final-review决策</div>

            <!-- 质押排名 -->
            <div v-if="market.competingMarkets && market.competingMarkets.length > 0" class="competing-section">
              <div class="competing-title">Competing Topics (by Stake)</div>
              <div class="competing-list">
                <div
                  v-for="(competing, index) in market.competingMarkets"
                  :key="competing.id"
                  :class="['competing-item', { highlighted: competing.id === market.id }]"
                >
                  <span class="rank">{{ index + 1 }}</span>
                  <span class="title">{{ competing.title }}</span>
                  <span class="stake">{{ competing.stakeAmount }} IMKT</span>
                </div>
              </div>
            </div>

            <!-- 审核标准 -->
            <div class="review-criteria">
              <div class="criteria-title">Review Criteria Check：</div>
              <div class="criteria-list">
                <label class="criteria-item">
                  <input type="checkbox" v-model="market.criteria.topic" />
                  <span>Topic Credible</span>
                </label>
                <label class="criteria-item">
                  <input type="checkbox" v-model="market.criteria.resolvable" />
                  <span>Results Resolvable</span>
                </label>
                <label class="criteria-item">
                  <input type="checkbox" v-model="market.criteria.source" />
                  <span>Data Source Reliable</span>
                </label>
                <label class="criteria-item">
                  <input type="checkbox" v-model="market.criteria.timeline" />
                  <span>Timeline Reasonable</span>
                </label>
              </div>
            </div>

            <div class="action-buttons">
              <button
                @click="rejectMarket(market, '未通过Final-review')"
                class="action-btn reject"
              >
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="2"/>
                  <path d="M8 5V11M8 5H5M8 5H11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                Reject
              </button>
              <button
                @click="approveMarket(market)"
                class="action-btn approve"
                :disabled="!allCriteriaChecked(market)"
              >
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M13.5 4.5L6 12L2.5 8.5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Approve
              </button>
            </div>
          </div>

          <!-- 已审核状态 -->
          <div v-else class="action-section reviewed">
            <div :class="['reviewed-badge', market.status]">
              <svg v-if="market.status === 'approved'" width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M13.5 4.5L6 12L2.5 8.5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <svg v-else width="16" height="16" viewBox="0 0 16 16" fill="none">
                <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="2"/>
                <path d="M8 5V11M8 5H5M8 5H11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              {{ market.status === 'approved' ? 'Approved' : 'Rejected' }}
            </div>
            <div v-if="market.reviewNote" class="review-note">
              Note：{{ market.reviewNote }}
            </div>
            <div class="review-time">
              Review Time：{{ formatDate(market.reviewTime) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
        <circle cx="32" cy="32" r="28" stroke="var(--border-color)" stroke-width="2"/>
        <path d="M32 20V32L40 40" stroke="var(--border-color)" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <div class="empty-title">No{{ getTabLabel(activeTab) }}Markets</div>
      <div class="empty-desc">当前没有需要审核的Markets</div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { mockMarkets } from '../data/markets';

export default {
  name: 'ReviewMarket',
  setup() {
    const router = useRouter();
    const activeTab = ref('pre-review');
    const reviewMarkets = ref([]);

    onMounted(() => {
      // 加载待审核的Markets
      loadReviewMarkets();
    });

    const loadReviewMarkets = () => {
      // 从localStorage加载用户创建的Markets
      const userCreated = JSON.parse(localStorage.getItem('userCreatedMarkets') || '[]');

      // 添加一些模拟的审核数据
      reviewMarkets.value = userCreated.map(market => ({
        ...market,
        duplicateCheck: checkDuplicates(market),
        competingMarkets: getCompetingMarkets(market, userCreated),
        criteria: {
          topic: false,
          resolvable: false,
          source: false,
          timeline: false
        }
      }));
    };

    const checkDuplicates = (market) => {
      // 检查与已发布Markets的重复
      const approvedDuplicates = mockMarkets.filter(m =>
        m.status === 'approved' &&
        m.title.toLowerCase().includes(market.title.toLowerCase().split(' ')[0])
      );

      // 检查与待审核Markets的重复
      const pendingDuplicates = reviewMarkets.value.filter(m =>
        m.id !== market.id &&
        m.title.toLowerCase().includes(market.title.toLowerCase().split(' ')[0])
      );

      const hasDuplicate = approvedDuplicates.length > 0 || pendingDuplicates.length > 0;

      return {
        hasDuplicate,
        duplicates: [...approvedDuplicates, ...pendingDuplicates]
      };
    };

    const getCompetingMarkets = (market, allMarkets) => {
      // 获取同类话题的所有申请，按质押金额排序
      const competing = allMarkets
        .filter(m =>
          m.id !== market.id &&
          m.stage === 'pre-review' &&
          m.category === market.category
        )
        .sort((a, b) => b.stakeAmount - a.stakeAmount)
        .slice(0, 3);

      return [market, ...competing].sort((a, b) => b.stakeAmount - a.stakeAmount);
    };

    const filteredMarkets = computed(() => {
      if (activeTab.value === 'pre-review') {
        return getMarketsByStage('pre-review');
      } else if (activeTab.value === 'final-review') {
        return getMarketsByStage('final-review');
      } else if (activeTab.value === 'approved') {
        return getMarketsByStatus('approved');
      } else {
        return getMarketsByStatus('rejected');
      }
    });

    const getMarketsByStage = (stage) => {
      return reviewMarkets.value.filter(m => m.stage === stage);
    };

    const getMarketsByStatus = (status) => {
      return reviewMarkets.value.filter(m => m.status === status);
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
      const now = Date.now();
      const diff = endTime - now;

      if (diff <= 0) return '已结束';

      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      if (days > 0) return `还剩 ${days} 天`;
      return '即将结束';
    };

    const getTabLabel = (tab) => {
      const labels = {
        'pre-review': 'Pre-review',
        'final-review': 'Final-review',
        'approved': 'Approved',
        'rejected': 'Rejected'
      };
      return labels[tab] || tab;
    };

    const allCriteriaChecked = (market) => {
      return market.criteria &&
        market.criteria.topic &&
        market.criteria.resolvable &&
        market.criteria.source &&
        market.criteria.timeline;
    };

    // 审核操作
    const promoteToFinalReview = (market) => {
      if (confirm(`确定将"${market.title}"进入Final-review环节吗？`)) {
        market.stage = 'final-review';
        saveMarketChanges();
        alert('已进入Final-review环节！');
      }
    };

    const approveMarket = (market) => {
      if (!allCriteriaChecked(market)) {
        alert('请完成所有Review Criteria Check！');
        return;
      }

      if (confirm(`Confirm to approve？Markets将立即发布！`)) {
        market.status = 'approved';
        market.stage = 'published';
        market.reviewTime = Date.now();
        market.reviewNote = '通过Final-review';
        saveMarketChanges();
        alert('Markets已发布！');
      }
    };

    const rejectMarket = (market, reason) => {
      const finalReason = market.quickRejectReason || reason;

      if (!finalReason) {
        alert('请选择或输入Reject原因！');
        return;
      }

      if (confirm(`确定Reject"${market.title}"吗？\n原因：${finalReason}`)) {
        market.status = 'rejected';
        market.reviewTime = Date.now();
        market.reviewNote = finalReason;
        saveMarketChanges();
        alert('MarketsRejected！');
      }
    };

    const saveMarketChanges = () => {
      // 保存到localStorage
      const userCreated = JSON.parse(localStorage.getItem('userCreatedMarkets') || '[]');

      // 更新对应的Markets
      reviewMarkets.value.forEach(market => {
        const index = userCreated.findIndex(m => m.id === market.id);
        if (index !== -1) {
          userCreated[index] = market;
        }
      });

      localStorage.setItem('userCreatedMarkets', JSON.stringify(userCreated));
      loadReviewMarkets();
    };

    const viewMarket = (marketId) => {
      router.push(`/market/${marketId}`);
    };

    const formatNumber = (num) => {
      if (!num) return '0';
      return num.toLocaleString('en-US', { maximumFractionDigits: 0 });
    };

    const getStakeTier = (amount) => {
      if (amount >= 5000) return 'Fast Track';
      if (amount >= 1000) return 'Priority';
      return 'Basic';
    };

    return {
      activeTab,
      filteredMarkets,
      getMarketsByStage,
      getMarketsByStatus,
      getCategoryName,
      formatDate,
      getTimeRemaining,
      getTabLabel,
      allCriteriaChecked,
      promoteToFinalReview,
      approveMarket,
      rejectMarket,
      viewMarket,
      formatNumber,
      getStakeTier
    };
  }
};
</script>

<style scoped>
.review-market-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 20px;
}

.review-header {
  text-align: center;
  margin-bottom: 32px;
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
  margin: 0;
}

/* 筛选标签 */
.filter-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  background: var(--card-bg);
  padding: 8px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
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
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
}

.tab-button:hover {
  background: var(--input-bg);
}

.tab-button.active {
  background: var(--accent-light);
  color: white;
}

.tab-count {
  padding: 2px 8px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  font-size: 12px;
}

.tab-button:not(.active) .tab-count {
  background: var(--input-bg);
}

/* Markets列表 */
.markets-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.market-review-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 24px;
  display: grid;
  grid-template-columns: 2fr 1.5fr 1fr;
  gap: 24px;
}

/* Markets信息 */
.market-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.market-category {
  display: inline-block;
  padding: 4px 12px;
  background: var(--accent-light);
  color: white;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  width: fit-content;
}

.market-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.4;
}

.market-description {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 申请人信息和质押 */
.applicant-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 16px;
  background: rgba(99, 102, 241, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(99, 102, 241, 0.2);
}

.applicant-card {
  display: flex;
  align-items: center;
  gap: 12px;
}

.applicant-icon {
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

.applicant-details {
  flex: 1;
}

.applicant-label {
  font-size: 11px;
  color: var(--text-secondary);
  margin-bottom: 2px;
}

.applicant-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  font-family: 'Monaco', 'Courier New', monospace;
}

.stake-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  text-align: right;
}

.stake-label {
  font-size: 11px;
  color: var(--text-secondary);
}

.stake-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--accent-light);
}

.stake-tier {
  font-size: 11px;
  color: var(--text-secondary);
  font-weight: 600;
  text-transform: uppercase;
}

.market-metadata {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.metadata-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}

.market-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag {
  padding: 4px 10px;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}

/* 审核信息 */
.review-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.resolution-section,
.time-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.section-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
}

.resolution-text,
.time-text {
  font-size: 13px;
  color: var(--text-primary);
  line-height: 1.5;
}

.time-remaining {
  font-size: 12px;
  color: var(--accent-light);
  font-weight: 600;
}

/* 重复检查 */
.duplicate-check {
  padding: 12px;
  border-radius: 8px;
  background: var(--input-bg);
}

.check-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px;
}

.check-status.success {
  color: #22C55E;
}

.check-status.warning {
  color: #F59E0B;
}

.duplicate-markets {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.duplicate-link {
  color: var(--accent-light);
  text-decoration: none;
  margin-left: 4px;
}

.duplicate-link:hover {
  text-decoration: underline;
}

/* 审核操作 */
.review-actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.action-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.action-btn.reject {
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
  border: 1px solid #EF4444;
}

.action-btn.reject:hover {
  background: #EF4444;
  color: white;
}

.action-btn.promote {
  background: rgba(245, 158, 11, 0.1);
  color: #F59E0B;
  border: 1px solid #F59E0B;
}

.action-btn.promote:hover {
  background: #F59E0B;
  color: white;
}

.action-btn.approve {
  background: rgba(34, 197, 94, 0.1);
  color: #22C55E;
  border: 1px solid #22C55E;
}

.action-btn.approve:hover:not(:disabled) {
  background: #22C55E;
  color: white;
}

.action-btn.approve:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quick-reject {
  margin-top: 4px;
}

.reject-reason-select {
  width: 100%;
  padding: 8px 12px;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  color: var(--text-primary);
  font-size: 13px;
}

/* 竞争者 */
.competing-section {
  padding: 12px;
  background: rgba(245, 158, 11, 0.1);
  border-radius: 8px;
}

.competing-title {
  font-size: 12px;
  font-weight: 600;
  color: #F59E0B;
  margin-bottom: 8px;
}

.competing-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.competing-item {
  display: grid;
  grid-template-columns: 24px 1fr auto;
  gap: 8px;
  align-items: center;
  padding: 8px;
  background: var(--card-bg);
  border-radius: 6px;
  font-size: 12px;
}

.competing-item.highlighted {
  background: rgba(245, 158, 11, 0.2);
  border: 1px solid #F59E0B;
}

.competing-item .rank {
  font-weight: 700;
  color: var(--accent-light);
  width: 24px;
  text-align: center;
}

.competing-item .title {
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.competing-item .stake {
  font-weight: 600;
  color: var(--accent-light);
}

/* 审核标准 */
.review-criteria {
  padding: 12px;
  background: var(--input-bg);
  border-radius: 8px;
}

.criteria-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.criteria-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.criteria-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
}

.criteria-item input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

/* 已审核状态 */
.reviewed {
  align-items: center;
  text-align: center;
}

.reviewed-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
}

.reviewed-badge.approved {
  background: rgba(34, 197, 94, 0.1);
  color: #22C55E;
}

.reviewed-badge.rejected {
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
}

.review-note {
  margin-top: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}

.review-time {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

/* 空状态 */
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

/* 响应式 */
@media (max-width: 1024px) {
  .market-review-card {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: row;
  }
}
</style>
