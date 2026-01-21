package com.market.business.service;

import com.market.business.global.PageVO;
import com.market.business.query.MarketAddQuery;
import com.market.business.query.MarketPageQuery;
import com.market.business.vo.MarketVO;

/**
 * Market Service 接口
 *
 * @author echo
 * @date 2026/01/21
 */
public interface MarketService {

    /**
     * 分页查询市场列表
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageVO<MarketVO> findPage(MarketPageQuery query);

    /**
     * 分页查询用户参与的市场列表
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageVO<MarketVO> findMyList(MarketPageQuery query);

    /**
     * 创建市场
     *
     * @param query 创建市场请求参数
     * @param userId 用户ID
     * @return 创建的市场ID
     */
    String add(MarketAddQuery query, Long userId);
}
