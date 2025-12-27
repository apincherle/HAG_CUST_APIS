package com.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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
        @Column(name = "submission_id", columnDefinition = "UUID")
        private UUID submissionId;

        @Column(name = "customer_id", nullable = false, columnDefinition = "UUID")
        private UUID customerId;
    
    @Column(name = "submission_number", unique = true, length = 50)
    private String submissionNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "service_level", nullable = false, length = 20)
    private ServiceLevel serviceLevel = ServiceLevel.BRONZE;
    
    @Column(name = "shipping_address_id", columnDefinition = "UUID", nullable = true)
    private UUID shippingAddressId;
    
    @Column(name = "notes_customer", length = 2000, nullable = true)
    private String notesCustomer;
    
    @Column(name = "status", nullable = false, length = 50)
    @Convert(converter = SubmissionStatusConverter.class)
    private SubmissionStatus status = SubmissionStatus.SUBMITTED_NOT_YET_RECEIVED;
    
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
        SUBMITTED_NOT_YET_RECEIVED("submitted-not yet received"),
        SUBMITTED_RECEIVED("submitted - received"),
        GRADING_STARTED("grading started"),
        GRADED("graded"),
        QA_CHECK("qa check"),
        FINALISED("finalised"),
        POSTED("posted");
        
        private final String displayValue;
        
        SubmissionStatus(String displayValue) {
            this.displayValue = displayValue;
        }
        
        @JsonValue
        public String getDisplayValue() {
            return displayValue;
        }
        
        @JsonCreator
        public static SubmissionStatus fromDisplayValue(String displayValue) {
            if (displayValue == null || displayValue.isEmpty()) {
                return SUBMITTED_NOT_YET_RECEIVED; // Default
            }
            for (SubmissionStatus status : values()) {
                if (status.displayValue.equalsIgnoreCase(displayValue)) {
                    return status;
                }
            }
            // Try to match by enum name for backward compatibility
            try {
                return valueOf(displayValue.toUpperCase().replace(" ", "_").replace("-", "_"));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown submission status: " + displayValue);
            }
        }
    }
}
