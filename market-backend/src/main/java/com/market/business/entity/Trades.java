package com.market.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 预测市场成交表（撮合双方）
 *
 * @author viEcho
 * @date 2026/01/23
 */
@Data
@TableName("trades")
public class Trades {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 市场ID
     */
    private String marketId;

    /**
     * 买方用户ID
     */
    private Long buyUserId;

    /**
     * 卖方用户ID
     */
    private Long sellUserId;

    /**
     * 买方订单ID
     */
    private Long buyOrderId;

    /**
     * 卖方订单ID
     */
    private Long sellOrderId;

    /**
     * 买方钱包地址
     */
    private String buyerAddress;

    /**
     * 卖方钱包地址
     */
    private String sellerAddress;

    /**
     * 预测方向：0-NO，1-YES
     */
    private Integer tokenType;

    /**
     * 成交的预测合约数量（token 数量，买卖双方一致）
     */
    private BigDecimal amount;

    /**
     * 预测合约单价，单位：USDC / token
     */
    private BigDecimal price;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    public static final String ID = "id";

    public static final String MARKET_ID = "market_id";

    public static final String BUY_USER_ID = "buy_user_id";

    public static final String SELL_USER_ID = "sell_user_id";

    public static final String BUY_ORDER_ID = "buy_order_id";

    public static final String SELL_ORDER_ID = "sell_order_id";

    public static final String BUYER_ADDRESS = "buyer_address";

    public static final String SELLER_ADDRESS = "seller_address";

    public static final String TOKEN_TYPE = "token_type";

    public static final String AMOUNT = "amount";

    public static final String PRICE = "price";

    public static final String CREATED_TIME = "created_time";

    public static final String UPDATED_TIME = "updated_time";
}