package com.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "submission_intake_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionIntakeCode {
    @Id
    @Column(name = "intake_code_id", columnDefinition = "UUID")
    private UUID intakeCodeId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private Submission submission;
    
    @Column(name = "value", nullable = false, length = 100)
    private String value;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "barcode_format", nullable = false, length = 20)
    private BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;
    
    @Column(name = "qr_value", nullable = false, length = 500)
    private String qrValue;
    
    @PrePersist
    protected void onCreate() {
        if (intakeCodeId == null) {
            intakeCodeId = UUID.randomUUID();
        }
    }
    
    public enum BarcodeFormat {
        CODE_128
    }
}

