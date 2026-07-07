package com.example.qrcert.label;

import com.example.qrcert.config.LabelBatchProperties;
import com.example.qrcert.entity.CardCertificate;
import com.example.qrcert.repository.CardCertificateRepository;
import com.example.qrcert.service.QrCodeService;
import com.example.storage.BlobObjectStore;
import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * On-demand label PDF generation — no batch rows persisted.
 * On Azure Container Apps the PDF is returned in the HTTP response (download via Swagger or curl).
 * When blob storage is configured, a copy is also written under
 * {@code grading-exports/label-batches/{date}/labels.pdf} for optional re-fetch.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LabelPrintService implements LabelPrintOperations {

    private final CardCertificateRepository certificateRepository;
    private final LabelPdfGenerator labelPdfRenderer;
    private final QrCodeService qrCodeService;
    private final LabelBatchProperties properties;
    private final BlobObjectStore blobObjectStore;

    @Transactional(readOnly = true)
    public byte[] generateSheetPdfForDate(LocalDate date, String submissionId) {
        LocalDate printDate = date != null ? date : LocalDate.now();
        List<CardCertificate> certificates = findCertificatesGradedOn(printDate, submissionId);
        if (certificates.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No graded certificates found for " + printDate
                    + (submissionId != null ? " (submission " + submissionId + ")" : "")
            );
        }

        byte[] pdf = renderPdf(certificates);
        uploadOptionalBlobCopy(printDate, submissionId, pdf);
        log.info("Generated label PDF for {} ({} cards)", printDate, certificates.size());
        return pdf;
    }

    @Transactional(readOnly = true)
    public byte[] reprintSingleLabel(String certificateNumber) {
        CardCertificate cert = certificateRepository.findBySerialNumberIgnoreCase(certificateNumber.trim())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Certificate not found: " + certificateNumber
            ));
        try {
            Path logoPath = resolveLogoPath();
            return labelPdfRenderer.renderLabelPair(toPrintData(cert, logoPath));
        } catch (IOException e) {
            log.error("Failed to reprint label for {}", certificateNumber, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate reprint PDF");
        }
    }

    private byte[] renderPdf(List<CardCertificate> certificates) {
        try {
            Path logoPath = resolveLogoPath();
            List<LabelPrintData> printData = new ArrayList<>(certificates.size());
            for (CardCertificate cert : certificates) {
                printData.add(toPrintData(cert, logoPath));
            }
            return labelPdfRenderer.renderBatchOnSheets(printData);
        } catch (IOException e) {
            log.error("Failed to render label PDF", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate label PDF");
        }
    }

    private void uploadOptionalBlobCopy(LocalDate printDate, String submissionId, byte[] pdf) {
        if (!blobObjectStore.isAvailable()) {
            return;
        }
        try {
            String blobKey = blobPdfKey(printDate, submissionId);
            blobObjectStore.put(properties.getBlobContainer(), blobKey, pdf, "application/pdf");
            log.info("Cached label PDF at {}/{}", properties.getBlobContainer(), blobKey);
        } catch (IOException e) {
            log.warn("Label PDF generated but blob upload failed for {}: {}", printDate, e.getMessage());
        }
    }

    private List<CardCertificate> findCertificatesGradedOn(LocalDate printDate, String submissionId) {
        LocalDateTime start = printDate.atStartOfDay();
        LocalDateTime end = printDate.plusDays(1).atStartOfDay();
        if (submissionId != null && !submissionId.isBlank()) {
            return certificateRepository.findGradedOnDateBySubmission(submissionId.trim(), start, end);
        }
        return certificateRepository.findGradedOnDate(start, end);
    }

    private String blobPdfKey(LocalDate printDate, String submissionId) {
        String suffix = submissionId != null && !submissionId.isBlank()
            ? "/" + submissionId.trim()
            : "";
        return properties.getBlobPrefix() + "/" + printDate + suffix + "/labels.pdf";
    }

    private LabelPrintData toPrintData(CardCertificate cert, Path logoPath) {
        Path qrPath = resolveQrPath(cert);
        String certUrl = qrCodeService.generateVerificationUrl(cert.getSerialNumber(), true);
        return new LabelPrintData(
            properties.getCompanyName(),
            cert.getCardName(),
            nullToEmpty(cert.getSetName()),
            nullToEmpty(cert.getCardNumber()),
            cert.getSerialNumber(),
            cert.getGrade(),
            certUrl,
            logoPath,
            qrPath
        );
    }

    private Path resolveQrPath(CardCertificate cert) {
        Path qrPath = qrCodeService.getQrCodeImagePath(cert.getPublicId());
        if (Files.exists(qrPath)) {
            return qrPath;
        }
        try {
            String url = qrCodeService.generateVerificationUrl(cert.getSerialNumber(), true);
            return qrCodeService.generateQrCodeImage(url, cert.getPublicId());
        } catch (IOException | WriterException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "QR image missing for certificate " + cert.getSerialNumber()
            );
        }
    }

    private Path resolveLogoPath() {
        try {
            ClassPathResource resource = new ClassPathResource(properties.getLogoClasspath());
            return resource.getFile().toPath();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Label logo asset not available");
        }
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
