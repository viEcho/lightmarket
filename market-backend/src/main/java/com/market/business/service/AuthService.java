package com.market.business.service;

import com.market.business.query.UserRegisterQuery;
import com.market.business.vo.LoginVO;
import com.market.business.vo.NonceVO;

/**
 * Auth Service Interface
 */
public interface AuthService {

    /**
     * Generate nonce for wallet signature
     *
     * @param walletAddress 钱包地址
     * @param chainId 链ID
     * @return Nonce响应
     */
    NonceVO generateNonce(String walletAddress, Integer chainId);

    /**
     * Verify signature and login user
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginVO verifyAndLogin(UserRegisterQuery request);
}
