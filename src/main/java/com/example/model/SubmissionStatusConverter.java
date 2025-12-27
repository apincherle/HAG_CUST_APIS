package com.example.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SubmissionStatusConverter implements AttributeConverter<Submission.SubmissionStatus, String> {

    @Override
    public String convertToDatabaseColumn(Submission.SubmissionStatus status) {
        if (status == null) {
            return null;
        }
        return status.getDisplayValue();
    }

    @Override
    public Submission.SubmissionStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return Submission.SubmissionStatus.SUBMITTED_NOT_YET_RECEIVED; // Default
        }
        
        // Handle legacy status values from old enum
        String normalized = dbData.trim();
        switch (normalized.toUpperCase()) {
            case "DRAFT":
                return Submission.SubmissionStatus.SUBMITTED_NOT_YET_RECEIVED;
            case "SUBMITTED":
                return Submission.SubmissionStatus.SUBMITTED_RECEIVED;
            case "PROCESSING":
                return Submission.SubmissionStatus.GRADING_STARTED;
            case "COMPLETED":
                return Submission.SubmissionStatus.FINALISED;
            case "CANCELLED":
                // Cancelled submissions - could map to a status or keep as is
                // For now, map to finalised as cancelled is no longer a valid status
                return Submission.SubmissionStatus.FINALISED;
            default:
                // Try to convert using the new display values
                try {
                    return Submission.SubmissionStatus.fromDisplayValue(dbData);
                } catch (IllegalArgumentException e) {
                    // If conversion fails, default to SUBMITTED_NOT_YET_RECEIVED
                    System.err.println("WARNING: Unknown submission status in database: '" + dbData + 
                                     "'. Defaulting to SUBMITTED_NOT_YET_RECEIVED. " +
                                     "Please update the database record manually.");
                    return Submission.SubmissionStatus.SUBMITTED_NOT_YET_RECEIVED;
                }
        }
    }
}

