-- Create card_certificate table
CREATE TABLE IF NOT EXISTS card_certificate (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(32) NOT NULL UNIQUE,
    serial_number VARCHAR(50) NOT NULL UNIQUE,
    submission_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    item_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'VERIFIED',
    card_name VARCHAR(255) NOT NULL,
    set_name VARCHAR(255),
    year INTEGER,
    card_number VARCHAR(50),
    variant VARCHAR(100),
    grade DOUBLE PRECISION NOT NULL,
    grader_version VARCHAR(50),
    graded_at TIMESTAMP NOT NULL,
    notes_public TEXT,
    notes_internal TEXT,
    checksum_sha256 VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create card_image table
CREATE TABLE IF NOT EXISTS card_image (
    id BIGSERIAL PRIMARY KEY,
    certificate_id BIGINT NOT NULL,
    kind VARCHAR(50) NOT NULL,
    url VARCHAR(500) NOT NULL,
    width INTEGER,
    height INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_image_certificate FOREIGN KEY (certificate_id) 
        REFERENCES card_certificate(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_card_certificate_public_id ON card_certificate(public_id);
CREATE INDEX IF NOT EXISTS idx_card_certificate_submission_id ON card_certificate(submission_id);
CREATE INDEX IF NOT EXISTS idx_card_certificate_customer_id ON card_certificate(customer_id);
CREATE INDEX IF NOT EXISTS idx_card_certificate_item_id ON card_certificate(item_id);
CREATE INDEX IF NOT EXISTS idx_card_image_certificate_id ON card_image(certificate_id);

