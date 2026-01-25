package com.market.business.service;

import com.market.business.entity.Market;

/**
 * 市场事件监听服务接口
 *
 * @author viEcho
 * @date 2026/01/25
 */
public interface MarketEventListener {

    /**
     * 启动所有市场的监听
     */
    void startListening();

    /**
     * 为单个市场添加监听
     *
     * @param market 市场信息
     */
    void addMarketListener(Market market);

    /**
     * 移除市场监听
     *
     * @param marketAddress 市场合约地址
     */
    void removeMarketListener(String marketAddress);

    /**
     * 停止所有监听
     */
    void stopListening();

    /**
     * 手动刷新指定市场的价格
     *
     * @param marketAddress 市场合约地址
     */
    void refreshPrice(String marketAddress);
}
