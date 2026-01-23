package com.market.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.market.business.entity.Market;
import com.market.business.entity.User;
import com.market.business.entity.UserMarket;
import com.market.business.global.PageVO;
import com.market.business.mapper.MarketMapper;
import com.market.business.mapper.UserMapper;
import com.market.business.mapper.UserMarketMapper;
import com.market.business.query.MarketAddQuery;
import com.market.business.query.MarketDeployingQuery;
import com.market.business.query.MarketOpeningQuery;
import com.market.business.query.MarketPageQuery;
import com.market.business.service.MarketService;
import com.market.business.service.Web3jService;
import com.market.business.vo.MarketVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
    @Autowired
    private UserMapper userMapper;

    @Resource
    private Web3jService web3jService;

    @Override
    public PageVO<MarketVO> findPage(MarketPageQuery query) {
        // 构建查询条件
        QueryWrapper<Market> queryWrapper = new QueryWrapper<>();

        // 市场状态
        if (query.getMarketStatus() != null) {
            queryWrapper.ge(Market.MARKET_STATUS, query.getMarketStatus());
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

        // 排序：按创建时间,权重降序降序
        queryWrapper
                .orderByDesc(Market.CREATED_TIME)
                .orderByDesc(Market.WEIGHT);

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
                .filter(Objects::nonNull)
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
        User user = userMapper.selectById(userId);
        String marketId = generateMarketId();

        // 3. 创建 Market 实体（字段名与数据库表一致）
        Market market = new Market();
        market.setMarketId(marketId);
        market.setUserId(userId);
        market.setTitle(query.getTitle());
        market.setDescription(query.getDescription());
        market.setCloseTime(closeTime);
        market.setResolveTime(resolveTime);
        market.setOracleSource(query.getOracleSource());
        market.setResolutionMethod(0); // 0-AI裁决
        market.setAiModel(query.getAiModel());
        market.setTags(query.getTags());
        market.setMarketStatus(0); // 0-待审核
        market.setBaseLiquidity(query.getBaseLiquidity());
        market.setYesPrice(new BigDecimal("0.5"));
        market.setNoPrice(new BigDecimal("0.5"));
        market.setResolvedOutcome(0); // 0-未结算
        market.setTotalVolume(BigDecimal.ZERO);
        market.setRiskStatus(0); // 0-正常
        market.setWeight(0);
        market.setChainId(1); // 默认 Ethereum
        market.setCreator(user.getUid());
        market.setCreatedTime(new Date());
        market.setUpdatedTime(new Date());

        // 4. 插入数据库
        marketMapper.insert(market);

        // 5. 创建用户市场关联记录
        UserMarket userMarket = new UserMarket();
        userMarket.setUserId(userId);
        userMarket.setMarketId(marketId);
        userMarket.setCreatedTime(new Date());
        userMarket.setUpdatedTime(new Date());
        userMarket.setTotalPay(BigDecimal.ZERO);
        userMarketMapper.insert(userMarket);

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

    @Override
    public Boolean opening(MarketOpeningQuery query) {
        log.info("[MarketServiceImpl] 开始发布市场, marketId: {}, userId: {}", query.getMarketId(), query.getUserId());

        // 1. 根据marketId查询市场
        QueryWrapper<Market> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(Market.MARKET_ID, query.getMarketId());
        Market market = marketMapper.selectOne(queryWrapper);

        if (market == null) {
            log.error("[MarketServiceImpl] 市场不存在, marketId: {}", query.getMarketId());
            throw new RuntimeException("市场不存在");
        }

        // 2. 校验市场创建人
        if (!market.getUserId().equals(query.getUserId())) {
            log.error("[MarketServiceImpl] 用户不是市场创建人, marketId: {}, userId: {}, creatorId: {}",
                    query.getMarketId(), query.getUserId(), market.getUserId());
            throw new RuntimeException("无权操作此市场");
        }

        // 3. 校验市场状态 - 只有终审通过(3)的市场才能开始发布
        if (market.getMarketStatus() != 3) {
            log.error("[MarketServiceImpl] 市场状态不正确, marketId: {}, currentStatus: {}",
                    query.getMarketId(), market.getMarketStatus());
            throw new RuntimeException("市场状态不正确，只有终审通过的市场才能发布");
        }

        // 4. 将状态改为4(发布中)
        market.setMarketStatus(4);
        market.setUpdatedTime(new Date());
        int updateResult = marketMapper.updateById(market);

        log.info("[MarketServiceImpl] 市场状态更新为发布中, marketId: {}, updateResult: {}",
                query.getMarketId(), updateResult);
        return updateResult > 0;
    }

    @Override
    public Boolean deploying(MarketDeployingQuery query) {
        log.info("[MarketServiceImpl] 市场部署中, marketId: {}, txHash: {}, onChainMarketId: {}",
                query.getMarketId(), query.getTxHash(), query.getOnChainMarketId());

        // 1. 根据marketId查询市场
        QueryWrapper<Market> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(Market.MARKET_ID, query.getMarketId());
        Market market = marketMapper.selectOne(queryWrapper);

        if (market == null) {
            log.error("[MarketServiceImpl] 市场不存在, marketId: {}", query.getMarketId());
            throw new RuntimeException("市场不存在");
        }

        // 2. 校验市场状态 - 只有发布中(4)的市场才能更新交易信息
        if (market.getMarketStatus() != 4) {
            log.error("[MarketServiceImpl] 市场状态不正确, marketId: {}, currentStatus: {}",
                    query.getMarketId(), market.getMarketStatus());
            throw new RuntimeException("市场状态不正确");
        }

        // 3. 更新交易哈希（保持状态为4，等待异步任务监听链上事件）
        market.setTxHash(query.getTxHash());
        market.setUpdatedTime(new Date());
        int updateResult = marketMapper.updateById(market);

        log.info("[MarketServiceImpl] 市场交易哈希已更新, marketId: {}, txHash: {}, updateResult: {}",
                query.getMarketId(), query.getTxHash(), updateResult);

        // 4. 启动异步任务监听链上事件
        CompletableFuture.runAsync(() -> {
            listenForMarketCreatedEvent(query.getMarketId(), query.getTxHash(), query.getOnChainMarketId());
        });

        return updateResult > 0;
    }

    /**
     * 异步监听链上MarketCreated事件
     * 通过Web3j监听交易确认并解析MarketCreated事件
     */
    private void listenForMarketCreatedEvent(String marketId, String txHash, String onChainMarketId) {
        log.info("[MarketServiceImpl] 开始监听链上MarketCreated事件, marketId: {}, txHash: {}, onChainMarketId: {}",
                marketId, txHash, onChainMarketId);

        try {
            // 1. 等待交易确认并获取交易收据
            log.info("[MarketServiceImpl] 等待交易确认...");
            String marketAddress = web3jService.getMarketAddressFromTx(txHash, onChainMarketId);
            log.info("[MarketServiceImpl] 成功获取市场地址, marketId: {}, marketAddress: {}",
                    marketId, marketAddress);

            // 2. 更新数据库 - 使用自定义mapper方法更新marketAddress和状态
            Date updatedTime = new Date();
            int updateResult = marketMapper.updateMarketAddressAndStatus(
                    marketId,
                    marketAddress,
                    onChainMarketId,
                    5, // 5-已发布上链open
                    updatedTime
            );

            if (updateResult > 0) {
                log.info("[MarketServiceImpl] 市场发布完成, marketId: {}, status: 5, marketAddress: {}",
                        marketId, marketAddress);
            } else {
                log.error("[MarketServiceImpl] 市场更新失败, marketId: {}", marketId);
            }

        } catch (Exception e) {
            log.error("[MarketServiceImpl] 监听链上事件失败, marketId: {}, error: {}",
                    marketId, e.getMessage(), e);

            // 发生异常时，可以考虑将状态重置或者标记为失败状态
            // 这里暂时不做处理，保持状态为4(发布中)
            // 可以考虑添加重试逻辑或者通知管理员
        }
    }
}
