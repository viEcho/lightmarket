ALTER TABLE `market` MODIFY COLUMN market_status tinyint unsigned NOT NULL COMMENT '市场状态：0-待审核，1-已拒绝，2-初审通过，3-终审通过，4-已发布上链open，5-已关闭，6-裁决中，7-挑战中，8-已结算';
ALTER TABLE `market` ADD COLUMN approve_tips varchar(256)  COMMENT '审核意见' after creator;
