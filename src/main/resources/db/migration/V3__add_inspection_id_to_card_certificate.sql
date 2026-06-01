-- Link public certificate to Ximilar inspection (graded in HAGS_ximilar_ai)
ALTER TABLE card_certificate
    ADD COLUMN IF NOT EXISTS inspection_id VARCHAR(36);

CREATE INDEX IF NOT EXISTS idx_card_certificate_inspection_id
    ON card_certificate(inspection_id);

CREATE INDEX IF NOT EXISTS idx_card_certificate_serial_number
    ON card_certificate(serial_number);
