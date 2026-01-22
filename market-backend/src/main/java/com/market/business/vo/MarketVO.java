package com.market.business.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.market.business.enums.AiEnum;
import com.market.business.enums.TagEnum;
import com.market.business.utils.EnumUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Market VO（返回给前端）
 *
 * @author echo
 * @date 2026/01/21
 */
@Data
@Schema(description = "Market VO")
public class MarketVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "市场唯一标识")
    private String marketId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "市场标题")
    private String title;

    @Schema(description = "市场描述")
    private String description;

    @Schema(description = "市场截止时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date closeTime;

    @Schema(description = "最早可结算时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date resolveTime;

    @Schema(description = "预言机来源")
    private String oracleSource;

    @Schema(description = "结算方式：0-AI裁决 1-人工")
    private Byte resolutionMethod;

    @Schema(description = "AI裁决模型列表")
    private List<Option<Integer>> aiModels;

    @Schema(description = "市场标签列表")
    private List<Option<Integer>> tags;

    @Schema(description = "市场状态：0-待审核，1-已拒绝，2-审核通过，3-已发布，4-已关闭，5-裁决中，6-挑战中，7-已结算")
    private Byte marketStatus;

    @Schema(description = "基础流动性（USDC）")
    private BigDecimal baseLiquidity;

    @Schema(description = "YES 当前价格（概率）")
    private BigDecimal yesPrice;

    @Schema(description = "NO 当前价格（概率）")
    private BigDecimal noPrice;

    @Schema(description = "最终结果：0-未结算 1-YES 2-NO 3-Invalid")
    private Byte resolvedOutcome;

    @Schema(description = "累计成交量（USDC）")
    private BigDecimal totalVolume;

    @Schema(description = "风控状态：0-正常 1-冻结 2-调查中")
    private Byte riskStatus;

    @Schema(description = "运营排序权重")
    private Integer weight;

    @Schema(description = "链ID")
    private Integer chainId;

    @Schema(description = "创建人")
    private String creator;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedTime;

    /**
     * 从 Entity 转换为 VO
     */
    public static MarketVO fromEntity(com.market.business.entity.Market entity) {
        MarketVO vo = new MarketVO();
        BeanUtils.copyProperties(entity, vo);

        // 转换 AI 模型字符串为枚举列表
        if (entity.getAiModel() != null) {
            vo.setAiModels(EnumUtil.convertCodesToOptions(entity.getAiModel(), AiEnum.class));
        }

        // 转换标签字符串为枚举列表
        if (entity.getTags() != null) {
            vo.setTags(EnumUtil.convertCodesToOptions(entity.getTags(), TagEnum.class));
        }
        return vo;
    }
}
