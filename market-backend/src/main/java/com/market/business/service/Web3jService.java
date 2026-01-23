package com.market.business.service;

import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.math.BigInteger;
import java.util.function.Consumer;

/**
 * Web3j 服务接口
 *
 * @author echo
 * @date 2026/01/23
 */
public interface Web3jService {

    /**
     * 等待交易确认并获取交易收据
     *
     * @param txHash 交易哈希
     * @return 交易收据
     */
    TransactionReceipt waitForTransactionReceipt(String txHash) throws Exception;

    /**
     * 从交易收据中解析MarketCreated事件，获取市场地址
     *
     * @param txHash 交易哈希
     * @param onChainMarketId 链上市场ID
     * @return 市场合约地址
     */
    String getMarketAddressFromTx(String txHash, String onChainMarketId) throws Exception;

    /**
     * 获取当前区块号
     *
     * @return 当前区块号
     */
    BigInteger getCurrentBlockNumber() throws Exception;
}
