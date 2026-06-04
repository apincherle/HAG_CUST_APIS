-- Azure SQL: rename reserved-word columns (run once on hags_customer)

IF COL_LENGTH('card_certificate', 'card_year') IS NULL AND COL_LENGTH('card_certificate', 'year') IS NOT NULL
    EXEC sp_rename 'card_certificate.year', 'card_year', 'COLUMN';
GO

IF COL_LENGTH('submission_intake_codes', 'code_value') IS NULL AND COL_LENGTH('submission_intake_codes', 'value') IS NOT NULL
    EXEC sp_rename 'submission_intake_codes.value', 'code_value', 'COLUMN';
GO
