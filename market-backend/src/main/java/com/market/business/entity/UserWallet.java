package com.market.business.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户钱包表
 *
 * @author viEcho
 * @date 2026/01/20
 */
@Data
@TableName("user_wallet")
public class UserWallet {

    /**
     * 主键ID
     */
    private Long id;

    private Long userId;

    /**
     * 钱包地址
     */
    private String walletAddress;

    /**
     * 链ID（1=Ethereum，137=Polygon，42161=Arbitrum等）
     */
    private Integer chainId;

    /**
     * 钱包类型（metamask / walletconnect 等）
     */
    private String walletType;

    /**
     * 是否主钱包：1-是，0-否
     */
    private Byte isPrimary;

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
    public static final String WALLET_ADDRESS = "wallet_address";
    public static final String CHAIN_ID = "chain_id";
    public static final String WALLET_TYPE = "wallet_type";
    public static final String IS_PRIMARY = "is_primary";
    public static final String CREATED_TIME = "created_time";
    public static final String UPDATED_TIME = "updated_time";
}