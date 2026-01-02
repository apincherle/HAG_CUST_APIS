-- Add missing columns to card_certificate table if they don't exist
-- This migration adds the foreign key columns for linking certificates to clients, submissions, and items

DO $$ 
BEGIN
    -- Add submission_id column if it doesn't exist (UUID to match submissions.submission_id)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'card_certificate' AND column_name = 'submission_id') THEN
        ALTER TABLE card_certificate ADD COLUMN submission_id UUID;
    END IF;
    
    -- Add customer_id column if it doesn't exist (UUID to match customers.customer_id)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'card_certificate' AND column_name = 'customer_id') THEN
        ALTER TABLE card_certificate ADD COLUMN customer_id UUID;
    END IF;
    
    -- Add item_id column if it doesn't exist (UUID to match submission_items.item_id)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'card_certificate' AND column_name = 'item_id') THEN
        ALTER TABLE card_certificate ADD COLUMN item_id UUID;
    END IF;
END $$;

-- Make columns NOT NULL if there are no existing records, or update existing records first
-- For existing records, you may want to set default values or update them manually
-- Uncomment and modify the following if you have existing data:

-- UPDATE card_certificate SET submission_id = 0 WHERE submission_id IS NULL;
-- UPDATE card_certificate SET customer_id = 0 WHERE customer_id IS NULL;
-- UPDATE card_certificate SET item_id = 0 WHERE item_id IS NULL;

-- ALTER TABLE card_certificate ALTER COLUMN submission_id SET NOT NULL;
-- ALTER TABLE card_certificate ALTER COLUMN customer_id SET NOT NULL;
-- ALTER TABLE card_certificate ALTER COLUMN item_id SET NOT NULL;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_card_certificate_submission_id ON card_certificate(submission_id);
CREATE INDEX IF NOT EXISTS idx_card_certificate_customer_id ON card_certificate(customer_id);
CREATE INDEX IF NOT EXISTS idx_card_certificate_item_id ON card_certificate(item_id);

