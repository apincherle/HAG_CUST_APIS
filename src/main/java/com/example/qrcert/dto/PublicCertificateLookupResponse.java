package com.example.qrcert.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * JSON shape expected by hags_certificate_lookup_ui ({@code lib/types.ts}).
 */
@Data
@Builder
public class PublicCertificateLookupResponse {
    private String certificateNumber;
    private String status;
    private String revokedReason;
    private String gradedDate;
    private CardPayload card;

    @Data
    @Builder
    public static class CardPayload {
        private String name;
        private String playerName;
        private Integer year;
        private String setName;
        private String cardNumber;
        private String variation;
        private String grade;
        private Map<String, String> subgrades;
        private String authenticationType;
        private String frontImageUrl;
        private String backImageUrl;
    }
}
