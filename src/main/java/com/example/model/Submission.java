package com.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "submissions", indexes = {
    @Index(name = "idx_submissions_customer_id", columnList = "customer_id"),
    @Index(name = "idx_submissions_status", columnList = "status"),
    @Index(name = "idx_submissions_submission_number", columnList = "submission_number")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Submission {
    @Id
    @Column(name = "submission_id", columnDefinition = "VARCHAR(36)")
    private UUID submissionId;
    
    @Column(name = "customer_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID customerId;
    
    @Column(name = "submission_number", unique = true, length = 50)
    private String submissionNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "service_level", nullable = false, length = 20)
    private ServiceLevel serviceLevel = ServiceLevel.BRONZE;
    
    @Column(name = "shipping_address_id", columnDefinition = "VARCHAR(36)", nullable = true)
    private UUID shippingAddressId;
    
    @Column(name = "notes_customer", length = 2000, nullable = true)
    private String notesCustomer;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private SubmissionStatus status = SubmissionStatus.DRAFT;
    
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubmissionItem> items = new ArrayList<>();
    
    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private SubmissionIntakeCode intakeCode;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (submissionId == null) {
            submissionId = UUID.randomUUID();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum ServiceLevel {
        BRONZE, SILVER, GOLD
    }
    
    public enum SubmissionStatus {
        DRAFT, SUBMITTED, PROCESSING, COMPLETED, CANCELLED
    }
}
