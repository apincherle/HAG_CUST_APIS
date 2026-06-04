-- HAG Customer API — Azure SQL bootstrap (equivalent to Postgres dev schema via Hibernate ddl-auto=update)
-- Run once on an empty database before prod deploy (spring.jpa.hibernate.ddl-auto=validate).
--
-- HOW TO RUN (Azure Portal Query editor):
--   1. Open server "hags" → Query editor (preview)
--   2. In the DATABASE dropdown, select your app DB (e.g. free-sql-db-5015439) — NOT "master"
--   3. Sign in, then run this entire script
--
-- Or uncomment USE below and run in SSMS / Azure Data Studio / DBeaver:
-- USE [hags_customer];
-- GO

IF DB_NAME() = N'master'
BEGIN
    RAISERROR(N'Wrong database: you are connected to [master]. Select your app database in Query Editor (e.g. free-sql-db-5015439) or uncomment USE [...] above.', 16, 1);
    RETURN;
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'customers' AND schema_id = SCHEMA_ID(N'dbo'))
BEGIN
    CREATE TABLE customers (
        customer_id           UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
        shopify_customer_id   BIGINT NULL,
        shopify_updated_at    DATETIME2 NULL,
        email                 NVARCHAR(255) NOT NULL,
        phone                 NVARCHAR(50) NULL,
        full_name             NVARCHAR(200) NOT NULL,
        billing_line1         NVARCHAR(255) NULL,
        billing_line2         NVARCHAR(255) NULL,
        billing_city          NVARCHAR(100) NULL,
        billing_region        NVARCHAR(100) NULL,
        billing_postcode      NVARCHAR(20) NULL,
        billing_country       NVARCHAR(100) NULL,
        shipping_line1        NVARCHAR(255) NULL,
        shipping_line2        NVARCHAR(255) NULL,
        shipping_city         NVARCHAR(100) NULL,
        shipping_region       NVARCHAR(100) NULL,
        shipping_postcode     NVARCHAR(20) NULL,
        shipping_country      NVARCHAR(100) NULL,
        marketing_opt_in      BIT NOT NULL DEFAULT 0,
        status                NVARCHAR(20) NOT NULL,
        created_at            DATETIME2 NOT NULL,
        updated_at            DATETIME2 NOT NULL,
        deleted_at            DATETIME2 NULL,
        CONSTRAINT UQ_customers_email UNIQUE (email)
    );
    CREATE UNIQUE INDEX idx_customers_shopify_customer_id ON customers(shopify_customer_id) WHERE shopify_customer_id IS NOT NULL;
    CREATE INDEX idx_customers_email ON customers(email);
    CREATE INDEX idx_customers_status ON customers(status);
    CREATE INDEX idx_customers_created_at ON customers(created_at);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'submissions')
BEGIN
    CREATE TABLE submissions (
        submission_id        UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
        customer_id          UNIQUEIDENTIFIER NOT NULL,
        submission_number    NVARCHAR(50) NULL,
        service_level        NVARCHAR(20) NOT NULL,
        shipping_address_id  UNIQUEIDENTIFIER NULL,
        notes_customer       NVARCHAR(2000) NULL,
        status               NVARCHAR(50) NOT NULL,
        created_at           DATETIME2 NOT NULL,
        updated_at           DATETIME2 NOT NULL,
        CONSTRAINT FK_submissions_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
    );
    CREATE UNIQUE INDEX idx_submissions_submission_number ON submissions(submission_number) WHERE submission_number IS NOT NULL;
    CREATE INDEX idx_submissions_customer_id ON submissions(customer_id);
    CREATE INDEX idx_submissions_status ON submissions(status);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'submission_items')
BEGIN
    CREATE TABLE submission_items (
        item_id                 UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
        submission_id           UNIQUEIDENTIFIER NOT NULL,
        line_number             INT NOT NULL,
        game                    NVARCHAR(20) NOT NULL,
        free_text_line          NVARCHAR(300) NOT NULL,
        customer_notes          NVARCHAR(1000) NULL,
        requested_photo_slots   INT NOT NULL,
        front_photo_id          NVARCHAR(255) NULL,
        back_photo_id           NVARCHAR(255) NULL,
        enrichment_status       NVARCHAR(20) NOT NULL,
        enrichment_confidence   FLOAT NULL,
        matched_catalog_id      NVARCHAR(255) NULL,
        created_at              DATETIME2 NOT NULL,
        CONSTRAINT FK_submission_items_submission FOREIGN KEY (submission_id) REFERENCES submissions(submission_id)
    );
    CREATE INDEX idx_submission_items_submission_id ON submission_items(submission_id);
    CREATE INDEX idx_submission_items_enrichment_status ON submission_items(enrichment_status);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'submission_intake_codes')
BEGIN
    CREATE TABLE submission_intake_codes (
        intake_code_id   UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
        submission_id    UNIQUEIDENTIFIER NOT NULL,
        value            NVARCHAR(100) NOT NULL,
        barcode_format   NVARCHAR(20) NOT NULL,
        qr_value         NVARCHAR(500) NOT NULL,
        CONSTRAINT FK_intake_submission FOREIGN KEY (submission_id) REFERENCES submissions(submission_id),
        CONSTRAINT UQ_intake_submission UNIQUE (submission_id)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'card_certificate')
BEGIN
    CREATE TABLE card_certificate (
        id                BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        public_id         NVARCHAR(32) NOT NULL,
        serial_number     NVARCHAR(50) NOT NULL,
        submission_id     NVARCHAR(36) NOT NULL,
        customer_id       NVARCHAR(36) NOT NULL,
        item_id           NVARCHAR(36) NOT NULL,
        inspection_id     NVARCHAR(36) NULL,
        status            NVARCHAR(20) NOT NULL DEFAULT 'VERIFIED',
        card_name           NVARCHAR(255) NOT NULL,
        set_name            NVARCHAR(255) NULL,
        year                INT NULL,
        card_number         NVARCHAR(50) NULL,
        variant             NVARCHAR(100) NULL,
        grade               FLOAT NOT NULL,
        grader_version      NVARCHAR(50) NULL,
        graded_at           DATETIME2 NOT NULL,
        notes_public        NVARCHAR(MAX) NULL,
        notes_internal      NVARCHAR(MAX) NULL,
        checksum_sha256     NVARCHAR(64) NULL,
        created_at          DATETIME2 NOT NULL,
        updated_at          DATETIME2 NOT NULL,
        CONSTRAINT UQ_card_certificate_public_id UNIQUE (public_id),
        CONSTRAINT UQ_card_certificate_serial_number UNIQUE (serial_number)
    );
    CREATE INDEX idx_card_certificate_public_id ON card_certificate(public_id);
    CREATE INDEX idx_card_certificate_submission_id ON card_certificate(submission_id);
    CREATE INDEX idx_card_certificate_customer_id ON card_certificate(customer_id);
    CREATE INDEX idx_card_certificate_item_id ON card_certificate(item_id);
    CREATE INDEX idx_card_certificate_inspection_id ON card_certificate(inspection_id);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'card_image')
BEGIN
    CREATE TABLE card_image (
        id              BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        certificate_id  BIGINT NOT NULL,
        kind            NVARCHAR(50) NOT NULL,
        url             NVARCHAR(500) NOT NULL,
        width           INT NULL,
        height          INT NULL,
        created_at      DATETIME2 NOT NULL,
        CONSTRAINT fk_card_image_certificate FOREIGN KEY (certificate_id) REFERENCES card_certificate(id) ON DELETE CASCADE
    );
    CREATE INDEX idx_card_image_certificate_id ON card_image(certificate_id);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'shopify_webhook_events')
BEGIN
    CREATE TABLE shopify_webhook_events (
        webhook_id      NVARCHAR(64) NOT NULL PRIMARY KEY,
        topic           NVARCHAR(128) NOT NULL,
        shop_domain     NVARCHAR(255) NOT NULL,
        status          NVARCHAR(32) NOT NULL,
        payload_hash    NVARCHAR(64) NULL,
        error_message   NVARCHAR(MAX) NULL,
        received_at     DATETIME2 NOT NULL,
        processed_at    DATETIME2 NULL
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'purchase_entitlements')
BEGIN
    CREATE TABLE purchase_entitlements (
        entitlement_id        UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
        shopify_order_id      BIGINT NOT NULL,
        shopify_line_item_id  BIGINT NOT NULL,
        shopify_customer_id   BIGINT NOT NULL,
        shopify_order_name    NVARCHAR(32) NULL,
        tier_code             NVARCHAR(32) NOT NULL,
        cards_allowed         INT NOT NULL,
        cards_used            INT NOT NULL DEFAULT 0,
        status                NVARCHAR(32) NOT NULL,
        created_at            DATETIME2 NOT NULL,
        updated_at            DATETIME2 NOT NULL,
        CONSTRAINT uq_purchase_entitlements_order_line UNIQUE (shopify_order_id, shopify_line_item_id)
    );
    CREATE INDEX idx_purchase_entitlements_customer ON purchase_entitlements(shopify_customer_id);
    CREATE INDEX idx_purchase_entitlements_status ON purchase_entitlements(status);
END
GO

IF COL_LENGTH('purchase_entitlements', 'line_item_title') IS NULL
    ALTER TABLE purchase_entitlements ADD line_item_title NVARCHAR(500) NULL;
IF COL_LENGTH('purchase_entitlements', 'line_properties_json') IS NULL
    ALTER TABLE purchase_entitlements ADD line_properties_json NVARCHAR(MAX) NULL;
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
        created_at                  DATETIME2 NOT NULL,
        updated_at                  DATETIME2 NOT NULL
    );
    CREATE INDEX idx_shopify_order_extras_updated ON shopify_order_extras(updated_at);
END
GO

IF COL_LENGTH('shopify_order_extras', 'globo_cards_json') IS NULL
    ALTER TABLE shopify_order_extras ADD globo_cards_json NVARCHAR(MAX) NULL;
IF COL_LENGTH('purchase_entitlements', 'globo_cards_json') IS NULL
    ALTER TABLE purchase_entitlements ADD globo_cards_json NVARCHAR(MAX) NULL;
GO

PRINT 'Azure SQL bootstrap complete.';
