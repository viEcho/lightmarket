-- LightMarket database schema (PostgreSQL)
-- Tables: admins, markets, market_tags, market_ai_models, transactions, rewards,
--         orders, price_history, trader_stats

-- Drop existing tables for idempotent local setup
DROP TABLE IF EXISTS trader_stats CASCADE;
DROP TABLE IF EXISTS price_history CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS rewards CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS market_ai_models CASCADE;
DROP TABLE IF EXISTS market_tags CASCADE;
DROP TABLE IF EXISTS markets CASCADE;
DROP TABLE IF EXISTS admins CASCADE;

-- 1) admins
CREATE TABLE admins (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(64) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2) markets
CREATE TABLE markets (
    id                   BIGSERIAL PRIMARY KEY,
    title                TEXT NOT NULL,
    question             TEXT NOT NULL,
    description          TEXT NOT NULL,
    category             VARCHAR(32) NOT NULL,
    end_time             TIMESTAMPTZ NOT NULL,
    stake_amount         NUMERIC(18,2) NOT NULL,
    resolution_source    TEXT NOT NULL,
    creator              VARCHAR(128) NOT NULL,
    status               VARCHAR(16) NOT NULL DEFAULT 'pending',  -- pending|approved|rejected|settled
    stage                VARCHAR(16) NOT NULL DEFAULT 'pre-review', -- pre-review|final-review|published
    current_probability  NUMERIC(6,5) NOT NULL DEFAULT 0.50000,
    yes_price            NUMERIC(6,5) NOT NULL DEFAULT 0.50000,
    no_price             NUMERIC(6,5) NOT NULL DEFAULT 0.50000,
    volume               NUMERIC(18,2) NOT NULL DEFAULT 0,
    liquidity            NUMERIC(18,2) NOT NULL DEFAULT 0,
    liquidity_providers  INT NOT NULL DEFAULT 1,
    create_time          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    update_time          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    review_time          TIMESTAMPTZ,
    review_note          TEXT,
    settled_at           TIMESTAMPTZ,
    outcome              VARCHAR(8) CHECK (outcome IN ('yes', 'no') OR outcome IS NULL),
    CHECK (yes_price >= 0 AND yes_price <= 1),
    CHECK (no_price  >= 0 AND no_price  <= 1),
    CHECK (current_probability >= 0 AND current_probability <= 1)
);

CREATE INDEX idx_markets_status ON markets(status);
CREATE INDEX idx_markets_stage ON markets(stage);
CREATE INDEX idx_markets_category ON markets(category);
CREATE INDEX idx_markets_creator ON markets(creator);
CREATE INDEX idx_markets_end_time ON markets(end_time);

-- 3) market_tags
CREATE TABLE market_tags (
    id        BIGSERIAL PRIMARY KEY,
    market_id BIGINT NOT NULL REFERENCES markets(id) ON DELETE CASCADE,
    tag       VARCHAR(64) NOT NULL
);
CREATE INDEX idx_market_tags_market_id ON market_tags(market_id);
CREATE INDEX idx_market_tags_tag ON market_tags(tag);

-- 4) market_ai_models
CREATE TABLE market_ai_models (
    id        BIGSERIAL PRIMARY KEY,
    market_id BIGINT NOT NULL REFERENCES markets(id) ON DELETE CASCADE,
    ai_model  VARCHAR(32) NOT NULL
);
CREATE INDEX idx_market_ai_models_market_id ON market_ai_models(market_id);

-- 5) transactions
CREATE TABLE transactions (
    id         BIGSERIAL PRIMARY KEY,
    market_id  BIGINT NOT NULL REFERENCES markets(id) ON DELETE CASCADE,
    type       VARCHAR(8) NOT NULL,   -- buy|sell
    option     VARCHAR(8) NOT NULL,   -- yes|no
    amount     NUMERIC(18,2) NOT NULL,
    price      NUMERIC(6,5) NOT NULL,
    total      NUMERIC(18,2) GENERATED ALWAYS AS (amount * price) STORED,
    trader     VARCHAR(128) NOT NULL,
    timestamp  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status     VARCHAR(16) NOT NULL DEFAULT 'completed' -- pending|completed|failed
);
CREATE INDEX idx_transactions_market_id ON transactions(market_id);
CREATE INDEX idx_transactions_trader ON transactions(trader);
CREATE INDEX idx_transactions_timestamp ON transactions(timestamp);
CREATE INDEX idx_transactions_type ON transactions(type);

-- 6) rewards
CREATE TABLE rewards (
    id          BIGSERIAL PRIMARY KEY,
    market_id   BIGINT NOT NULL REFERENCES markets(id) ON DELETE CASCADE,
    creator     VARCHAR(128) NOT NULL,
    amount      NUMERIC(18,2) NOT NULL,
    status      VARCHAR(16) NOT NULL DEFAULT 'pending', -- pending|claimed
    settled_at  TIMESTAMPTZ,
    claimed_at  TIMESTAMPTZ
);
CREATE INDEX idx_rewards_creator ON rewards(creator);
CREATE INDEX idx_rewards_status ON rewards(status);
CREATE INDEX idx_rewards_market_id ON rewards(market_id);

-- 7) orders (optional if using orderbook)
CREATE TABLE orders (
    id          BIGSERIAL PRIMARY KEY,
    market_id   BIGINT NOT NULL REFERENCES markets(id) ON DELETE CASCADE,
    type        VARCHAR(8) NOT NULL,   -- bid|ask
    price       NUMERIC(6,5) NOT NULL,
    amount      NUMERIC(18,2) NOT NULL,
    trader      VARCHAR(128) NOT NULL,
    status      VARCHAR(16) NOT NULL DEFAULT 'open', -- open|filled|cancelled
    create_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    update_time TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_orders_market_id ON orders(market_id);
CREATE INDEX idx_orders_status ON orders(status);

-- 8) price_history (optional for chart performance)
CREATE TABLE price_history (
    id         BIGSERIAL PRIMARY KEY,
    market_id  BIGINT NOT NULL REFERENCES markets(id) ON DELETE CASCADE,
    timestamp  TIMESTAMPTZ NOT NULL,
    price      NUMERIC(6,5) NOT NULL,
    volume     NUMERIC(18,2) NOT NULL DEFAULT 0,
    yes_price  NUMERIC(6,5),
    no_price   NUMERIC(6,5)
);
CREATE INDEX idx_price_history_market_time ON price_history(market_id, timestamp);

-- 9) trader_stats (optional pre-aggregated leaderboard)
CREATE TABLE trader_stats (
    id            BIGSERIAL PRIMARY KEY,
    trader        VARCHAR(128) NOT NULL,
    name          VARCHAR(128),
    avatar        TEXT,
    total_profit  NUMERIC(18,2) NOT NULL DEFAULT 0,
    win_rate      NUMERIC(5,2)  NOT NULL DEFAULT 0,
    total_trades  INT NOT NULL DEFAULT 0,
    update_time   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX idx_trader_stats_trader ON trader_stats(trader);

-- ---------------------------------------------------------------------------
-- Seed data (lightweight, for local dev/demo)
-- ---------------------------------------------------------------------------

INSERT INTO admins (username, password_hash) VALUES
  ('admin', 'bcrypt$2a$10$demo_salt_hash_admin'),
  ('reviewer', 'bcrypt$2a$10$demo_salt_hash_reviewer');

INSERT INTO markets
  (title, question, description, category, end_time, stake_amount, resolution_source,
   creator, status, stage, current_probability, yes_price, no_price, volume, liquidity,
   liquidity_providers, create_time)
VALUES
  ('Will Bitcoin exceed $150,000 by end of 2025?',
   'Will Bitcoin exceed $150,000 by end of 2025?',
   'Resolves YES if BTC trades above $150,000 on major exchanges before Dec 31, 2025.',
   'crypto', NOW() + INTERVAL '365 days', 10000, 'CoinMarketCap close price on 2025-12-31 23:59:59 UTC',
   '0xcreator1', 'approved', 'published', 0.65, 0.65, 0.35, 1250000, 500000, 12, NOW() - INTERVAL '30 days'),
  ('Will AI pass the Turing test convincingly by 2026?',
   'Will AI pass the Turing test convincingly by 2026?',
   'YES if an AI system achieves >70% success rate in an official Turing test competition.',
   'technology', NOW() + INTERVAL '540 days', 5000, 'Official Turing test competition results',
   '0xcreator2', 'pending', 'pre-review', 0.35, 0.35, 0.65, 450000, 200000, 5, NOW() - INTERVAL '20 days');

INSERT INTO market_tags (market_id, tag) VALUES
  (1, 'bitcoin'), (1, 'crypto'), (1, 'defi'),
  (2, 'ai'), (2, 'turing'), (2, 'ml');

INSERT INTO market_ai_models (market_id, ai_model) VALUES
  (1, 'chatgpt'), (1, 'claude'), (1, 'gemini'),
  (2, 'chatgpt'), (2, 'grok'), (2, 'perplexity');

INSERT INTO transactions
  (market_id, type, option, amount, price, trader, status, timestamp)
VALUES
  (1, 'buy', 'yes', 5000, 0.65, '0xabc...1234', 'completed', NOW() - INTERVAL '1 day'),
  (1, 'sell', 'yes', 2500, 0.66, '0xdef...5678', 'completed', NOW() - INTERVAL '2 hours'),
  (2, 'buy', 'no', 3000, 0.65, '0x999...8888', 'completed', NOW() - INTERVAL '3 hours');

INSERT INTO rewards (market_id, creator, amount, status, settled_at)
VALUES
  (1, '0xcreator1', 1250.50, 'pending', NOW() - INTERVAL '7 days');

INSERT INTO orders
  (market_id, type, price, amount, trader, status, create_time)
VALUES
  (1, 'bid', 0.64, 15000, '0xorder1', 'open', NOW() - INTERVAL '30 minutes'),
  (1, 'ask', 0.66, 12000, '0xorder2', 'open', NOW() - INTERVAL '20 minutes'),
  (2, 'bid', 0.34, 8000, '0xorder3', 'open', NOW() - INTERVAL '10 minutes');

INSERT INTO price_history
  (market_id, timestamp, price, volume, yes_price, no_price)
VALUES
  (1, NOW() - INTERVAL '3 hours', 0.64, 20000, 0.64, 0.36),
  (1, NOW() - INTERVAL '2 hours', 0.65, 18000, 0.65, 0.35),
  (1, NOW() - INTERVAL '1 hour', 0.66, 22000, 0.66, 0.34),
  (2, NOW() - INTERVAL '3 hours', 0.35, 8000, 0.35, 0.65),
  (2, NOW() - INTERVAL '1 hour', 0.36, 9000, 0.36, 0.64);

INSERT INTO trader_stats
  (trader, name, avatar, total_profit, win_rate, total_trades, update_time)
VALUES
  ('0x1234...5678', 'CryptoWhale', 'https://api.dicebear.com/7.x/avataaars/svg?seed=whale', 528000, 78.00, 1243, NOW()),
  ('0x9876...4321', 'DiamondHands', 'https://api.dicebear.com/7.x/avataaars/svg?seed=diamond', 389000, 72.00, 892, NOW());

