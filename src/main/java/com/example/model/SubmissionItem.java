package com.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "submission_items", indexes = {
    @Index(name = "idx_submission_items_submission_id", columnList = "submission_id"),
    @Index(name = "idx_submission_items_enrichment_status", columnList = "enrichment_status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionItem {
    @Id
    @Column(name = "item_id", columnDefinition = "VARCHAR(36)")
    private UUID itemId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;
    
    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "game", nullable = false, length = 20)
    private GameType game;
    
    @Column(name = "free_text_line", nullable = false, length = 300)
    private String freeTextLine;
    
    @Column(name = "customer_notes", length = 1000, nullable = true)
    private String customerNotes;
    
    @Column(name = "requested_photo_slots", nullable = false)
    private Integer requestedPhotoSlots = 2;
    
    @Column(name = "front_photo_id", length = 255, nullable = true)
    private String frontPhotoId;
    
    @Column(name = "back_photo_id", length = 255, nullable = true)
    private String backPhotoId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "enrichment_status", nullable = false, length = 20)
    private EnrichmentStatus enrichmentStatus = EnrichmentStatus.PENDING;
    
    @Column(name = "enrichment_confidence", nullable = true)
    private Double enrichmentConfidence;
    
    @Column(name = "matched_catalog_id", length = 255, nullable = true)
    private String matchedCatalogId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (itemId == null) {
            itemId = UUID.randomUUID();
        }
    }
    
    public enum GameType {
        POKEMON, MTG, SPORTS, OTHER
    }
    
    public enum EnrichmentStatus {
        PENDING, MATCHED, AMBIGUOUS, NOT_FOUND
    }
}

