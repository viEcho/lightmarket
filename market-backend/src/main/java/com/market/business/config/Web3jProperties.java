package com.market.business.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Web3j 配置属性
 *
 * @author echo
 * @date 2026/01/23
 */
@Data
@Component
@ConfigurationProperties(prefix = "web3j")
public class Web3jProperties {

    /**
     * RPC节点地址
     */
    private String rpcUrl;

    /**
     * 网络ID
     */
    private Integer networkId;

    /**
     * 市场工厂合约地址
     */
    private String marketFactoryAddress;

    /**
     * 私钥 (仅用于开发环境)
     */
    private String privateKey;
}
