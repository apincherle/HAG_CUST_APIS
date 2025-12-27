package com.example.dto;

import com.example.model.SubmissionItem;
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
public class SubmissionItemLite {
    private UUID itemId;
    private UUID submissionId;
    private Integer lineNumber;
    private SubmissionItem.GameType game;
    private String freeTextLine;
    private String customerNotes;
    private Integer requestedPhotoSlots;
    private String frontPhotoId;
    private String backPhotoId;
    private SubmissionItem.EnrichmentStatus enrichmentStatus;
    private Double enrichmentConfidence;
    private String matchedCatalogId;
    private LocalDateTime createdAt;
    
    public static SubmissionItemLite fromEntity(SubmissionItem item) {
        return SubmissionItemLite.builder()
                .itemId(item.getItemId())
                .submissionId(item.getSubmission().getSubmissionId())
                .lineNumber(item.getLineNumber())
                .game(item.getGame())
                .freeTextLine(item.getFreeTextLine())
                .customerNotes(item.getCustomerNotes())
                .requestedPhotoSlots(item.getRequestedPhotoSlots())
                .frontPhotoId(item.getFrontPhotoId())
                .backPhotoId(item.getBackPhotoId())
                .enrichmentStatus(item.getEnrichmentStatus())
                .enrichmentConfidence(item.getEnrichmentConfidence())
                .matchedCatalogId(item.getMatchedCatalogId())
                .createdAt(item.getCreatedAt())
                .build();
    }
}

