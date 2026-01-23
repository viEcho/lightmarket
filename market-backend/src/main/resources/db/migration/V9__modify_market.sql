ALTER TABLE market ADD COLUMN on_chain_market_id VARCHAR(66) COMMENT '链上市场ID' after market_address;
CREATE INDEX idx_onChainMarketId ON market(on_chain_market_id);
ALTER TABLE `market` MODIFY COLUMN market_status tinyint unsigned NOT NULL
    COMMENT '市场状态：0-待审核，1-已拒绝，2-初审通过，3-终审通过，4-deploying发布中，5-已发布上链open，6-已关闭，7-裁决中，8-挑战中，9-已终裁，10-结算中，99-已结算';
ALTER TABLE `market` DROP COLUMN category;