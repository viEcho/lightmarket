package com.market.business.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Market 分页查询参数
 *
 * @author echo
 * @date 2026/01/21
 */
@Data
@Schema(description = "Market分页查询参数")
public class MarketPageQuery {

    @Schema(description = "用户ID（查询我的市场时必填）")
    private Long userId;

    @Schema(description = "页码，从1开始", example = "1")
    private Integer num = 1;

    @Schema(description = "每页大小", example = "6")
    private Integer size = 6;

    @Schema(description = "市场标签：1-crypto, 2-technology, 3-politics, 4-sports, 5-finance, 6-entertainment, 7-other")
    @JsonProperty("tagCode")
    private String tagCode;

    @Schema(description = "市场状态：0-待审核，1-已拒绝，2-初审通过，3-终审通过，4-deploying发布中，5-已发布上链open，6-已关闭，7-裁决中，8-挑战中，9-已终裁，10-结算中，99-已结算")
    private Integer marketStatus = 5;

    @Schema(description = "搜索关键词")
    private String keyword;
}
