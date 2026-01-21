package com.market.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 用户参与市场表
 *
 * @author viEcho
 * @date 2026/01/21
 */
@Data
@TableName("user_market")
public class UserMarket {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 市场唯一标识
     */
    private String marketId;

    /**
     * 累计买入
     */
    private BigDecimal totalPay;

    /**
     * 添加时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    public static final String ID = "id";

    public static final String USER_ID = "user_id";

    public static final String MARKET_ID = "market_id";

    public static final String TOTAL_PAY = "total_pay";

    public static final String CREATED_TIME = "created_time";

    public static final String UPDATED_TIME = "updated_time";
}