package com.market.business.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 市场开始发布请求参数
 *
 * @author echo
 * @date 2026/01/23
 */
@Data
@Schema(description = "市场开始发布请求参数")
public class MarketOpeningQuery {

    @Schema(description = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "市场ID", required = true)
    @NotBlank(message = "市场ID不能为空")
    private String marketId;
}
