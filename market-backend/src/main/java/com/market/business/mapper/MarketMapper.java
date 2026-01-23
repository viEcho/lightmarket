package com.market.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.market.business.entity.Market;
import org.apache.ibatis.annotations.Param;

/**
 * Market Mapper
 *
 * @author viEcho
 * @date 2026/01/21
 */
public interface MarketMapper extends BaseMapper<Market> {


    /**
     * 更新市场地址和状态
     *
     * @param marketId 市场ID
     * @param marketAddress 市场合约地址
     * @param chainId 链上合约对应的id
     * @param marketStatus 市场状态
     * @param updatedTime 更新时间
     * @return 影响行数
     */
    int updateMarketAddressAndStatus(@Param("marketId") String marketId,
                                    @Param("marketAddress") String marketAddress,
                                     @Param("chainId") String chainId,
                                    @Param("marketStatus") Integer marketStatus,
                                    @Param("updatedTime") java.util.Date updatedTime);
}