-- Database schema for LightMarket (MySQL 8)
-- 要求：不额外创建索引，仅保留主键/唯一约束；钱包地址为唯一标识
-- 字符集/排序根据实际需要设置，这里示例 utf8mb4

CREATE TABLE IF NOT EXISTS users (
  wallet_address   varchar(128) PRIMARY KEY,
  display_name     varchar(128),
  created_at       datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS admins (
  wallet_address   varchar(128) PRIMARY KEY,
  role             varchar(32) DEFAULT 'reviewer',
  created_at       datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS markets (
  market_id            varchar(128) PRIMARY KEY,
  title                text NOT NULL,
  question             text,
  description          text NOT NULL,
  category             varchar(64) NOT NULL,
  status               varchar(32) NOT NULL,
  stage                varchar(32),
  current_probability  decimal(5,4),
  yes_price            decimal(5,4),
  no_price             decimal(5,4),
  volume               decimal(38,18) DEFAULT 0,
  liquidity            decimal(38,18) DEFAULT 0,
  liquidity_providers  int,
  end_time             datetime NOT NULL,
  image_url            text,
  tags                 json,
  creator_address      varchar(128) NOT NULL,
  create_time          datetime NOT NULL,
  resolution_source    text NOT NULL,
  stake_amount         decimal(38,18) NOT NULL,
  resolution_result    varchar(16),
  resolved_at          datetime,
  creator_reward_rate  decimal(5,4),
  selected_ai_models   json,
  tx_hash_create       varchar(128) NOT NULL,
  block_height_create  bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS market_tags (
  market_id   varchar(128) NOT NULL,
  tag         varchar(64) NOT NULL,
  PRIMARY KEY (market_id, tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS market_ai_models (
  market_id   varchar(128) NOT NULL,
  ai_code     varchar(64) NOT NULL,
  PRIMARY KEY (market_id, ai_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS stakes (
  stake_id        varchar(160) PRIMARY KEY,
  market_id       varchar(128) NOT NULL,
  staker_address  varchar(128) NOT NULL,
  amount          decimal(38,18) NOT NULL,
  tx_hash         varchar(128) NOT NULL,
  block_height    bigint NOT NULL,
  block_time      datetime NOT NULL,
  event_type      varchar(32) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS trades (
  trade_id        varchar(160) PRIMARY KEY,
  market_id       varchar(128) NOT NULL,
  trader_address  varchar(128) NOT NULL,
  side            varchar(8) NOT NULL,
  price           decimal(20,10) NOT NULL,
  amount          decimal(38,18) NOT NULL,
  total           decimal(38,18) NOT NULL,
  tx_hash         varchar(128) NOT NULL,
  block_height    bigint NOT NULL,
  block_time      datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS orderbook_levels (
  snapshot_id   bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  market_id     varchar(128) NOT NULL,
  side          varchar(8) NOT NULL,
  price         decimal(20,10) NOT NULL,
  amount        decimal(38,18) NOT NULL,
  depth_percent decimal(5,2),
  taken_at      datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS price_candles (
  market_id     varchar(128) NOT NULL,
  interval_name varchar(8) NOT NULL,
  bucket_start  datetime NOT NULL,
  open          decimal(20,10) NOT NULL,
  high          decimal(20,10) NOT NULL,
  low           decimal(20,10) NOT NULL,
  close         decimal(20,10) NOT NULL,
  volume        decimal(38,18) NOT NULL,
  tx_count      int NOT NULL,
  PRIMARY KEY (market_id, interval_name, bucket_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reviews (
  review_id         bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  market_id         varchar(128) NOT NULL,
  reviewer_address  varchar(128) NOT NULL,
  stage             varchar(32) NOT NULL,
  decision          varchar(16) NOT NULL,
  note              text,
  decision_time     datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS settlements (
  market_id         varchar(128) PRIMARY KEY,
  outcome           varchar(8) NOT NULL,
  resolver_address  varchar(128) NOT NULL,
  resolved_at       datetime NOT NULL,
  tx_hash           varchar(128) NOT NULL,
  block_height      bigint NOT NULL,
  resolution_note   text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS rewards (
  reward_id       varchar(160) PRIMARY KEY,
  market_id       varchar(128) NOT NULL,
  wallet_address  varchar(128) NOT NULL,
  amount          decimal(38,18) NOT NULL,
  status          varchar(16) NOT NULL,
  settled_at      datetime NOT NULL,
  claimed_at      datetime,
  tx_hash_settle  varchar(128),
  tx_hash_claim   varchar(128)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS activity_feed (
  activity_id     bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  market_id       varchar(128) NOT NULL,
  wallet_address  varchar(128) NOT NULL,
  type            varchar(16) NOT NULL,
  price           decimal(20,10),
  amount          decimal(38,18),
  position        varchar(8),
  created_at      datetime NOT NULL,
  tx_hash         varchar(128)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS leaderboard_stats (
  wallet_address   varchar(128) PRIMARY KEY,
  profit           decimal(38,18),
  win_rate         decimal(5,2),
  trades           int,
  pnl_updated_at   datetime
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS auth_nonces (
  wallet_address  varchar(128) PRIMARY KEY,
  nonce           varchar(128) NOT NULL,
  issued_at       datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 示例数据插入（按需调整）
-- ------------------------------------------------------------

-- 用户与管理员
INSERT INTO users (wallet_address, display_name) VALUES
  ('0x1234...5678', 'CryptoWhale'),
  ('0x9876...4321', 'DiamondHands')
ON DUPLICATE KEY UPDATE display_name = VALUES(display_name);

INSERT INTO admins (wallet_address, role) VALUES
  ('0xaabb...ccdd', 'reviewer')
ON DUPLICATE KEY UPDATE role = VALUES(role);

-- 市场
INSERT INTO markets (
  market_id, title, question, description, category, status, stage,
  current_probability, yes_price, no_price, volume, liquidity, liquidity_providers,
  end_time, image_url, tags, creator_address, create_time,
  resolution_source, stake_amount, resolution_result, resolved_at,
  creator_reward_rate, selected_ai_models, tx_hash_create, block_height_create
) VALUES (
  'market-1',
  'Will Bitcoin exceed $150,000 by end of 2025?',
  'Will Bitcoin exceed $150,000 by end of 2025?',
  'Resolves YES if BTC trades above $150,000 on any major exchange before Dec 31, 2025.',
  'crypto',
  'approved',
  'published',
  0.65, 0.65, 0.35,
  1250000, 500000, 12,
  '2025-12-31 23:59:59',
  NULL,
  JSON_ARRAY('bitcoin','crypto','defi'),
  '0x1234...5678',
  NOW(),
  'Based on Bitcoin price at CoinMarketCap on Dec 31, 2025 23:59:59 UTC',
  10000,
  NULL,
  NULL,
  0.10,
  JSON_ARRAY('chatgpt','claude','gemini'),
  '0xtxhash_create_1',
  12345678
)
ON DUPLICATE KEY UPDATE title = VALUES(title);

-- 标签与AI模型
INSERT INTO market_tags (market_id, tag) VALUES
  ('market-1', 'bitcoin'),
  ('market-1', 'crypto'),
  ('market-1', 'defi')
ON DUPLICATE KEY UPDATE tag = VALUES(tag);

INSERT INTO market_ai_models (market_id, ai_code) VALUES
  ('market-1', 'chatgpt'),
  ('market-1', 'claude'),
  ('market-1', 'gemini')
ON DUPLICATE KEY UPDATE ai_code = VALUES(ai_code);

-- 质押记录
INSERT INTO stakes (
  stake_id, market_id, staker_address, amount,
  tx_hash, block_height, block_time, event_type
) VALUES (
  'stake-1', 'market-1', '0x1234...5678', 10000,
  '0xtxhash_stake_1', 12345679, NOW(), 'add'
)
ON DUPLICATE KEY UPDATE amount = VALUES(amount);

-- 交易记录
INSERT INTO trades (
  trade_id, market_id, trader_address, side, price, amount, total,
  tx_hash, block_height, block_time
) VALUES (
  'trade-1', 'market-1', '0x9876...4321', 'buy', 0.65, 1000, 650,
  '0xtxhash_trade_1', 12345680, NOW()
)
ON DUPLICATE KEY UPDATE total = VALUES(total);

-- K线示例
INSERT INTO price_candles (
  market_id, interval_name, bucket_start,
  open, high, low, close, volume, tx_count
) VALUES (
  'market-1', '1D', '2025-01-01 00:00:00',
  0.64, 0.66, 0.63, 0.65, 50000, 120
)
ON DUPLICATE KEY UPDATE close = VALUES(close);

-- 审核记录
INSERT INTO reviews (
  market_id, reviewer_address, stage, decision, note, decision_time
) VALUES (
  'market-1', '0xaabb...ccdd', 'final-review', 'approve', 'Looks good', NOW()
);

-- 结算（示例未结算，可注释）
-- INSERT INTO settlements (
--   market_id, outcome, resolver_address, resolved_at, tx_hash, block_height, resolution_note
-- ) VALUES (
--   'market-1', 'yes', '0xaabb...ccdd', NOW(), '0xtxhash_settle_1', 12349999, 'Resolved YES'
-- );

-- 奖励记录
INSERT INTO rewards (
  reward_id, market_id, wallet_address, amount, status, settled_at, claimed_at, tx_hash_settle, tx_hash_claim
) VALUES (
  'reward-1', 'market-1', '0x1234...5678', 125.50, 'pending', NOW(), NULL, NULL, NULL
)
ON DUPLICATE KEY UPDATE amount = VALUES(amount);

-- 活动流
INSERT INTO activity_feed (
  market_id, wallet_address, type, price, amount, position, created_at, tx_hash
) VALUES (
  'market-1', '0x9876...4321', 'buy', 0.65, 1000, 'yes', NOW(), '0xtxhash_trade_1'
);

-- 排行榜
INSERT INTO leaderboard_stats (
  wallet_address, profit, win_rate, trades, pnl_updated_at
) VALUES (
  '0x9876...4321', 528000, 78.00, 1243, NOW()
)
ON DUPLICATE KEY UPDATE profit = VALUES(profit);

-- 登录 nonce
INSERT INTO auth_nonces (wallet_address, nonce, issued_at) VALUES
  ('0xaabb...ccdd', 'nonce-123', NOW())
ON DUPLICATE KEY UPDATE nonce = VALUES(nonce);
