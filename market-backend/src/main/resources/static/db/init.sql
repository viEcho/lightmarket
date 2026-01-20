CREATE TABLE `user` (
    `id` bigint unsigned PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `uid` varchar(64) NOT NULL COMMENT '对外用户唯一标识',
    `nickname` varchar(64) NOT NULL COMMENT '用户昵称',
    `avatar` varchar(256) DEFAULT NULL COMMENT '用户头像',
    `deleted_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
    `created_time` datetime(3) NOT NULL COMMENT '添加时间',
    `update_time` datetime(3) NOT NULL COMMENT '更新时间',
    UNIQUE KEY `uk_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '用户表';

CREATE TABLE user_wallet (
     `id` bigint unsigned PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
     `user_id` bigint NOT NULL,
     `wallet_address` VARCHAR(128) NOT NULL COMMENT '钱包地址',
     `chain_id` INT NOT NULL COMMENT '链ID（1=Ethereum，137=Polygon，42161=Arbitrum等）',
     `wallet_type` VARCHAR(32) DEFAULT NULL COMMENT '钱包类型（metamask / walletconnect 等）',
     `is_primary` TINYINT DEFAULT 1 COMMENT '是否主钱包：1-是，0-否',
     `created_time` datetime(3) NOT NULL COMMENT '添加时间',
     `updated_time` datetime(3) NOT NULL COMMENT '更新时间',
     KEY `idx_user_id` (`user_id`),
     UNIQUE KEY uk_wallet_chain (wallet_address, chain_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '用户钱包表';

CREATE TABLE `user_nonce` (
      `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键id',
      `wallet_address` VARCHAR(128) NOT NULL COMMENT '钱包地址',
      `chain_id` INT NOT NULL COMMENT '链ID',
      `nonce` VARCHAR(64) NOT NULL COMMENT '随机Nonce，用于钱包签名',
      `expired_at` DATETIME NOT NULL COMMENT 'Nonce过期时间',
      `used` TINYINT DEFAULT 0 COMMENT '是否已使用：0-未使用，1-已使用',
      `created_time` datetime(3) NOT NULL COMMENT '创建时间',
      KEY `idx_wallet_chain` (`wallet_address`, `chain_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包签名Nonce表';



