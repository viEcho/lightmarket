package com.market.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 预测市场订单簿表（链下撮合核心）
 *
 * @author viEcho
 * @date 2026/01/23
 */
@Data
@TableName("orders")
public class Orders {
    /**
     * 主键ID-订单id
     */
    private Long id;

    /**
     * 父单id,扩展字段用户可拆单设计
     */
    private Long parentId;

    /**
     * 前端幂等订单ID
     */
    private String clientOrderId;

    /**
     * 市场ID（链下）
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
     * 订单类型：1-买入，-1-卖出
     */
    private Integer side;

    /**
     * 订单类型：0-市价单，1-限价单
     */
    private Integer orderType;

    /**
     * 委托价格
     */
    private BigDecimal price;

    /**
     * 委托数量
     */
    private BigDecimal amount;

    /**
     * 已成交数量
     */
    private BigDecimal filledAmount;

    /**
     * 剩余未成交数量
     */
    private BigDecimal remainingAmount;

    /**
     * 买单，冻结USDC金额
     */
    private BigDecimal lockedBuyAmount;

    /**
     * 卖单，冻结token数量；某些token可能为小数
     */
    private BigDecimal lockedSellAmount;

    /**
     * 订单状态：1-OPEN,2-FILLED,3-CANCELLED,4-EXPIRE
     */
    private Integer status;

    /**
     * 链上结算交易hash
     */
    private String chainTxHash;

    /**
     * 链上结算状态：0-PENDING / 1-SUCCESS / -1-FAILED
     */
    private Integer txStatus;

    /**
     * 订单过期时间，限价单
     */
    private Date expireTime;

    /**
     * 订单取消时间
     */
    private Date cancelledTime;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    public static final String ID = "id";

    public static final String PARENT_ID = "parent_id";

    public static final String CLIENT_ORDER_ID = "client_order_id";

    public static final String MARKET_ID = "market_id";

    public static final String WALLET_ADDRESS = "wallet_address";

    public static final String TOKEN_TYPE = "token_type";

    public static final String SIDE = "side";

    public static final String ORDER_TYPE = "order_type";

    public static final String PRICE = "price";

    public static final String AMOUNT = "amount";

    public static final String FILLED_AMOUNT = "filled_amount";

    public static final String REMAINING_AMOUNT = "remaining_amount";

    public static final String LOCKED_BUY_AMOUNT = "locked_buy_amount";

    public static final String LOCKED_SELL_AMOUNT = "locked_sell_amount";

    public static final String STATUS = "status";

    public static final String CHAIN_TX_HASH = "chain_tx_hash";

    public static final String TX_STATUS = "tx_status";

    public static final String EXPIRE_TIME = "expire_time";

    public static final String CANCELLED_TIME = "cancelled_time";

    public static final String CREATED_TIME = "created_time";

    public static final String UPDATED_TIME = "updated_time";
}