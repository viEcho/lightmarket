package com.market.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.market.business.entity.Market;
import com.market.business.global.PageVO;
import com.market.business.mapper.MarketMapper;
import com.market.business.query.AdminApproveQuery;
import com.market.business.query.MarketApproveQuery;
import com.market.business.service.AdminService;
import com.market.business.vo.AdminStatisticsVO;
import com.market.business.vo.MarketVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin Service 实现类
 *
 * @author echo
 * @date 2026/01/22
 */
@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private MarketMapper marketMapper;

    @Override
    public PageVO<MarketVO> getApproveList(AdminApproveQuery query) {
        // 1. 查询指定状态的市场列表
        QueryWrapper<Market> queryWrapper = new QueryWrapper<>();

        if (query.getMarketStatus() != null) {
            queryWrapper.eq(Market.MARKET_STATUS, query.getMarketStatus());
        }

        // 如果设置了excludeRejected，排除已拒绝状态（状态=1）
        if (query.getExcludeRejected() != null && query.getExcludeRejected()) {
            queryWrapper.ne(Market.MARKET_STATUS, 1);
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(Market.CREATED_TIME);

        List<Market> markets = marketMapper.selectList(queryWrapper);

        // 2. 转换为 VO 并分页
        int total = markets.size();
        int start = (query.getNum() - 1) * query.getSize();
        int end = Math.min(start + query.getSize(), total);

        List<MarketVO> list = markets.subList(start, end).stream()
                .map(MarketVO::fromEntity)
                .collect(Collectors.toList());

        // 3. 查询各状态的统计数量
        Map<String, Integer> statusCounts = getStatusCounts();

        // 4. 构建 PageVO
        PageVO<MarketVO> result = new PageVO<>(query.getNum(), query.getSize(), total);
        result.setList(list);
        result.setExt(statusCounts);

        return result;
    }

    @Override
    public Boolean approveMarket(MarketApproveQuery query) {
        // 1. 查询市场
        Market market = marketMapper.selectOne(
            new QueryWrapper<Market>().eq(Market.MARKET_ID, query.getMarketId())
        );

        if (market == null) {
            throw new IllegalArgumentException("市场不存在");
        }

        Byte currentStatus = market.getMarketStatus();
        Integer targetStatus = query.getStatus();

        // 2. 校验状态流转规则
        validateStatusTransition(currentStatus, targetStatus);

        // 3. 更新状态
        market.setMarketStatus(targetStatus.byteValue());
        market.setUpdatedTime(new Date());

        int rows = marketMapper.updateById(market);

        log.info("Market approved: marketId={}, fromStatus={}, toStatus={}",
            query.getMarketId(), currentStatus, targetStatus);

        return rows > 0;
    }

    /**
     * 校验状态流转规则
     * 状态定义：0-待审核，1-已拒绝，2-初审通过，3-终审通过
     * 规则：
     * - 待审核(0) → 拒绝(1) 或 初审通过(2)
     * - 初审通过(2) → 拒绝(1) 或 终审通过(3)
     * - 状态不能大于3
     * - 已拒绝和已终审通过的状态不能再变更
     */
    private void validateStatusTransition(Byte currentStatus, Integer targetStatus) {
        // 校验目标状态范围
        if (targetStatus == null || targetStatus < 1 || targetStatus > 3) {
            throw new IllegalArgumentException("无效的目标状态，只能是1(拒绝)、2(初审通过)或3(终审通过)");
        }

        // 当前状态不能流转的情况
        if (currentStatus == 1) {
            throw new IllegalArgumentException("已拒绝的市场不能变更状态");
        }
        if (currentStatus == 3) {
            throw new IllegalArgumentException("已终审通过的市场不能变更状态");
        }

        // 状态流转规则校验
        switch (currentStatus) {
            case 0: // 待审核
                if (targetStatus != 1 && targetStatus != 2) {
                    throw new IllegalArgumentException("待审核状态只能变更为已拒绝(1)或初审通过(2)");
                }
                break;
            case 2: // 初审通过
                if (targetStatus != 1 && targetStatus != 3) {
                    throw new IllegalArgumentException("初审通过状态只能变更为已拒绝(1)或终审通过(3)");
                }
                break;
            default:
                throw new IllegalArgumentException("无效的当前状态");
        }
    }

    /**
     * 查询各状态的统计数量
     */
    private Map<String, Integer> getStatusCounts() {
        Map<String, Integer> counts = new HashMap<>();

        // 查询待审核 (pre-review) - market_status = 0
        Long preReviewCount = marketMapper.selectCount(
            new QueryWrapper<Market>().eq(Market.MARKET_STATUS, 0)
        );
        counts.put("preReview", preReviewCount.intValue());

        // 查询审核通过 (final-review) - market_status = 2
        Long finalReviewCount = marketMapper.selectCount(
            new QueryWrapper<Market>().eq(Market.MARKET_STATUS, 2)
        );
        counts.put("finalReview", finalReviewCount.intValue());

        // 查询已发布 (approved) - market_status = 3
        Long approvedCount = marketMapper.selectCount(
            new QueryWrapper<Market>().eq(Market.MARKET_STATUS, 3)
        );
        counts.put("approved", approvedCount.intValue());

        // 查询已拒绝 (rejected) - market_status = 1
        Long rejectedCount = marketMapper.selectCount(
            new QueryWrapper<Market>().eq(Market.MARKET_STATUS, 1)
        );
        counts.put("rejected", rejectedCount.intValue());

        return counts;
    }

    @Override
    public AdminStatisticsVO getStatistics() {
        AdminStatisticsVO stats = new AdminStatisticsVO();

        // 1. 总市场数：状态大于等于开启状态的市场数（状态>=4）
        Long totalMarkets = marketMapper.selectCount(
            new QueryWrapper<Market>().ge(Market.MARKET_STATUS, 4)
        );
        stats.setTotalMarkets(totalMarkets.intValue());

        // 2. 活跃市场数：市场开启数（状态=4已发布上链open）
        QueryWrapper<Market> activeWrapper = new QueryWrapper<Market>().eq(Market.MARKET_STATUS, 4);
        Long activeMarkets = marketMapper.selectCount(activeWrapper);
        stats.setActiveMarkets(activeMarkets.intValue());

        // 3. 待审核市场数：状态<3且不为拒绝（状态=0待审核 或 状态=2初审通过）
        QueryWrapper<Market> pendingWrapper = new QueryWrapper<Market>()
            .lt(Market.MARKET_STATUS, 3)
            .ne(Market.MARKET_STATUS, 1);
        Long pendingReview = marketMapper.selectCount(pendingWrapper);
        stats.setPendingReview(pendingReview.intValue());

        // 4. 总流动性：所有active市场（状态=4）的流动性之和
        List<Market> activeMarketList = marketMapper.selectList(activeWrapper);
        BigDecimal totalLiquidity = activeMarketList.stream()
            .map(Market::getBaseLiquidity)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalLiquidity(totalLiquidity);

        return stats;
    }
}
