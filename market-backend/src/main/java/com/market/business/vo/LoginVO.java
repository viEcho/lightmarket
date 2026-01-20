package com.market.business.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Login Response VO
 */
@Data
@Schema(description = "登录响应")
@AllArgsConstructor
public class LoginVO {

    @Schema(description = "JWT访问令牌")
    String accessToken;

    @Schema(description = "用户唯一标识")
    String uid;

    @Schema(description = "用户昵称")
    String nickname;

    @Schema(description = "用户头像")
    String avatar;

    @Schema(description = "钱包地址")
    String walletAddress;
}
