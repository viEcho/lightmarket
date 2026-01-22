package com.market.business.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 管理员统计信息 VO
 *
 * @author echo
 * @date 2026/01/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员统计信息")
public class AdminStatisticsVO {

    @Schema(description = "总市场数（状态大于等于开启状态的市场数）", example = "100")
    private Integer totalMarkets;

    @Schema(description = "活跃市场数（市场开启数，状态=4已发布上链open）", example = "50")
    private Integer activeMarkets;

    @Schema(description = "待审核市场数（状态<3且不为拒绝）", example = "10")
    private Integer pendingReview;

    @Schema(description = "总流动性（所有active市场流动性之和）", example = "1000000.00")
    private BigDecimal totalLiquidity;
}
