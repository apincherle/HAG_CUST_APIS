-- Structured Globo card fields: cardname-N, notes-N, card-front-N, card-back-N

ALTER TABLE shopify_order_extras
    ADD COLUMN IF NOT EXISTS globo_cards_json TEXT;

ALTER TABLE purchase_entitlements
    ADD COLUMN IF NOT EXISTS globo_cards_json TEXT;
