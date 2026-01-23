package com.market.business.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 预测市场表
 *
 * @author viEcho
 * @date 2026/01/21
 */
@Data
@TableName("market")
public class Market {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 市场唯一标识（对外ID）
     */
    private String marketId;

    /**
     * 市场地址,右后端监听获得不得用于网络传输
     * 避免mybatis-plus 更新插入此值 只能xml中写sql更新
     */
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String marketAddress;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String onChainMarketId;
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 预测市场标题
     */
    private String title;

    /**
     * 预测市场描述
     */
    private String description;

    /**
     * 市场截止时间
     */
    private Date closeTime;

    /**
     * 最早可结算时间，默认为市场截止时间后24小时
     */
    private Date resolveTime;

    /**
     * 预言机来源（如 UMA / API / 人工）
     */
    private String oracleSource;

    /**
     * 结算方式：0-AI裁决 1-人工
     */
    private Integer resolutionMethod;

    /**
     * AI裁决模型标识（逗号分隔）
     */
    private String aiModel;

    /**
     * 市场标签（逗号分隔）
     */
    private String tags;

    /**
     * 市场状态：市场状态：0-待审核，1-已拒绝，2-初审通过，3-终审通过，4-deploying发布中，5-已发布上链open，6-已关闭，7-裁决中，8-挑战中，9-已终裁，10-结算中，99-已结算
     */
    private Integer marketStatus;

    /**
     * 基础流动性（默认USDC）
     */
    private BigDecimal baseLiquidity;

    /**
     * YES 当前价格（概率）
     */
    private BigDecimal yesPrice;

    /**
     * NO 当前价格（概率）
     */
    private BigDecimal noPrice;

    /**
     * 最终结果：0-未结算 1-YES 2-NO 3-Invalid
     */
    private Integer resolvedOutcome;

    /**
     * 累计成交量（USDC）
     */
    private BigDecimal totalVolume;

    /**
     * 风控状态：0-正常 1-冻结 2-调查中
     */
    private Integer riskStatus;

    /**
     * 运营排序权重
     */
    private Integer weight;

    /**
     * 链ID（如 1-Ethereum 137-Polygon）
     */
    private Integer chainId;

    /**
     * 创建市场的链上交易哈希
     */
    private String txHash;

    /**
     * 市场创建人（用户ID或链上地址）
     */
    private String creator;

    /** 审批tips */
    private String approve_tips;

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

    public static final String USER_ID = "user_id";

    public static final String TITLE = "title";

    public static final String DESCRIPTION = "description";

    public static final String CLOSE_TIME = "close_time";

    public static final String RESOLVE_TIME = "resolve_time";

    public static final String ORACLE_SOURCE = "oracle_source";

    public static final String RESOLUTION_METHOD = "resolution_method";

    public static final String AI_MODEL = "ai_model";

    public static final String TAGS = "tags";

    public static final String MARKET_STATUS = "market_status";

    public static final String BASE_LIQUIDITY = "base_liquidity";

    public static final String YES_PRICE = "yes_price";

    public static final String NO_PRICE = "no_price";

    public static final String RESOLVED_OUTCOME = "resolved_outcome";

    public static final String TOTAL_VOLUME = "total_volume";

    public static final String RISK_STATUS = "risk_status";

    public static final String WEIGHT = "weight";

    public static final String CHAIN_ID = "chain_id";

    public static final String TX_HASH = "tx_hash";

    public static final String CREATOR = "creator";

    public static final String APPROVE_TIPS = "approve_tips";

    public static final String CREATED_TIME = "created_time";

    public static final String UPDATED_TIME = "updated_time";
}