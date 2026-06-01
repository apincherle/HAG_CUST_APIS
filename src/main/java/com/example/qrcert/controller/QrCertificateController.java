package com.example.qrcert.controller;

import com.example.qrcert.entity.CardCertificate;
import com.example.qrcert.service.CertificateService;
import com.example.qrcert.service.QrCodeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/qr-certificate")
@RequiredArgsConstructor
@Slf4j
public class QrCertificateController {

    private final CertificateService certificateService;
    private final QrCodeService qrCodeService;

    /**
     * POST /api/qr-certificate/generate
     * Creates a new certificate and generates QR code
     */
    @PostMapping("/generate")
    public ResponseEntity<CertificateResponse> generateCertificate(
            @RequestBody CertificateGenerateRequest request) {
        
        try {
            // Convert request to service request
            CertificateService.CertificateCreateRequest createRequest = 
                CertificateService.CertificateCreateRequest.builder()
                    .inspectionId(request.getInspectionId())
                    .submissionId(request.getSubmissionId())
                    .customerId(request.getCustomerId())
                    .itemId(request.getItemId())
                    .cardName(request.getCardName())
                    .setName(request.getSetName())
                    .year(request.getYear())
                    .cardNumber(request.getCardNumber())
                    .variant(request.getVariant())
                    .grade(request.getGrade())
                    .graderVersion(request.getGraderVersion())
                    .gradedAt(request.getGradedAt() != null ? request.getGradedAt() : LocalDateTime.now())
                    .notesPublic(request.getNotesPublic())
                    .notesInternal(request.getNotesInternal())
                    .checksumSha256(request.getChecksumSha256())
                    .status(request.getStatus())
                    .images(request.getImages())
                    .build();

            CertificateService.CertificateCreationResult result = 
                certificateService.createCertificate(createRequest);

            // Build response
            String certificateNumber = result.getSerialNumber();
            String gradedDate = result.getCertificate().getGradedAt() != null
                    ? result.getCertificate().getGradedAt().toLocalDate().toString()
                    : null;

            CertificateResponse response = CertificateResponse.builder()
                .id(result.getCertificate().getId())
                .publicId(result.getPublicId())
                .serialNumber(certificateNumber)
                .certificateNumber(certificateNumber)
                .gradedDate(gradedDate)
                .inspectionId(result.getCertificate().getInspectionId())
                .submissionId(result.getCertificate().getSubmissionId())
                .customerId(result.getCertificate().getCustomerId())
                .itemId(result.getCertificate().getItemId())
                .status(result.getCertificate().getStatus())
                .cardName(result.getCertificate().getCardName())
                .setName(result.getCertificate().getSetName())
                .year(result.getCertificate().getYear())
                .cardNumber(result.getCertificate().getCardNumber())
                .variant(result.getCertificate().getVariant())
                .grade(result.getCertificate().getGrade())
                .graderVersion(result.getCertificate().getGraderVersion())
                .gradedAt(result.getCertificate().getGradedAt())
                .notesPublic(result.getCertificate().getNotesPublic())
                .certificateUrl(result.getCertificateUrl())
                .qrImageUrl("/api/qr-certificate/qr/" + result.getPublicId())
                .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating certificate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/qr-certificate/qr/{publicId}
     * Returns the QR code image for a certificate
     */
    @GetMapping("/qr/{publicId}")
    public ResponseEntity<Resource> getQrCodeImage(@PathVariable String publicId) {
        try {
            // Verify certificate exists
            CardCertificate certificate = certificateService.findByPublicId(publicId);
            
            // Get QR code image path
            Path qrPath = qrCodeService.getQrCodeImagePath(publicId);
            
            if (!Files.exists(qrPath)) {
                // Generate QR code if it doesn't exist
                String certificateUrl = qrCodeService.generateCertificateUrl(publicId, true);
                qrPath = qrCodeService.generateQrCodeImage(certificateUrl, publicId);
            }

            Resource resource = new FileSystemResource(qrPath);
            
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "inline; filename=\"" + publicId + ".png\"")
                .body(resource);
        } catch (RuntimeException e) {
            log.debug("Certificate not found: {}", publicId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving QR code image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/qr-certificate/{publicId}
     * Returns certificate details
     */
    @GetMapping("/{publicId}")
    public ResponseEntity<CertificateResponse> getCertificate(@PathVariable String publicId) {
        try {
            CardCertificate certificate = certificateService.findByPublicId(publicId);
            
            String certificateUrl = qrCodeService.generateVerificationUrl(
                    certificate.getSerialNumber(), true);
            
            String certificateNumber = certificate.getSerialNumber();
            String gradedDate = certificate.getGradedAt() != null
                    ? certificate.getGradedAt().toLocalDate().toString()
                    : null;

            CertificateResponse response = CertificateResponse.builder()
                .id(certificate.getId())
                .publicId(certificate.getPublicId())
                .serialNumber(certificateNumber)
                .certificateNumber(certificateNumber)
                .gradedDate(gradedDate)
                .inspectionId(certificate.getInspectionId())
                .submissionId(certificate.getSubmissionId())
                .customerId(certificate.getCustomerId())
                .itemId(certificate.getItemId())
                .status(certificate.getStatus())
                .cardName(certificate.getCardName())
                .setName(certificate.getSetName())
                .year(certificate.getYear())
                .cardNumber(certificate.getCardNumber())
                .variant(certificate.getVariant())
                .grade(certificate.getGrade())
                .graderVersion(certificate.getGraderVersion())
                .gradedAt(certificate.getGradedAt())
                .notesPublic(certificate.getNotesPublic())
                .certificateUrl(certificateUrl)
                .qrImageUrl("/api/qr-certificate/qr/" + publicId)
                .build();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.debug("Certificate not found: {}", publicId);
            return ResponseEntity.notFound().build();
        }
    }

    // Request/Response DTOs
    @Data
    public static class CertificateGenerateRequest {
        /** Required for new slabs: Ximilar inspection_id from grading. */
        private String inspectionId;
        private String submissionId;
        private String customerId;
        /** Submission item id (cardId). */
        private String itemId;
        private String cardName;
        private String setName;
        private Integer year;
        private String cardNumber;
        private String variant;
        private Double grade;
        private String graderVersion;
        private LocalDateTime gradedAt;
        private String notesPublic;
        private String notesInternal;
        private String checksumSha256;
        private String status;
        private List<CertificateService.ImageRequest> images;
    }

    @lombok.Data
    @lombok.Builder
    public static class CertificateResponse {
        private Long id;
        private String publicId;
        private String serialNumber;
        /** Public certificate number on slab (same as serialNumber). */
        private String certificateNumber;
        /** ISO date (yyyy-MM-dd) when graded. */
        private String gradedDate;
        private String inspectionId;
        private String submissionId;
        private String customerId;
        private String itemId;
        private String status;
        private String cardName;
        private String setName;
        private Integer year;
        private String cardNumber;
        private String variant;
        private Double grade;
        private String graderVersion;
        private LocalDateTime gradedAt;
        private String notesPublic;
        private String certificateUrl;
        private String qrImageUrl;
    }
}

