package com.market.business.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 市场部署中请求参数
 *
 * @author echo
 * @date 2026/01/23
 */
@Data
@Schema(description = "市场部署中请求参数")
public class MarketDeployingQuery {

    @Schema(description = "市场ID", required = true)
    @NotBlank(message = "市场ID不能为空")
    private String marketId;

    @Schema(description = "交易哈希", required = true)
    @NotBlank(message = "交易哈希不能为空")
    private String txHash;

    @Schema(description = "链上市场ID", required = true)
    @NotBlank(message = "链上市场ID不能为空")
    private String onChainMarketId;
}
