package com.example.qrcert.service;

import com.example.qrcert.dto.PublicCertificateLookupResponse;
import com.example.qrcert.entity.CardCertificate;
import com.example.qrcert.entity.CardImage;
import org.springframework.stereotype.Component;

@Component
public class CertificateLookupMapper {

    public PublicCertificateLookupResponse toPublicLookup(CardCertificate certificate) {
        String frontUrl = null;
        String backUrl = null;
        if (certificate.getImages() != null) {
            for (CardImage image : certificate.getImages()) {
                if (image.getKind() == null) {
                    continue;
                }
                String kind = image.getKind().toLowerCase();
                if (kind.contains("front") && frontUrl == null) {
                    frontUrl = image.getUrl();
                } else if (kind.contains("back") && backUrl == null) {
                    backUrl = image.getUrl();
                }
            }
        }

        String status = mapStatus(certificate.getStatus());
        String revokedReason = null;
        if ("revoked".equals(status) && certificate.getNotesPublic() != null) {
            revokedReason = certificate.getNotesPublic();
        }

        PublicCertificateLookupResponse.CardPayload card = PublicCertificateLookupResponse.CardPayload.builder()
                .name(certificate.getCardName())
                .playerName(certificate.getCardName())
                .year(certificate.getYear())
                .setName(certificate.getSetName())
                .cardNumber(certificate.getCardNumber())
                .variation(certificate.getVariant())
                .grade(certificate.getGrade() != null ? String.valueOf(certificate.getGrade()) : null)
                .authenticationType("Encapsulated")
                .frontImageUrl(frontUrl)
                .backImageUrl(backUrl)
                .build();

        String gradedDate = certificate.getGradedAt() != null
                ? certificate.getGradedAt().toLocalDate().toString()
                : null;

        return PublicCertificateLookupResponse.builder()
                .certificateNumber(certificate.getSerialNumber())
                .status(status)
                .revokedReason(revokedReason)
                .gradedDate(gradedDate)
                .card(card)
                .build();
    }

    private String mapStatus(String dbStatus) {
        if (dbStatus == null) {
            return "not_found";
        }
        return switch (dbStatus.toUpperCase()) {
            case "VERIFIED" -> "verified";
            case "REVOKED", "FLAGGED" -> "revoked";
            default -> "not_found";
        };
    }
}
