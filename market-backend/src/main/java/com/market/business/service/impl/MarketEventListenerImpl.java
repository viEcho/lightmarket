package com.market.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.market.business.config.Web3jProperties;
import com.market.business.contract.PredictionMarket;
import com.market.business.entity.Market;
import com.market.business.mapper.MarketMapper;
import com.market.business.service.MarketEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.http.HttpService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 市场事件监听服务实现
 * 功能：监听已部署市场的交易事件，实时更新价格
 *
 * @author viEcho
 * @date 2026/01/25
 */
@Slf4j
@Service
public class MarketEventListenerImpl implements MarketEventListener {

    @Autowired
    private MarketMapper marketMapper;

    @Autowired
    private Web3jProperties web3jProperties;

    private Web3j web3j;

    // 存储每个市场最后处理的区块号
    private final Map<String, BigInteger> lastProcessedBlocks = new ConcurrentHashMap<>();

    // 定时任务执行器
    private ScheduledExecutorService scheduler;

    // 是否正在运行
    private volatile boolean isRunning = false;

    /**
     * 应用启动完成后自动开始监听
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("[MarketEventListener] 应用启动完成，开始初始化事件监听服务");
        initWeb3j();
        startListening();
    }

    /**
     * 初始化 Web3j 客户端
     */
    private void initWeb3j() {
        try {
            String rpcUrl = web3jProperties.getRpcUrl();
            if (rpcUrl != null && !rpcUrl.isEmpty()) {
                web3j = Web3j.build(new HttpService(rpcUrl));

                // 测试连接
                BigInteger blockNumber = web3j.ethBlockNumber().send().getBlockNumber();
                log.info("[MarketEventListener] Web3j 客户端初始化成功, 当前区块: {}", blockNumber);
            } else {
                log.warn("[MarketEventListener] RPC URL 未配置，事件监听功能将不可用");
            }
        } catch (Exception e) {
            log.error("[MarketEventListener] Web3j 初始化失败", e);
        }
    }

    @Override
    public void startListening() {
        if (isRunning) {
            log.warn("[MarketEventListener] 监听服务已在运行中");
            return;
        }

        if (web3j == null) {
            log.error("[MarketEventListener] Web3j 未初始化，无法启动监听");
            return;
        }

        isRunning = true;
        log.info("[MarketEventListener] 启动事件监听服务");

        // 查询所有已部署的市场（状态为 5-已发布上链）
        QueryWrapper<Market> wrapper = new QueryWrapper<>();
        wrapper.eq("market_status", 5)
               .isNotNull("market_address")
               .ne("market_address", "");

        List<Market> markets = marketMapper.selectList(wrapper);
        log.info("[MarketEventListener] 找到 {} 个已部署的市场", markets.size());

        // 为每个市场添加监听
        for (Market market : markets) {
            try {
                addMarketListener(market);
            } catch (Exception e) {
                log.error("[MarketEventListener] 添加市场监听失败: {}", market.getMarketAddress(), e);
            }
        }

        // 启动定时任务：每10秒检查一次新事件
        startScheduledTasks();
    }

    @Override
    public void addMarketListener(Market market) {
        if (market.getMarketAddress() == null || market.getMarketAddress().isEmpty()) {
            log.warn("[MarketEventListener] 市场地址为空，无法添加监听: marketId={}", market.getMarketId());
            return;
        }

        String marketAddress = market.getMarketAddress();
        log.info("[MarketEventListener] 添加市场监听: marketAddress={}", marketAddress);

        // 初始化最后处理区块为当前区块
        if (!lastProcessedBlocks.containsKey(marketAddress)) {
            try {
                BigInteger currentBlock = web3j.ethBlockNumber().send().getBlockNumber();
                // 从前100个区块开始监听，避免遗漏
                lastProcessedBlocks.put(marketAddress, currentBlock.subtract(BigInteger.valueOf(100)));
                log.info("[MarketEventListener] 初始化监听区块: marketAddress={}, startBlock={}",
                    marketAddress, lastProcessedBlocks.get(marketAddress));
            } catch (Exception e) {
                log.error("[MarketEventListener] 获取当前区块失败", e);
            }
        }
    }

    @Override
    public void removeMarketListener(String marketAddress) {
        lastProcessedBlocks.remove(marketAddress);
        log.info("[MarketEventListener] 移除市场监听: marketAddress={}", marketAddress);
    }

    @Override
    public void stopListening() {
        isRunning = false;
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("[MarketEventListener] 事件监听服务已停止");
    }

    @Override
    public void refreshPrice(String marketAddress) {
        updateMarketPrice(marketAddress);
    }

    /**
     * 启动定时任务
     */
    private void startScheduledTasks() {
        scheduler = Executors.newScheduledThreadPool(2);

        // 任务1：每10秒检查一次新事件并更新价格
        scheduler.scheduleAtFixedRate(() -> {
            if (isRunning && !lastProcessedBlocks.isEmpty()) {
                checkNewEvents();
            }
        }, 10, 10, TimeUnit.SECONDS);

        // 任务2：每30秒强制刷新所有活跃市场的价格（兜底机制）
        scheduler.scheduleAtFixedRate(() -> {
            if (isRunning) {
                refreshAllPrices();
            }
        }, 30, 30, TimeUnit.SECONDS);

        log.info("[MarketEventListener] 定时任务已启动");
    }

    /**
     * 检查新事件并更新价格
     */
    private void checkNewEvents() {
        try {
            BigInteger currentBlock = web3j.ethBlockNumber().send().getBlockNumber();

            for (Map.Entry<String, BigInteger> entry : lastProcessedBlocks.entrySet()) {
                String marketAddress = entry.getKey();
                BigInteger lastBlock = entry.getValue();

                // 如果有新区块，检查是否有交易事件
                if (currentBlock.compareTo(lastBlock) > 0) {
                    checkMarketEvents(marketAddress, lastBlock, currentBlock);

                    // 更新最后处理区块
                    lastProcessedBlocks.put(marketAddress, currentBlock);
                }
            }
        } catch (Exception e) {
            log.error("[MarketEventListener] 检查新事件失败", e);
        }
    }

    /**
     * 检查指定市场的交易事件
     */
    private void checkMarketEvents(String marketAddress, BigInteger fromBlock, BigInteger toBlock) {
        try {
            // 监听 BoughtYes 和 SoldYes 事件
            EthFilter filter = new EthFilter(
                org.web3j.protocol.core.DefaultBlockParameter.valueOf(fromBlock),
                org.web3j.protocol.core.DefaultBlockParameter.valueOf(toBlock),
                marketAddress
            );

            // 获取事件日志
            var logResults = web3j.ethGetLogs(filter).send().getLogs();

            if (!logResults.isEmpty()) {
                log.info("[MarketEventListener] 发现 {} 个交易事件: marketAddress={}",
                    logResults.size(), marketAddress);

                // 有交易发生，更新价格
                updateMarketPrice(marketAddress);
            }

        } catch (Exception e) {
            log.error("[MarketEventListener] 检查市场事件失败: marketAddress={}", marketAddress, e);
        }
    }

    /**
     * 更新市场价格
     */
    private void updateMarketPrice(String marketAddress) {
        try {
            // 加载合约
            PredictionMarket contract = PredictionMarket.load(marketAddress, web3j);

            // 查询价格
            BigInteger yesPrice = contract.getYesPrice();
            BigInteger noPrice = contract.getNoPrice();

            // 转换为 BigDecimal (除以100)
            BigDecimal yesPriceDecimal = new BigDecimal(yesPrice).divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
            BigDecimal noPriceDecimal = new BigDecimal(noPrice).divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);

            // 更新数据库
            marketMapper.updatePrice(marketAddress, yesPriceDecimal, noPriceDecimal);

            log.info("[MarketEventListener] 价格已更新: marketAddress={}, yesPrice={}, noPrice={}",
                marketAddress, yesPriceDecimal, noPriceDecimal);

        } catch (Exception e) {
            log.error("[MarketEventListener] 更新价格失败: marketAddress={}", marketAddress, e);
        }
    }

    /**
     * 刷新所有活跃市场的价格（兜底机制）
     */
    private void refreshAllPrices() {
        try {
            QueryWrapper<Market> wrapper = new QueryWrapper<>();
            wrapper.eq("market_status", 5)
                   .isNotNull("market_address")
                   .ne("market_address", "");

            List<Market> markets = marketMapper.selectList(wrapper);

            log.debug("[MarketEventListener] 强制刷新 {} 个市场的价格", markets.size());

            for (Market market : markets) {
                updateMarketPrice(market.getMarketAddress());
            }

        } catch (Exception e) {
            log.error("[MarketEventListener] 批量刷新价格失败", e);
        }
    }
}
