package com.market.business.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Nonce Response VO
 */
@Data
@Schema(description = "Nonce响应")
@AllArgsConstructor
public class NonceVO {

    @Schema(description = "随机Nonce值，用于签名")
    String nonce;

    @Schema(description = "Nonce过期时间戳")
    Long expiredAt;

    @Schema(description = "签名的消息内容")
    String message;
}
