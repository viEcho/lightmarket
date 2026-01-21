-- Market 表 Mock 数据
-- 用于测试首页分页接口
-- ai_model 存储格式：AiEnum 的 code 值逗号拼接（如 "1,2" 代表 ChatGpt,Claude）
-- tags 存储格式：TagEnum 的 code 值逗号拼接（如 "1,2,3" 代表 Crypto,Technology,Politics）

INSERT INTO market (
    market_id, user_id, title, description, category, close_time, resolve_time,
    oracle_source, resolution_method, ai_model, tags, market_status,
    base_liquidity, yes_price, no_price, resolved_outcome, total_volume,
    risk_status, weight, chain_id, creator, created_time, updated_time
) VALUES
-- Market 1: Bitcoin (Crypto, Tags: bitcoin,crypto,defi)
('mkt_btc_150k_2025', 1, 'Will Bitcoin exceed $150,000 by end of 2025?',
 'This market resolves to YES if Bitcoin (BTC) trades above $150,000 on any major exchange before December 31, 2025.',
 1, '2025-12-31 23:59:59', '2026-01-01 23:59:59',
 'CoinMarketCap', 0, '1', '1,7,8', 3,
 500000.00, 0.65, 0.35, 0, 1250000.00,
 0, 100, 1, 'CryptoWhale', NOW(), NOW()),

-- Market 2: Ethereum (Crypto, Tags: ethereum,eth,crypto)
('mkt_eth_10k_2025', 2, 'Will Ethereum reach $10,000 in 2025?',
 'Resolves YES if ETH trades at or above $10,000 on at least 3 major exchanges in 2025.',
 1, '2025-12-31 23:59:59', '2026-01-01 23:59:59',
 'Binance,Coinbase,Kraken', 0, '2', '1,1', 3,
 350000.00, 0.42, 0.58, 0, 890000.00,
 0, 95, 1, 'EtherMax', NOW(), NOW()),

-- Market 3: AI Turing Test (Technology, Tags: ai,technology,ml)
('mkt_ai_turing_2026', 3, 'Will AI pass the Turing test convincingly by 2026?',
 'Market resolves YES if an AI system achieves >70% success rate in an official Turing test competition.',
 2, '2026-06-30 23:59:59', '2026-07-01 23:59:59',
 'Official Competition Results', 0, '3', '9,2,2', 3,
 200000.00, 0.35, 0.65, 0, 450000.00,
 0, 90, 1, 'TechVisionary', NOW(), NOW()),

-- Market 4: SpaceX Mars (Technology, Tags: spacex,mars,space)
('mkt_spacex_mars_2030', 4, 'Will SpaceX successfully land humans on Mars by 2030?',
 'Resolves YES if SpaceX successfully lands at least one human on Mars and returns them safely to Earth.',
 2, '2030-12-31 23:59:59', '2031-01-01 23:59:59',
 'SpaceX Official', 0, '2', '2,7,7', 3,
 300000.00, 0.28, 0.72, 0, 670000.00,
 0, 85, 1, 'SpaceEnthusiast', NOW(), NOW()),

-- Market 5: US Election 2028 (Politics, Tags: politics,election,usa)
('mkt_us_election_2028', 5, 'Will a Democrat win the 2028 US Presidential Election?',
 'Resolves to the winning party of the 2028 US Presidential Election.',
 3, '2028-11-05 23:59:59', '2028-11-06 23:59:59',
 'Federal Election Commission', 0, '1', '3,3,7', 3,
 1000000.00, 0.52, 0.48, 0, 2100000.00,
 0, 80, 1, 'PoliticalWatcher', NOW(), NOW()),

-- Market 6: Tesla Deliveries (Finance, Tags: tesla,ev,automotive)
('mkt_tesla_2m_2025', 6, 'Will Tesla deliver more than 2 million vehicles in 2025?',
 'Resolves YES if Tesla''s official annual report shows >2M vehicle deliveries for fiscal year 2025.',
 5, '2026-01-31 23:59:59', '2026-02-01 23:59:59',
 'Tesla Form 10-K', 0, '3', '5,5,7', 3,
 150000.00, 0.58, 0.42, 0, 320000.00,
 0, 75, 1, 'AutoAnalyst', NOW(), NOW()),

-- Market 7: China GDP Growth (Finance, Tags: china,economy,gdp)
('mkt_china_gdp_2025', 7, 'Will China''s GDP growth exceed 5% in 2025?',
 'Resolves YES if China''s official GDP growth rate for 2025 exceeds 5%.',
 5, '2026-01-31 23:59:59', '2026-02-28 23:59:59',
 'National Bureau of Statistics', 0, '2', '1,5,7', 3,
 250000.00, 0.45, 0.55, 0, 580000.00,
 0, 70, 1, 'EconoWatcher', NOW(), NOW()),

-- Market 8: World Cup 2026 (Sports, Tags: football,worldcup,brazil)
('mkt_worldcup_2026', 8, 'Will Brazil win the 2026 FIFA World Cup?',
 'Resolves YES if Brazil wins the 2026 FIFA World Cup final.',
 6, '2026-07-20 23:59:59', '2026-07-21 23:59:59',
 'FIFA Official', 0, '1', '2,6,7', 3,
 400000.00, 0.38, 0.62, 0, 920000.00,
 0, 65, 1, 'SportsFan', NOW(), NOW()),

-- Market 9: Apple Stock Price (Finance, Tags: apple,stock,tech)
('mkt_aapl_250_2025', 9, 'Will Apple stock reach $250 by end of 2025?',
 'Resolves YES if AAPL trades at or above $250 on NASDAQ.',
 5, '2025-12-31 23:59:59', '2026-01-01 23:59:59',
 'NASDAQ', 0, '3', '2,5,2', 3,
 180000.00, 0.55, 0.45, 0, 410000.00,
 0, 60, 1, 'StockTrader', NOW(), NOW()),

-- Market 10: COVID-19 Vaccine (Other, Tags: health,covid,vaccine)
('mkt_covid_vaccine_2026', 10, 'Will there be a new COVID-19 variant requiring updated vaccines in 2026?',
 'Resolves YES if WHO announces a new COVID-19 variant requiring updated vaccine formulation.',
 7, '2026-12-31 23:59:59', '2027-01-31 23:59:59',
 'WHO Official', 0, '2', '1,2,7', 3,
 220000.00, 0.62, 0.38, 0, 730000.00,
 0, 55, 1, 'HealthExpert', NOW(), NOW());