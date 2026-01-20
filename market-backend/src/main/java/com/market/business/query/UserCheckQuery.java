package com.market.business.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * User Check Request DTO
 */
@Data
@Schema(description = "Get nonce request")
public class UserCheckQuery {

    @NotBlank(message = "Wallet address is required")
    @Schema(description = "钱包地址", required = true)
    private String walletAddress;

    @NotNull(message = "Chain ID is required")
    @Schema(description = "链ID (1=Ethereum, 137=Polygon, etc.)", required = true)
    private Integer chainId;
}
