CREATE TABLE `user_market`
(
    `id`           bigint unsigned PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `user_id`      bigint       NOT NULL COMMENT '用户id',
    `market_id`    VARCHAR(64)      NOT NULL COMMENT '市场唯一标识',
    `total_pay` DECIMAL(18,6)    NOT NULL DEFAULT 0 COMMENT '累计买入',
    `created_time` datetime(3)      NOT NULL COMMENT '添加时间',
    `updated_time`  datetime(3)     NOT NULL COMMENT '更新时间',
    INDEX `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '用户参与市场表';