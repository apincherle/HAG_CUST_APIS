-- Globo (line item properties / uploads) and Subscribee (selling plans, subscription properties)

ALTER TABLE purchase_entitlements
    ADD COLUMN IF NOT EXISTS line_item_title VARCHAR(500);

ALTER TABLE purchase_entitlements
    ADD COLUMN IF NOT EXISTS line_properties_json TEXT;

CREATE TABLE IF NOT EXISTS shopify_order_extras (
    shopify_order_id            BIGINT PRIMARY KEY,
    shopify_order_name          VARCHAR(32),
    order_note                  TEXT,
    note_attributes_json        TEXT,
    tags                        VARCHAR(2000),
    source_name                 VARCHAR(128),
    subscription_metadata_json  TEXT,
    line_items_json             TEXT,
    created_at                  TIMESTAMP NOT NULL,
    updated_at                  TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_shopify_order_extras_updated
    ON shopify_order_extras(updated_at);
