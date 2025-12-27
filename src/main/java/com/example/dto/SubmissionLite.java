package com.example.dto;

import com.example.model.Submission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionLite {
    private UUID submissionId;
    private UUID customerId;
    private String submissionNumber;
    private Submission.ServiceLevel serviceLevel;
    private UUID shippingAddressId;
    private String notesCustomer;
    private Submission.SubmissionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static SubmissionLite fromEntity(Submission submission) {
        return SubmissionLite.builder()
                .submissionId(submission.getSubmissionId())
                .customerId(submission.getCustomerId())
                .submissionNumber(submission.getSubmissionNumber())
                .serviceLevel(submission.getServiceLevel())
                .shippingAddressId(submission.getShippingAddressId())
                .notesCustomer(submission.getNotesCustomer())
                .status(submission.getStatus())
                .createdAt(submission.getCreatedAt())
                .updatedAt(submission.getUpdatedAt())
                .build();
    }
}

