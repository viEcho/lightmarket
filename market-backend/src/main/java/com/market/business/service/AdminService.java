package com.market.business.service;

import com.market.business.global.PageVO;
import com.market.business.query.AdminApproveQuery;
import com.market.business.query.MarketApproveQuery;
import com.market.business.vo.AdminStatisticsVO;
import com.market.business.vo.MarketVO;

/**
 * Admin Service 接口
 *
 * @author echo
 * @date 2026/01/22
 */
public interface AdminService {

    /**
     * 分页查询审核市场列表
     *
     * @param query 查询参数
     * @return 分页结果，ext 中包含各状态统计
     */
    PageVO<MarketVO> getApproveList(AdminApproveQuery query);

    /**
     * 审批市场
     *
     * @param query 审批请求参数
     * @return 是否成功
     */
    Boolean approveMarket(MarketApproveQuery query);

    /**
     * 获取管理员统计数据
     *
     * @return 统计信息
     */
    AdminStatisticsVO getStatistics();
}
