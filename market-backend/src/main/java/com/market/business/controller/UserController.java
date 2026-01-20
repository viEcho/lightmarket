package com.market.business.controller;

import com.market.business.query.UserCheckQuery;
import com.market.business.query.UserRegisterQuery;
import com.market.business.global.Result;
import com.market.business.service.AuthService;
import com.market.business.service.UserService;
import com.market.business.vo.LoginVO;
import com.market.business.vo.NonceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * User Controller
 */
@Tag(name = "User Authentication", description = "用户钱包登录相关接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    UserService userService;

    @Resource
    AuthService authService;

    /**
     * Get nonce for wallet signature
     */
    @Operation(summary = "获取Nonce", description = "前端调用此接口获取随机Nonce，用于钱包签名验证")
    @PostMapping("/nonce")
    public Result<NonceVO> getNonce(@Validated @RequestBody UserCheckQuery request) {
        NonceVO response = authService.generateNonce(
                request.getWalletAddress(),
                request.getChainId()
        );
        return Result.success(response);
    }

    /**
     * Verify signature and login
     */
    @Operation(summary = "钱包登录", description = "验证钱包签名并完成用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody UserRegisterQuery request) {
        LoginVO response = authService.verifyAndLogin(request);
        return Result.success(response);
    }
}

