package com.example.qrcert.label;

import com.example.qrcert.config.LabelBatchProperties;
import com.example.qrcert.config.QrCertificateProperties;
import com.example.qrcert.entity.CardCertificate;
import com.example.qrcert.repository.CardCertificateRepository;
import com.example.qrcert.service.QrCodeService;
import com.example.storage.DisabledBlobObjectStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LabelPrintServiceTest {

    private static final byte[] SAMPLE_PDF = new byte[] {37, 80, 68, 70};

    @Mock
    private CardCertificateRepository certificateRepository;

    @Mock
    private LabelPdfGenerator labelPdfGenerator;

    @TempDir
    Path tempDir;

    private LabelPrintService labelPrintService;
    private StubQrCodeService qrCodeService;

    @BeforeEach
    void setUp() {
        LabelBatchProperties properties = new LabelBatchProperties();
        properties.setLogoClasspath("labels/assets/hags_logo_gold.png");
        qrCodeService = new StubQrCodeService(tempDir);
        labelPrintService = new LabelPrintService(
            certificateRepository,
            labelPdfGenerator,
            qrCodeService,
            properties,
            new DisabledBlobObjectStore()
        );
    }

    @Test
    void generateSheetPdfForDateThrowsWhenNoCertificates() {
        LocalDate date = LocalDate.of(2026, 7, 7);
        when(certificateRepository.findGradedOnDate(any(), any())).thenReturn(List.of());

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> labelPrintService.generateSheetPdfForDate(date, null)
        );
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void generateSheetPdfForDateRendersAndReturnsPdf() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 7);
        CardCertificate cert = sampleCert(date);
        Files.write(tempDir.resolve("ABCD1234.png"), new byte[] {1});

        when(certificateRepository.findGradedOnDate(any(), any())).thenReturn(List.of(cert));
        when(labelPdfGenerator.renderBatchOnSheets(anyList())).thenReturn(SAMPLE_PDF);

        byte[] pdf = labelPrintService.generateSheetPdfForDate(date, null);

        assertArrayEquals(SAMPLE_PDF, pdf);
        verify(labelPdfGenerator).renderBatchOnSheets(anyList());
    }

    @Test
    void reprintSingleLabelThrowsWhenCertificateMissing() {
        when(certificateRepository.findBySerialNumberIgnoreCase("HAGS-2026-000099"))
            .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> labelPrintService.reprintSingleLabel("HAGS-2026-000099")
        );
        assertEquals(404, ex.getStatusCode().value());
    }

    private static CardCertificate sampleCert(LocalDate gradedDate) {
        return CardCertificate.builder()
            .publicId("ABCD1234")
            .serialNumber("HAGS-2026-000001")
            .submissionId("sub-1")
            .customerId("cust-1")
            .itemId("item-1")
            .status("VERIFIED")
            .cardName("Umbreon ex")
            .setName("Prismatic Evolutions")
            .cardNumber("161/131")
            .grade(9.5)
            .gradedAt(gradedDate.atTime(14, 30))
            .build();
    }

    private static final class StubQrCodeService extends QrCodeService {
        private final Path tempDir;

        StubQrCodeService(Path tempDir) {
            super(new QrCertificateProperties());
            this.tempDir = tempDir;
        }

        @Override
        public Path getQrCodeImagePath(String publicId) {
            return tempDir.resolve(publicId + ".png");
        }

        @Override
        public String generateVerificationUrl(String serialNumber, boolean signed) {
            return "https://example.com/cert/" + serialNumber;
        }
    }
}
