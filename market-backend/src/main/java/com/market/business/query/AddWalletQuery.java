package com.market.business.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 添加钱包请求
 *
 * @author echo
 * @date 2026/01/23
 */
@Data
@Schema(description = "添加钱包请求")
public class AddWalletQuery {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @NotBlank(message = "钱包地址不能为空")
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "钱包地址格式不正确")
    @Schema(description = "钱包地址", example = "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266")
    private String walletAddress;

    @NotNull(message = "链ID不能为空")
    @Schema(description = "链ID（1=Ethereum，137=Polygon，42161=Arbitrum等）", example = "1")
    private Integer chainId;

    @Schema(description = "钱包类型（metamask/walletconnect等）", example = "metamask")
    private String walletType;

    @NotBlank(message = "签名不能为空")
    @Schema(description = "钱包签名（用于验证钱包所有权）", example = "0x...")
    private String signature;
}
