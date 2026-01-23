ALTER TABLE `market` MODIFY COLUMN market_status tinyint unsigned NOT NULL
    COMMENT '市场状态：0-待审核，1-已拒绝，2-初审通过，3-终审通过，4-deploying发布中，5-已发布上链open，6-已关闭，7-裁决中，8-挑战中，9-已终裁，10-结算中，99-已关闭';
ALTER TABLE `market` ADD COLUMN market_address varchar(126) COMMENT '市场合约地址' after market_id;
