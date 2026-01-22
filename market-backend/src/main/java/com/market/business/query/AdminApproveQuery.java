package com.market.business.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员审核市场查询参数
 *
 * @author echo
 * @date 2026/01/22
 */
@Data
@Schema(description = "管理员审核市场查询参数")
public class AdminApproveQuery {

    @Schema(description = "市场状态：0-待审核(pre-review)，1-已拒绝，2-审核通过，3-已发布，4-已关闭，5-裁决中，6-挑战中，7-已结算", example = "0")
    private Integer marketStatus;

    @Schema(description = "是否排除已拒绝状态（当传此字段时，查询状态不为审批拒绝的数据）", example = "false")
    private Boolean excludeRejected;

    @Schema(description = "页码，从1开始", example = "1")
    private Integer num = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;
}
