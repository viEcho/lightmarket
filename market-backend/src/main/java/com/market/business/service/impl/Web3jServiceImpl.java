package com.market.business.service.impl;

import com.market.business.config.Web3jProperties;
import com.market.business.service.Web3jService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.List;

/**
 * Web3j 服务实现类
 *
 * @author echo
 * @date 2026/01/23
 */
@Slf4j
@Service
public class Web3jServiceImpl implements Web3jService {

    @Resource
    private Web3jProperties web3jProperties;

    private Web3j web3j;

    /**
     * 初始化Web3j客户端
     */
    @PostConstruct
    public void init() {
        try {
            String rpcUrl = web3jProperties.getRpcUrl();
            if (StringUtils.hasText(rpcUrl)) {
                web3j = Web3j.build(new HttpService(rpcUrl));
                log.info("[Web3jService] Web3j客户端初始化成功, rpcUrl: {}", rpcUrl);

                // 测试连接
                BigInteger blockNumber = web3j.ethBlockNumber().send().getBlockNumber();
                log.info("[Web3jService] 当前区块号: {}", blockNumber);
            } else {
                log.warn("[Web3jService] RPC URL未配置，Web3j功能将不可用");
            }
        } catch (Exception e) {
            log.error("[Web3jService] Web3j客户端初始化失败", e);
        }
    }

    @Override
    public TransactionReceipt waitForTransactionReceipt(String txHash) throws Exception {
        if (web3j == null) {
            throw new RuntimeException("Web3j客户端未初始化");
        }
        log.info("[Web3jService] 等待交易确认, txHash: {}", txHash);

        // 优化版：使用指数退避策略
        // 测试网/本地网络：快速确认（3秒起）
        // 主网：12-15秒出块，也够用
        // 超时时间：5分钟（大多数交易2-3分钟内确认）

        int sleepDuration = 3000; // 初始3秒
        int maxSleepDuration = 15000; // 最大15秒
        int totalWaitTime = 0;
        int timeoutMs = 300000; // 5分钟超时

        while (totalWaitTime < timeoutMs) {
            try {
                EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(txHash).send();

                if (receipt.getTransactionReceipt().isPresent()) {
                    TransactionReceipt transactionReceipt = receipt.getTransactionReceipt().get();

                    // 验证交易状态
                    if ("0x1".equals(transactionReceipt.getStatus())) {
                        log.info("[Web3jService] 交易已确认成功, txHash: {}, gasUsed: {}, 等待时间: {}秒",
                                txHash, transactionReceipt.getGasUsed(), totalWaitTime / 1000);
                    } else {
                        log.warn("[Web3jService] 交易已确认但失败, txHash: {}, status: {}",
                                txHash, transactionReceipt.getStatus());
                    }
                    return transactionReceipt;
                }

                // 指数退避：每次等待时间增加，但不超过最大值
                Thread.sleep(sleepDuration);
                totalWaitTime += sleepDuration;
                sleepDuration = Math.min(sleepDuration * 2, maxSleepDuration);

                // 每分钟记录一次进度
                if (totalWaitTime % 60000 < sleepDuration) {
                    log.info("[Web3jService] 等待交易确认中... 已等待: {}秒", totalWaitTime / 1000);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("交易等待被中断: " + txHash, e);
            }
        }

        throw new RuntimeException("交易确认超时(5分钟): " + txHash);
    }

    @Override
    public String getMarketAddressFromTx(String txHash, String onChainMarketId) throws Exception {
        log.info("[Web3jService] 从交易中获取市场地址, txHash: {}, onChainMarketId: {}", txHash, onChainMarketId);

        // 1. 等待交易确认
        TransactionReceipt receipt = waitForTransactionReceipt(txHash);

        // 2. 检查交易状态
        if (!"0x1".equals(receipt.getStatus())) {
            throw new RuntimeException("交易失败: " + txHash);
        }

        // 3. 解析日志获取MarketCreated事件
        List<Log> logs = receipt.getLogs();
        if (logs.isEmpty()) {
            throw new RuntimeException("交易日志为空: " + txHash);
        }

        // MarketCreated事件签名
        // event MarketCreated(bytes32 indexed marketId, address indexed market, address indexed creator, uint256 endTime, uint256 initialLiquidity)
        // 计算事件签名：keccak256("MarketCreated(bytes32,address,address,uint256,uint256)")
        String eventSignatureRaw = "MarketCreated(bytes32,address,address,uint256,uint256)";
        byte[] hash = org.web3j.crypto.Hash.sha3(eventSignatureRaw.getBytes());
        String eventSignature = Numeric.toHexString(hash);

        log.info("[Web3jService] MarketCreated事件签名: {}", eventSignature);

        // 4. 遍历日志找到MarketCreated事件
        for (Log transactionLog : logs) {
            List<String> topics = transactionLog.getTopics();
            if (topics.isEmpty()) {
                continue;
            }

            String topic0 = topics.get(0);
            log.debug("[Web3jService] 日志topics[0]: {}", topic0);

            // 检查是否是MarketCreated事件
            if (topic0.equalsIgnoreCase(eventSignature)) {
                // 从topics中提取参数
                // topics[0] = 事件签名
                // topics[1] = marketId (indexed)
                // topics[2] = market (indexed, address)
                // topics[3] = creator (indexed, address)

                if (topics.size() >= 3) {
                    String marketAddress = topics.get(2);
                    // 地址是32字节，需要取最后20字节
                    marketAddress = "0x" + marketAddress.substring(marketAddress.length() - 40);

                    log.info("[Web3jService] 成功获取市场地址, marketId: {}, marketAddress: {}",
                            onChainMarketId, marketAddress);
                    return marketAddress;
                }
            }
        }

        // 如果从日志中解析失败，尝试通过查询合约状态获取
        log.warn("[Web3jService] 无法从交易日志中解析市场地址，尝试查询合约状态");
        return getMarketAddressByQuery(onChainMarketId);
    }

    /**
     * 通过查询合约状态获取市场地址
     * 注意：这里需要MarketFactory合约的Java包装类
     * 或者使用eth_call直接调用合约的getMarketAddress方法
     */
    private String getMarketAddressByQuery(String onChainMarketId) throws Exception {
        log.info("[Web3jService] 通过合约查询获取市场地址, onChainMarketId: {}", onChainMarketId);

        // TODO: 实现合约调用逻辑
        // 1. 确保marketId格式正确（bytes32）
        // 2. 调用MarketFactory的getMarketAddress(bytes32)方法
        // 3. 解析返回值获取市场地址

        throw new RuntimeException("合约查询功能尚未实现，请先部署MarketFactory合约并配置地址");
    }

    @Override
    public BigInteger getCurrentBlockNumber() throws Exception {
        if (web3j == null) {
            throw new RuntimeException("Web3j客户端未初始化");
        }
        return web3j.ethBlockNumber().send().getBlockNumber();
    }
}
