package com.example.qrcert.service;

import com.example.qrcert.config.QrCertificateProperties;
import com.example.qrcert.entity.CardCertificate;
import com.example.qrcert.entity.CardImage;
import com.example.qrcert.repository.CardCertificateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateService {

    private static final Pattern INSPECTION_ID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    private final CardCertificateRepository certificateRepository;
    private final QrCodeService qrCodeService;
    private final QrCertificateProperties properties;
    private final SecureRandom random = new SecureRandom();

    /**
     * Generates a new non-guessable public ID (16 hex characters)
     */
    public String generatePublicId() {
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /**
     * Generates a human-readable serial number
     */
    public String generateSerialNumber(Integer year, Integer sequence) {
        if (year == null) {
            year = LocalDateTime.now().getYear();
        }
        if (sequence == null) {
            // Get count of certificates for this year
            long count = certificateRepository.count();
            sequence = (int) (count + 1);
        }
        String prefix = properties.getSerialPrefix();
        return String.format("%s-%d-%06d", prefix, year, sequence);
    }

    /**
     * Creates a new certificate with QR code generation
     */
    @Transactional
    public CertificateCreationResult createCertificate(CertificateCreateRequest request) {
        if (request.getInspectionId() != null
                && !request.getInspectionId().isBlank()
                && !INSPECTION_ID_PATTERN.matcher(request.getInspectionId().trim()).matches()) {
            throw new IllegalArgumentException("inspectionId must be a UUID");
        }

        // Generate IDs
        String publicId = generatePublicId();
        // Ensure uniqueness
        while (certificateRepository.existsByPublicId(publicId)) {
            publicId = generatePublicId();
        }

        String serialNumber = generateSerialNumber(request.getYear(), null);
        // Ensure uniqueness
        while (certificateRepository.existsBySerialNumber(serialNumber)) {
            long count = certificateRepository.count();
            serialNumber = generateSerialNumber(request.getYear(), (int) (count + 1));
        }

        // Create certificate entity
        CardCertificate certificate = CardCertificate.builder()
            .publicId(publicId)
            .serialNumber(serialNumber)
            .submissionId(request.getSubmissionId())
            .customerId(request.getCustomerId())
            .itemId(request.getItemId())
            .inspectionId(request.getInspectionId() != null ? request.getInspectionId().trim() : null)
            .status(request.getStatus() != null ? request.getStatus() : "VERIFIED")
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
            .build();

        // Add images if provided
        if (request.getImages() != null) {
            for (ImageRequest imgReq : request.getImages()) {
                CardImage image = CardImage.builder()
                    .certificate(certificate)
                    .kind(imgReq.getKind())
                    .url(imgReq.getUrl())
                    .width(imgReq.getWidth())
                    .height(imgReq.getHeight())
                    .build();
                certificate.getImages().add(image);
            }
        }

        // Save certificate
        certificate = certificateRepository.save(certificate);

        // QR encodes public verify URL (/cert/{serialNumber}); mapping stored in DB
        String certificateUrl = qrCodeService.generateVerificationUrl(serialNumber, true);
        Path qrImagePath;
        try {
            qrImagePath = qrCodeService.generateQrCodeImage(certificateUrl, publicId);
        } catch (Exception e) {
            log.error("Failed to generate QR code image", e);
            throw new RuntimeException("Failed to generate QR code image", e);
        }

        log.info("Certificate created: publicId={}, serialNumber={}", publicId, serialNumber);

        return CertificateCreationResult.builder()
            .certificate(certificate)
            .publicId(publicId)
            .serialNumber(serialNumber)
            .certificateUrl(certificateUrl)
            .qrImagePath(qrImagePath)
            .build();
    }

    /**
     * Finds a certificate by public ID
     */
    public CardCertificate findByPublicId(String publicId) {
        return certificateRepository.findByPublicId(publicId)
            .orElseThrow(() -> new RuntimeException("Certificate not found: " + publicId));
    }

    /**
     * Resolve public certificate id (serial on slab) or legacy public_id.
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Optional<CardCertificate> findByCertificateId(String certificateId) {
        if (certificateId == null || certificateId.isBlank()) {
            return Optional.empty();
        }
        String trimmed = certificateId.trim();
        Optional<CardCertificate> bySerial =
                certificateRepository.findBySerialNumberIgnoreCase(trimmed);
        if (bySerial.isPresent()) {
            return bySerial;
        }
        return certificateRepository.findByPublicId(trimmed.toUpperCase(Locale.ROOT));
    }

    /**
     * Lookup by submission item id ({@code cardId}).
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Optional<CardCertificate> findByCardId(String cardId) {
        if (cardId == null || cardId.isBlank()) {
            return Optional.empty();
        }
        return certificateRepository.findByItemId(cardId.trim());
    }

    // Inner classes for request/response
    @lombok.Data
    @lombok.Builder
    public static class CertificateCreateRequest {
        /** Ximilar inspection_id from HAGS_ximilar_ai (stored at QR generation). */
        private String inspectionId;
        private String submissionId;
        private String customerId;
        /** Submission item id — public "cardId" for alternate lookup. */
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
        private List<ImageRequest> images;
    }

    @lombok.Data
    @lombok.Builder
    public static class ImageRequest {
        private String kind;
        private String url;
        private Integer width;
        private Integer height;
    }

    @lombok.Data
    @lombok.Builder
    public static class CertificateCreationResult {
        private CardCertificate certificate;
        private String publicId;
        private String serialNumber;
        private String certificateUrl;
        private Path qrImagePath;
    }
}

