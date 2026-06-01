package com.example.qrcert.controller;

import com.example.qrcert.dto.CertificateInspectionMappingResponse;
import com.example.qrcert.dto.PublicCertificateLookupResponse;
import com.example.qrcert.entity.CardCertificate;
import com.example.qrcert.service.CertificateLookupMapper;
import com.example.qrcert.service.CertificateService;
import com.example.qrcert.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public certificate lookup for the verify UI ({@code /cert/{certificateId}}).
 * <p>
 * {@code certificateId} is the human-readable serial on the slab (e.g. HAGS-2026-000123).
 * {@code cardId} is the submission item id stored at grading time.
 */
@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
@Slf4j
public class CertificateLookupController {

    private final CertificateService certificateService;
    private final CertificateLookupMapper lookupMapper;
    private final QrCodeService qrCodeService;

    @GetMapping("/{certificateId}")
    public ResponseEntity<PublicCertificateLookupResponse> getCertificate(
            @PathVariable String certificateId) {
        return certificateService.findByCertificateId(certificateId)
                .map(lookupMapper::toPublicLookup)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Resolve slab certificate id to Ximilar inspection id (stored when QR was generated).
     */
    @GetMapping("/{certificateId}/inspection")
    public ResponseEntity<CertificateInspectionMappingResponse> getInspectionMapping(
            @PathVariable String certificateId) {
        return certificateService.findByCertificateId(certificateId)
                .filter(cert -> cert.getInspectionId() != null && !cert.getInspectionId().isBlank())
                .map(this::toInspectionMapping)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Lookup by submission item id ({@code cardId}).
     */
    @GetMapping("/by-card/{cardId}")
    public ResponseEntity<PublicCertificateLookupResponse> getCertificateByCardId(
            @PathVariable String cardId) {
        return certificateService.findByCardId(cardId)
                .map(lookupMapper::toPublicLookup)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-card/{cardId}/inspection")
    public ResponseEntity<CertificateInspectionMappingResponse> getInspectionMappingByCardId(
            @PathVariable String cardId) {
        return certificateService.findByCardId(cardId)
                .filter(cert -> cert.getInspectionId() != null && !cert.getInspectionId().isBlank())
                .map(this::toInspectionMapping)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private CertificateInspectionMappingResponse toInspectionMapping(CardCertificate cert) {
        String serial = cert.getSerialNumber();
        String gradedDate = cert.getGradedAt() != null
                ? cert.getGradedAt().toLocalDate().toString()
                : null;
        return CertificateInspectionMappingResponse.builder()
                .certificateId(serial)
                .certificateNumber(serial)
                .inspectionId(cert.getInspectionId())
                .cardId(cert.getItemId())
                .verificationUrl(qrCodeService.generateVerificationUrl(serial, true))
                .gradedDate(gradedDate)
                .build();
    }
}
