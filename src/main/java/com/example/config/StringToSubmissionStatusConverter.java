package com.example.config;

import com.example.model.Submission;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StringToSubmissionStatusConverter implements Converter<String, Submission.SubmissionStatus> {

    @Override
    public Submission.SubmissionStatus convert(@NonNull String source) {
        if (source == null || source.trim().isEmpty()) {
            return Submission.SubmissionStatus.SUBMITTED_NOT_YET_RECEIVED; // Default
        }
        
        // Spring automatically URL-decodes query parameters, so "submitted-not%20yet%20received" 
        // becomes "submitted-not yet received" before reaching this converter
        String trimmed = source.trim();
        
        // Use the enum's fromDisplayValue method which handles both display values and enum names
        try {
            return Submission.SubmissionStatus.fromDisplayValue(trimmed);
        } catch (IllegalArgumentException e) {
            // Provide a helpful error message with valid values
            String validValues = java.util.Arrays.toString(
                java.util.Arrays.stream(Submission.SubmissionStatus.values())
                    .map(Submission.SubmissionStatus::getDisplayValue)
                    .toArray());
            throw new IllegalArgumentException("Invalid submission status: '" + trimmed + 
                "'. Valid values are: " + validValues, e);
        }
    }
}

