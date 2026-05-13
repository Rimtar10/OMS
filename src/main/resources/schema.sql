-- ============================================
-- Trade Order Management System - Schema
-- ============================================

CREATE DATABASE IF NOT EXISTS trade_oms;
USE trade_oms;

-- Instruments: the assets being traded (stocks, bonds)
CREATE TABLE IF NOT EXISTS instruments (
    id      VARCHAR(10)  PRIMARY KEY,   -- e.g. "AAPL", "TSLA"
    symbol  VARCHAR(10)  NOT NULL,
    name    VARCHAR(100) NOT NULL
);

-- Orders: every buy/sell request submitted
CREATE TABLE IF NOT EXISTS orders (
    id           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    instrument_id VARCHAR(10) NOT NULL,
    type         VARCHAR(4)   NOT NULL,   -- BUY or SELL
    status       VARCHAR(10)  NOT NULL,   -- PENDING, FILLED, PARTIAL, CANCELLED
    quantity     INT          NOT NULL,
    price        DECIMAL(10,2) NOT NULL,
    user_id      VARCHAR(50)  NOT NULL,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (instrument_id) REFERENCES instruments(id)
);

-- Trades: created whenever a buy order matches a sell order
CREATE TABLE IF NOT EXISTS trades (
    id           BIGINT        PRIMARY KEY AUTO_INCREMENT,
    buy_order_id BIGINT        NOT NULL,
    sell_order_id BIGINT       NOT NULL,
    instrument_id VARCHAR(10)  NOT NULL,
    quantity     INT           NOT NULL,
    price        DECIMAL(10,2) NOT NULL,
    executed_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (buy_order_id)  REFERENCES orders(id),
    FOREIGN KEY (sell_order_id) REFERENCES orders(id),
    FOREIGN KEY (instrument_id) REFERENCES instruments(id)
);

-- Portfolio: tracks each user's position per instrument
CREATE TABLE IF NOT EXISTS portfolio (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    user_id       VARCHAR(50)  NOT NULL,
    instrument_id VARCHAR(10)  NOT NULL,
    quantity      INT          NOT NULL DEFAULT 0,
    avg_price     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    UNIQUE KEY uq_user_instrument (user_id, instrument_id),
    FOREIGN KEY (instrument_id) REFERENCES instruments(id)
);

-- Seed some instruments to work with
INSERT IGNORE INTO instruments (id, symbol, name) VALUES
    ('AAPL',  'AAPL',  'Apple Inc.'),
    ('TSLA',  'TSLA',  'Tesla Inc.'),
    ('GOOGL', 'GOOGL', 'Alphabet Inc.'),
    ('MSFT',  'MSFT',  'Microsoft Corporation');