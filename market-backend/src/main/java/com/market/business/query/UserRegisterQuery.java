package com.market.business.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * User Login Request DTO
 */
@Data
@Schema(description = "Wallet login request")
public class UserRegisterQuery {

    @NotBlank(message = "Wallet address is required")
    @Schema(description = "钱包地址")
    private String walletAddress;

    @NotNull(message = "Chain ID is required")
    @Schema(description = "链ID")
    private Integer chainId;

    @NotBlank(message = "Signature is required")
    @Schema(description = "钱包签名")
    private String signature;

    @Schema(description = "钱包类型 (metamask, walletconnect等)")
    private String walletType;
}
