-- 订单表（链下订单簿核心）
CREATE TABLE `orders`
(
    `id`                 bigint unsigned PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID-订单id',
    `parent_id`          bigint unsigned COMMENT '父单id,扩展字段用户可拆单设计',
    `client_order_id`    varchar(64) COMMENT '前端幂等订单ID',
    `market_id`          varchar(64)    NOT NULL COMMENT '市场ID（链下）',
    `wallet_address`     varchar(128)   NOT NULL COMMENT '用户钱包地址',
    `token_type`         tinyint        NOT NULL COMMENT '预测方向：0-NO，1-YES',
    `side`               tinyint        NOT NULL COMMENT '订单类型：1-买入，-1-卖出',
    `order_type`         tinyint        NOT NULL COMMENT '订单类型：0-市价单，1-限价单',
    `price`              decimal(10, 4) NOT NULL COMMENT '委托价格',
    `amount`             decimal(20, 6) NOT NULL COMMENT '委托数量',
    `filled_amount`      decimal(20, 6)          DEFAULT 0 COMMENT '已成交数量',
    `remaining_amount`   decimal(20, 6) NOT NULL COMMENT '剩余未成交数量',
    `locked_buy_amount`  decimal(20, 6) NOT NULL default 0 COMMENT '买单，冻结USDC金额',
    `locked_sell_amount` decimal(20, 6) NOT NULL default 0 COMMENT '卖单，冻结token数量；某些token可能为小数',
    `status`             tinyint        NOT NULL COMMENT '订单状态：1-OPEN,2-FILLED,3-CANCELLED,4-EXPIRE',
    `chain_tx_hash`      varchar(128) COMMENT '链上结算交易hash',
    `tx_status`          tinyint        NOT NULL default 0 COMMENT '链上结算状态：0-PENDING / 1-SUCCESS / -1-FAILED',
    `expire_time`        timestamp      NULL COMMENT '订单过期时间，限价单',
    `cancelled_time`     timestamp      NULL COMMENT '订单取消时间',
    `created_time`       timestamp               DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`       timestamp               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '预测市场订单簿表（链下撮合核心）';

CREATE TABLE `trades`
(
    `id`              bigint unsigned PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `market_id`       varchar(64)    NOT NULL COMMENT '市场ID',
    `buy_user_id`     bigint         NOT NULL COMMENT '买方用户ID',
    `sell_user_id`    bigint         NOT NULL COMMENT '卖方用户ID',
    `buy_order_id`    bigint         NOT NULL COMMENT '买方订单ID',
    `sell_order_id`   bigint         NOT NULL COMMENT '卖方订单ID',
    `buyer_address`   varchar(128)   NOT NULL COMMENT '买方钱包地址',
    `seller_address`  varchar(128)   NOT NULL COMMENT '卖方钱包地址',
    `token_type`      tinyint        NOT NULL COMMENT '预测方向：0-NO，1-YES',
    `amount`          decimal(20, 6) NOT NULL COMMENT '成交的预测合约数量（token 数量，买卖双方一致）',
    `price`           decimal(10, 4) NOT NULL COMMENT '预测合约单价，单位：USDC / token',
    `created_time`    timestamp      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`       timestamp   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '预测市场成交表（撮合双方）';

CREATE TABLE `user_trades`
(
    `id`             bigint unsigned PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `user_id`        bigint        NOT NULL COMMENT '用户id',
    `trade_id`       bigint        NOT NULL COMMENT '撮合成交ID',
    `order_id`       bigint        NOT NULL COMMENT '用户订单ID',
    `market_id`      varchar(64)   NOT NULL COMMENT '市场ID',
    `wallet_address` varchar(128)  NOT NULL COMMENT '用户钱包地址',
    `token_type`     tinyint       NOT NULL COMMENT '预测方向：0-NO，1-YES',
    `side`           tinyint       NOT NULL COMMENT '1-买入，-1-卖出',
    `price`          decimal(10,4) NOT NULL COMMENT '成交价格，单位：USDC/token',
    `amount`         decimal(20,6) NOT NULL COMMENT '成交数量，成交的预测合约数量（token 数量，买卖双方一致）',
    `trade_amount`   decimal(20,6) NOT NULL COMMENT '成交token数量',
    `fee_amount`     decimal(20,6) NOT NULL DEFAULT 0 COMMENT '手续费数量',
    `tx_status`       tinyint        NOT NULL default 0 COMMENT '链上结算状态：0-PENDING / 1-SUCCESS / -1-FAILED',
    `tx_hash`         varchar(128) COMMENT '链上结算交易hash',
    `fee_currency`   tinyint       NOT NULL DEFAULT 1 COMMENT '手续费币种：1-USDC（预留扩展）',
    `created_time`   timestamp     DEFAULT CURRENT_TIMESTAMP COMMENT '成交时间',
    `updated_time`       timestamp   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        INDEX `idx_userId` (`user_id`),
    INDEX `idx_orderId` (`order_id`),
    INDEX `idx_tradeId` (`trade_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT ='用户成交记录表（单边视角）';

-- 用户在预测市场中的仓位
CREATE TABLE `user_positions`
(
    `id`              bigint unsigned PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `user_id`          bigint        NOT NULL COMMENT '用户ID（系统内部）',
    `market_id`        varchar(64)   NOT NULL COMMENT '市场ID',
    `yes_balance`      decimal(20,6) DEFAULT 0 COMMENT 'YES仓位，份额',
    `no_balance`       decimal(20,6) DEFAULT 0 COMMENT 'NO仓位，份额',
    `locked_yes`       decimal(20,6) DEFAULT 0 COMMENT '锁定YES,token份额',
    `locked_no`        decimal(20,6) DEFAULT 0 COMMENT '锁定NO,token份额',
    `claimable_yes`    decimal(20,6) DEFAULT 0 COMMENT '可结算YES份额',
    `claimable_no`     decimal(20,6) DEFAULT 0 COMMENT '可结算NO份额',
    `last_sync_block`  bigint COMMENT '最近同步区块高度',
    `created_time`     timestamp     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`     timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_market` (`user_id`, `market_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT ='预测市场用户仓位表（链上映射）';
