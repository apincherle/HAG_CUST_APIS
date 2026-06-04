-- Copy-paste into Azure SQL (hags_customer). Same as tail of azure_sql_bootstrap.sql.

IF COL_LENGTH('shopify_order_extras', 'globo_cards_json') IS NULL
    ALTER TABLE shopify_order_extras ADD globo_cards_json NVARCHAR(MAX) NULL;
GO

IF COL_LENGTH('purchase_entitlements', 'globo_cards_json') IS NULL
    ALTER TABLE purchase_entitlements ADD globo_cards_json NVARCHAR(MAX) NULL;
GO
