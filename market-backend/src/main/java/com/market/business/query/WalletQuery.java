package com.market.business.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 钱包查询请求
 *
 * @author echo
 * @date 2026/01/23
 */
@Data
@Schema(description = "钱包查询请求")
public class WalletQuery {

    @NotBlank(message = "钱包地址不能为空")
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "钱包地址格式不正确")
    @Schema(description = "钱包地址", example = "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266")
    private String walletAddress;
}
