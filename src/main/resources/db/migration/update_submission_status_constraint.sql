-- Migration script to update submission status CHECK constraint
-- This allows the new status values to be stored in the database

-- Drop the old constraint if it exists
ALTER TABLE submissions DROP CONSTRAINT IF EXISTS submission_status_check;

-- Create new constraint with updated status values
ALTER TABLE submissions ADD CONSTRAINT submission_status_check 
    CHECK (status IN (
        'submitted-not yet received',
        'submitted - received',
        'grading started',
        'graded',
        'qa check',
        'finalised',
        'posted',
        -- Legacy values for backward compatibility during migration
        'DRAFT',
        'SUBMITTED',
        'PROCESSING',
        'COMPLETED',
        'CANCELLED'
    ));

