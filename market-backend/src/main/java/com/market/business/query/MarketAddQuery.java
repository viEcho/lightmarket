package com.market.business.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建市场请求 DTO
 * 字段名与数据库表字段保持一致
 *
 * @author echo
 * @date 2026/01/21
 */
@Data
@Schema(description = "创建市场请求参数")
public class MarketAddQuery {

    @NotBlank(message = "Title is required")
    @Size(max = 128, message = "Title must not exceed 128 characters")
    @Schema(description = "市场标题", example = "Will Bitcoin exceed $100,000 by December 31, 2024?")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 512, message = "Description must not exceed 512 characters")
    @Schema(description = "市场描述")
    private String description;

    @NotNull(message = "Category is required")
    @Schema(description = "市场分类（数字code）", example = "1")
    private Integer category;

    @NotBlank(message = "Close time is required")
    @Schema(description = "市场截止时间（ISO 8601格式）", example = "2024-12-31T23:59:59")
    private String closeTime;

    @NotNull(message = "Base liquidity is required")
    @DecimalMin(value = "100", message = "Minimum base liquidity is 100")
    @Schema(description = "基础流动性金额（IMKT）", example = "1000")
    private BigDecimal baseLiquidity;

    @NotBlank(message = "Oracle source is required")
    @Size(max = 128, message = "Oracle source must not exceed 128 characters")
    @Schema(description = "预言机来源/结算方法", example = "Based on Bitcoin price at CoinMarketCap on December 31, 2024")
    private String oracleSource;

    @NotBlank(message = "AI model is required")
    @Schema(description = "AI模型（逗号分隔的数字code）", example = "1,2,3")
    private String aiModel;

    @NotBlank(message = "Tags is required")
    @Schema(description = "标签（逗号分隔的数字code）", example = "1,5")
    private String tags;
}
