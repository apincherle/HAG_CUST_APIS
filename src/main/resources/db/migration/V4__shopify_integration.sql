-- Shopify webhook idempotency and purchase entitlements (reference migration for prod/Azure SQL)

ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS shopify_customer_id BIGINT;

ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS shopify_updated_at TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS idx_customers_shopify_customer_id
    ON customers(shopify_customer_id)
    WHERE shopify_customer_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS shopify_webhook_events (
    webhook_id       VARCHAR(64) PRIMARY KEY,
    topic            VARCHAR(128) NOT NULL,
    shop_domain      VARCHAR(255) NOT NULL,
    status           VARCHAR(32) NOT NULL,
    payload_hash     VARCHAR(64),
    error_message    TEXT,
    received_at      TIMESTAMP NOT NULL,
    processed_at     TIMESTAMP
);

CREATE TABLE IF NOT EXISTS purchase_entitlements (
    entitlement_id        UUID PRIMARY KEY,
    shopify_order_id      BIGINT NOT NULL,
    shopify_line_item_id  BIGINT NOT NULL,
    shopify_customer_id   BIGINT NOT NULL,
    shopify_order_name    VARCHAR(32),
    tier_code             VARCHAR(32) NOT NULL,
    cards_allowed         INT NOT NULL,
    cards_used            INT NOT NULL DEFAULT 0,
    status                VARCHAR(32) NOT NULL,
    created_at            TIMESTAMP NOT NULL,
    updated_at            TIMESTAMP NOT NULL,
    CONSTRAINT uq_purchase_entitlements_order_line UNIQUE (shopify_order_id, shopify_line_item_id)
);

CREATE INDEX IF NOT EXISTS idx_purchase_entitlements_customer
    ON purchase_entitlements(shopify_customer_id);

CREATE INDEX IF NOT EXISTS idx_purchase_entitlements_status
    ON purchase_entitlements(status);
