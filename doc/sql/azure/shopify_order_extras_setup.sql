-- Run on Azure SQL database hags_customer (one script, in order).
-- Creates shopify_order_extras + entitlement columns if missing, then globo_cards_json.

IF COL_LENGTH('purchase_entitlements', 'line_item_title') IS NULL
    ALTER TABLE purchase_entitlements ADD line_item_title NVARCHAR(500) NULL;
GO

IF COL_LENGTH('purchase_entitlements', 'line_properties_json') IS NULL
    ALTER TABLE purchase_entitlements ADD line_properties_json NVARCHAR(MAX) NULL;
GO

IF COL_LENGTH('purchase_entitlements', 'globo_cards_json') IS NULL
    ALTER TABLE purchase_entitlements ADD globo_cards_json NVARCHAR(MAX) NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'shopify_order_extras')
BEGIN
    CREATE TABLE shopify_order_extras (
        shopify_order_id            BIGINT NOT NULL PRIMARY KEY,
        shopify_order_name          NVARCHAR(32) NULL,
        order_note                  NVARCHAR(MAX) NULL,
        note_attributes_json        NVARCHAR(MAX) NULL,
        tags                        NVARCHAR(2000) NULL,
        source_name                 NVARCHAR(128) NULL,
        subscription_metadata_json  NVARCHAR(MAX) NULL,
        line_items_json             NVARCHAR(MAX) NULL,
        globo_cards_json            NVARCHAR(MAX) NULL,
        created_at                  DATETIME2 NOT NULL,
        updated_at                  DATETIME2 NOT NULL
    );
    CREATE INDEX idx_shopify_order_extras_updated ON shopify_order_extras(updated_at);
END
ELSE
BEGIN
    IF COL_LENGTH('shopify_order_extras', 'globo_cards_json') IS NULL
        ALTER TABLE shopify_order_extras ADD globo_cards_json NVARCHAR(MAX) NULL;
END
GO

PRINT 'shopify_order_extras setup complete.';
