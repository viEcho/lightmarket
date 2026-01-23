package com.market.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 用户成交记录表（单边视角）
 *
 * @author viEcho
 * @date 2026/01/23
 */
@Data
@TableName("user_trades")
public class UserTrades {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 撮合成交ID
     */
    private Long tradeId;

    /**
     * 用户订单ID
     */
    private Long orderId;

    /**
     * 市场ID
     */
    private String marketId;

    /**
     * 用户钱包地址
     */
    private String walletAddress;

    /**
     * 预测方向：0-NO，1-YES
     */
    private Integer tokenType;

    /**
     * 1-买入，-1-卖出
     */
    private Integer side;

    /**
     * 成交价格，单位：USDC/token
     */
    private BigDecimal price;

    /**
     * 成交数量，成交的预测合约数量（token 数量，买卖双方一致）
     */
    private BigDecimal amount;

    /**
     * 成交token数量
     */
    private BigDecimal tradeAmount;

    /**
     * 手续费数量
     */
    private BigDecimal feeAmount;

    /**
     * 链上结算状态：0-PENDING / 1-SUCCESS / -1-FAILED
     */
    private Integer txStatus;

    /**
     * 链上结算交易hash
     */
    private String txHash;

    /**
     * 手续费币种：1-USDC（预留扩展）
     */
    private Integer feeCurrency;

    /**
     * 成交时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    public static final String ID = "id";

    public static final String USER_ID = "user_id";

    public static final String TRADE_ID = "trade_id";

    public static final String ORDER_ID = "order_id";

    public static final String MARKET_ID = "market_id";

    public static final String WALLET_ADDRESS = "wallet_address";

    public static final String TOKEN_TYPE = "token_type";

    public static final String SIDE = "side";

    public static final String PRICE = "price";

    public static final String AMOUNT = "amount";

    public static final String TRADE_AMOUNT = "trade_amount";

    public static final String FEE_AMOUNT = "fee_amount";

    public static final String TX_STATUS = "tx_status";

    public static final String TX_HASH = "tx_hash";

    public static final String FEE_CURRENCY = "fee_currency";

    public static final String CREATED_TIME = "created_time";

    public static final String UPDATED_TIME = "updated_time";
}