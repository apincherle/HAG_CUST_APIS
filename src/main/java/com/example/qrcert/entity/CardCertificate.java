package com.example.qrcert.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "card_certificate", indexes = {
    @Index(name = "idx_card_certificate_public_id", columnList = "public_id"),
    @Index(name = "idx_card_certificate_submission_id", columnList = "submission_id"),
    @Index(name = "idx_card_certificate_customer_id", columnList = "customer_id"),
    @Index(name = "idx_card_certificate_item_id", columnList = "item_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", unique = true, nullable = false, length = 32)
    private String publicId;

    @Column(name = "serial_number", unique = true, nullable = false, length = 50)
    private String serialNumber;

    @Column(name = "submission_id", nullable = false, length = 36)
    private String submissionId;

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Column(name = "item_id", nullable = false, length = 36)
    private String itemId;

    /** Ximilar inspection_id from POST /v1/cards/inspect (HAGS_ximilar_ai). */
    @Column(name = "inspection_id", length = 36)
    private String inspectionId;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "VERIFIED";

    @Column(name = "card_name", nullable = false, length = 255)
    private String cardName;

    @Column(name = "set_name", length = 255)
    private String setName;

    @Column(name = "year")
    private Integer year;

    @Column(name = "card_number", length = 50)
    private String cardNumber;

    @Column(name = "variant", length = 100)
    private String variant;

    @Column(name = "grade", nullable = false)
    private Double grade;

    @Column(name = "grader_version", length = 50)
    private String graderVersion;

    @Column(name = "graded_at", nullable = false)
    private LocalDateTime gradedAt;

    @Column(name = "notes_public", columnDefinition = "TEXT")
    private String notesPublic;

    @Column(name = "notes_internal", columnDefinition = "TEXT")
    private String notesInternal;

    @Column(name = "checksum_sha256", length = 64)
    private String checksumSha256;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "certificate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CardImage> images = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

