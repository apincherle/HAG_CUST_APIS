-- Rename columns that break H2 DDL (year, value are reserved)

ALTER TABLE card_certificate RENAME COLUMN year TO card_year;

ALTER TABLE submission_intake_codes RENAME COLUMN value TO code_value;
