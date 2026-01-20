package com.market.business.entity;

import java.util.Date;
import lombok.Data;

/**
 * 钱包签名Nonce表
 *
 * @author viEcho
 * @date 2026/01/20
 */
@Data
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
     * 是否已使用：0-未使用，1-已使用
     */
    private Byte used;

    /**
     * 创建时间
     */
    private Date createdTime;
}