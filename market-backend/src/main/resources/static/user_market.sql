-- User_Market 表 Mock 数据
-- 用户ID为1，参与部分市场的数据
-- total_pay 为用户在该市场的累计买入金额

INSERT INTO user_market (
    user_id, market_id, total_pay, created_time, updated_time
) VALUES
-- 用户1参与了比特币市场，投入较多
(1, 'mkt_btc_150k_2025', 5000.00, NOW(), NOW()),

-- 用户1参与了以太坊市场，投入中等
(1, 'mkt_eth_10k_2025', 3200.50, NOW(), NOW()),

-- 用户1参与了AI图灵测试市场，投入较多
(1, 'mkt_ai_turing_2026', 4500.00, NOW(), NOW()),

-- 用户1参与了特斯拉市场，投入较少
(1, 'mkt_tesla_2m_2025', 1800.00, NOW(), NOW()),

-- 用户1参与了苹果股票市场，投入较多
(1, 'mkt_aapl_250_2025', 6200.00, NOW(), NOW()),

-- 用户1参与了美国大选市场，投入中等
(1, 'mkt_us_election_2028', 2800.00, NOW(), NOW()),

-- 用户1参与了SpaceX火星计划市场，投入较少
(1, 'mkt_spacex_mars_2030', 1200.00, NOW(), NOW());
