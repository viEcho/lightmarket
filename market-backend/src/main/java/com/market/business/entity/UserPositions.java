package com.market.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 预测市场用户仓位表（链上映射）
 *
 * @author viEcho
 * @date 2026/01/23
 */
@Data
@TableName("user_positions")
public class UserPositions {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID（系统内部）
     */
    private Long userId;

    /**
     * 市场ID
     */
    private String marketId;

    /**
     * YES仓位，份额
     */
    private BigDecimal yesBalance;

    /**
     * NO仓位，份额
     */
    private BigDecimal noBalance;

    /**
     * 锁定YES,token份额
     */
    private BigDecimal lockedYes;

    /**
     * 锁定NO,token份额
     */
    private BigDecimal lockedNo;

    /**
     * 可结算YES份额
     */
    private BigDecimal claimableYes;

    /**
     * 可结算NO份额
     */
    private BigDecimal claimableNo;

    /**
     * 最近同步区块高度
     */
    private Long lastSyncBlock;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    public static final String ID = "id";

    public static final String USER_ID = "user_id";

    public static final String MARKET_ID = "market_id";

    public static final String YES_BALANCE = "yes_balance";

    public static final String NO_BALANCE = "no_balance";

    public static final String LOCKED_YES = "locked_yes";

    public static final String LOCKED_NO = "locked_no";

    public static final String CLAIMABLE_YES = "claimable_yes";

    public static final String CLAIMABLE_NO = "claimable_no";

    public static final String LAST_SYNC_BLOCK = "last_sync_block";

    public static final String CREATED_TIME = "created_time";

    public static final String UPDATED_TIME = "updated_time";
}