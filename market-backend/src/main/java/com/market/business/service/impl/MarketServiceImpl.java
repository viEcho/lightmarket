package com.market.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.market.business.entity.Market;
import com.market.business.entity.UserMarket;
import com.market.business.global.PageVO;
import com.market.business.mapper.MarketMapper;
import com.market.business.mapper.UserMarketMapper;
import com.market.business.query.MarketAddQuery;
import com.market.business.query.MarketPageQuery;
import com.market.business.service.MarketService;
import com.market.business.vo.MarketVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Market Service 实现类
 *
 * @author echo
 * @date 2026/01/21
 */
@Slf4j
@Service
public class MarketServiceImpl implements MarketService {

    @Resource
    private MarketMapper marketMapper;

    @Resource
    private UserMarketMapper userMarketMapper;

    @Override
    public PageVO<MarketVO> findPage(MarketPageQuery query) {
        // 构建查询条件
        QueryWrapper<Market> queryWrapper = new QueryWrapper<>();

        // 市场状态
        if (query.getMarketStatus() != null) {
            queryWrapper.eq(Market.MARKET_STATUS, query.getMarketStatus());
        }

        // 标签筛选（使用 FIND_IN_SET 查询逗号分隔的 tags 字段）
        if (query.getTagCode() != null && !query.getTagCode().isEmpty()) {
            queryWrapper.apply("FIND_IN_SET({0}, tags)", query.getTagCode());
        }

        // 关键词搜索
        if (StringUtils.isNotBlank(query.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Market.TITLE, query.getKeyword())
                    .or()
                    .like(Market.DESCRIPTION, query.getKeyword())
                    .or()
                    .like(Market.TAGS, query.getKeyword())
            );
        }

        // 排序：按权重降序，创建时间降序
        queryWrapper.orderByDesc(Market.WEIGHT)
                .orderByDesc(Market.CREATED_TIME);

        IPage<Market> resultPage = marketMapper.selectPage(new Page<>(query.getNum(), query.getSize()), queryWrapper);

        // 转换 Entity 为 VO
        List<MarketVO> voList = resultPage.getRecords().stream()
                .map(MarketVO::fromEntity)
                .collect(Collectors.toList());

        // 构建返回结果
        PageVO<MarketVO> page = new PageVO<>();
        page.setNum(query.getNum());
        page.setSize(query.getSize());
        page.setTotal((int) resultPage.getTotal());
        page.setList(voList);
        return page;
    }

    @Override
    public PageVO<MarketVO> findMyList(MarketPageQuery query) {
        // 1. 先查询 user_market 表，按创建时间倒序分页
        QueryWrapper<UserMarket> userMarketWrapper = new QueryWrapper<>();
        userMarketWrapper.eq(UserMarket.USER_ID, query.getUserId())
                .orderByDesc(UserMarket.CREATED_TIME);

        IPage<UserMarket> userMarketPage = userMarketMapper.selectPage(
                new Page<>(query.getNum(), query.getSize()),
                userMarketWrapper
        );

        // 如果没有数据，直接返回空结果
        if (userMarketPage.getRecords().isEmpty()) {
            PageVO<MarketVO> emptyPage = new PageVO<>();
            emptyPage.setNum(query.getNum());
            emptyPage.setSize(query.getSize());
            emptyPage.setTotal(0);
            emptyPage.setList(List.of());
            return emptyPage;
        }

        // 2. 提取所有 market_id
        List<String> marketIds = userMarketPage.getRecords().stream()
                .map(UserMarket::getMarketId)
                .collect(Collectors.toList());

        // 3. 批量查询 market 表
        QueryWrapper<Market> marketWrapper = new QueryWrapper<>();
        marketWrapper.in(Market.MARKET_ID, marketIds);

        List<Market> markets = marketMapper.selectList(marketWrapper);

        // 4. 转换为 VO 并按照 user_market 的顺序排序
        Map<String, Market> marketMap = markets.stream()
                .collect(Collectors.toMap(Market::getMarketId, m -> m));

        List<MarketVO> voList = marketIds.stream()
                .map(marketMap::get)
                .filter(m -> m != null)
                .map(MarketVO::fromEntity)
                .collect(Collectors.toList());

        // 5. 构建返回结果
        PageVO<MarketVO> page = new PageVO<>();
        page.setNum(query.getNum());
        page.setSize(query.getSize());
        page.setTotal((int) userMarketPage.getTotal());
        page.setList(voList);
        return page;
    }

    @Override
    public String add(MarketAddQuery query, Long userId) {
        // 1. 解析 closeTime
        Date closeTime = parseCloseTime(query.getCloseTime());
        Date resolveTime = new Date(closeTime.getTime() + 24 * 60 * 60 * 1000L); // +24小时

        // 2. 生成唯一 marketId
        String marketId = generateMarketId();

        // 3. 创建 Market 实体（字段名与数据库表一致）
        Market market = new Market();
        market.setMarketId(marketId);
        market.setUserId(userId);
        market.setTitle(query.getTitle());
        market.setDescription(query.getDescription());
        market.setCategory(query.getCategory().byteValue());
        market.setCloseTime(closeTime);
        market.setResolveTime(resolveTime);
        market.setOracleSource(query.getOracleSource());
        market.setResolutionMethod((byte) 0); // 0-AI裁决
        market.setAiModel(query.getAiModel());
        market.setTags(query.getTags());
        market.setMarketStatus((byte) 0); // 0-待审核
        market.setBaseLiquidity(query.getBaseLiquidity());
        market.setYesPrice(new BigDecimal("0.5"));
        market.setNoPrice(new BigDecimal("0.5"));
        market.setResolvedOutcome((byte) 0); // 0-未结算
        market.setTotalVolume(BigDecimal.ZERO);
        market.setRiskStatus((byte) 0); // 0-正常
        market.setWeight(0);
        market.setChainId(1); // 默认 Ethereum
        market.setCreator(String.valueOf(userId));
        market.setCreatedTime(new Date());
        market.setUpdatedTime(new Date());

        // 4. 插入数据库
        marketMapper.insert(market);

        // 5. 创建用户市场关联记录
        UserMarket userMarket = new UserMarket();
        userMarket.setUserId(userId);
        userMarket.setMarketId(marketId);
        userMarket.setCreatedTime(new Date());
        userMarketMapper.insert(userMarket);

        log.info("Market created successfully: marketId={}, userId={}, title={}",
                marketId, userId, query.getTitle());

        return marketId;
    }

    /**
     * 解析 closeTime 字符串为 Date
     */
    private Date parseCloseTime(String closeTime) {
        try {
            // 尝试 ISO 8601 格式
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            return isoFormat.parse(closeTime);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid close time format. Expected format: yyyy-MM-dd'T'HH:mm:ss", e);
        }
    }

    /**
     * 生成唯一的市场ID
     */
    private String generateMarketId() {
        return "MKT-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
