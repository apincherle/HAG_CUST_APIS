package com.example.qrcert.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CertificateInspectionMappingResponse {
    private String certificateId;
    /** Public certificate number on the slab (same as certificateId / serial_number). */
    private String certificateNumber;
    private String inspectionId;
    private String cardId;
    private String verificationUrl;
    /** ISO date (yyyy-MM-dd) when the card was graded. */
    private String gradedDate;
}
