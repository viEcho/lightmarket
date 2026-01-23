package com.market.business.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 钱包信息响应
 *
 * @author echo
 * @date 2026/01/23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "钱包信息响应")
public class WalletVO {

    @Schema(description = "钱包地址", example = "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266")
    private String walletAddress;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户昵称", example = "alice")
    private String nickname;

    @Schema(description = "用户头像", example = "https://example.com/avatar.png")
    private String avatar;

    @Schema(description = "链ID", example = "31337")
    private Integer chainId;

    @Schema(description = "钱包类型", example = "metamask")
    private String walletType;

    @Schema(description = "是否主钱包：1-是，0-否", example = "1")
    private Byte isPrimary;

    @Schema(description = "添加时间", example = "2026-01-23 12:00:00")
    private String createdTime;
}
