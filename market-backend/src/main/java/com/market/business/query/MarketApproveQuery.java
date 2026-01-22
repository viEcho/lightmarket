package com.market.business.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 市场审批请求 DTO
 *
 * @author echo
 * @date 2026/01/22
 */
@Data
@Schema(description = "市场审批请求参数")
public class MarketApproveQuery {

    @Schema(description = "市场ID", example = "MKT-xxx")
    private String marketId;

    @Schema(description = "目标状态：1-已拒绝，2-初审通过，3-终审通过", example = "2")
    private Integer status;
}
