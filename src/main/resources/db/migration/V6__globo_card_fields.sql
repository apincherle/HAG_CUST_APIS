-- Azure SQL Server: structured Globo card fields (cardname-N, notes-N, card-front-N, card-back-N)
-- Prerequisite: run V5 first if shopify_order_extras does not exist (or use doc/sql/azure/shopify_order_extras_setup.sql).

IF OBJECT_ID(N'dbo.shopify_order_extras', N'U') IS NOT NULL
   AND COL_LENGTH('shopify_order_extras', 'globo_cards_json') IS NULL
    ALTER TABLE shopify_order_extras ADD globo_cards_json NVARCHAR(MAX) NULL;
GO

IF COL_LENGTH('purchase_entitlements', 'globo_cards_json') IS NULL
    ALTER TABLE purchase_entitlements ADD globo_cards_json NVARCHAR(MAX) NULL;
GO
