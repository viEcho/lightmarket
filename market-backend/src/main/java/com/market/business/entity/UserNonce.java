package com.market.business.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 钱包签名Nonce表
 *
 * @author viEcho
 * @date 2026/01/20
 */
@Data
@TableName("user_nonce")
public class UserNonce {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 钱包地址
     */
    private String walletAddress;

    /**
     * 链ID
     */
    private Integer chainId;

    /**
     * 随机Nonce，用于钱包签名
     */
    private String nonce;

    /**
     * Nonce过期时间
     */
    private Date expiredAt;

    /**
     * 格式化的过期时间字符串（用于签名验证，避免纳秒精度问题）
     */
    private String formattedExpiredAt;

    /**
     * 是否已使用：0-未使用，1-已使用
     */
    private Byte used;

    /**
     * 创建时间
     */
    private Date createdTime;


    public static final String ID = "id";
    public static final String WALLET_ADDRESS = "wallet_address";
    public static final String CHAIN_ID = "chain_id";
    public static final String NONCE = "nonce";
    public static final String EXPIRED_AT = "expired_at";
    public static final String FORMATTED_EXPIRED_AT = "formatted_Expired_At";
    public static final String USED = "used";
    public static final String CREATED_TIME = "created_time";

}