package com.example.qrcert.label;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Writes all sample label PDFs under {@code target/} from current HTML templates.
 */
final class LabelSamplePdfGenerator {

    private static final String SAMPLE_CERT = "HAGS-2026-000123";
    private static final String SAMPLE_CERT_URL = LabelCertificateUrls.certUrl(SAMPLE_CERT);

    private LabelSamplePdfGenerator() {
    }

    static void regenerateAll() throws Exception {
        Path workDir = Path.of("target", "label-print-test");
        Files.createDirectories(workDir);

        Path logo = new ClassPathResource("labels/assets/hags_logo_gold.png").getFile().toPath();
        LabelPdfRenderer renderer = new LabelPdfRenderer();

        regenerateSingle(renderer, workDir, logo);
        regenerateBatch(renderer, workDir, logo, 20, "sample_labels_batch_20.pdf");
        regenerateBatch(renderer, workDir, logo, 80, "sample_labels_batch_80.pdf");

        System.out.println("All sample PDFs regenerated under target/ (close open PDFs before viewing)");
    }

    static void regenerateSingle(LabelPdfRenderer renderer, Path workDir, Path logo) throws Exception {
        Path qr = resolveSampleQr(workDir);
        LabelPrintData sample = new LabelPrintData(
            "Hags",
            "Charizard",
            "Base Set",
            "4/102",
            SAMPLE_CERT,
            9.5,
            SAMPLE_CERT_URL,
            logo,
            qr
        );

        byte[] pdf = renderer.renderLabelPair(sample);
        Path output = Path.of("target", "sample_labels.pdf");
        Files.write(output, pdf);

        try (PDDocument document = PDDocument.load(pdf)) {
            assertEquals(2, document.getNumberOfPages());
        }
        assertTrue(Files.size(output) > 0);
        System.out.println("  sample_labels.pdf");
    }

    static void regenerateBatch(
        LabelPdfRenderer renderer,
        Path workDir,
        Path logo,
        int batchSize,
        String outputFileName
    ) throws Exception {
        List<LabelPrintData> cards = buildSampleCards(batchSize, logo, workDir);
        byte[] pdf = renderer.renderBatchOnSheets(cards);
        Path output = Path.of("target", outputFileName);
        Files.write(output, pdf);

        int sheetsPerSide = LabelSheetLayout.sheetCount(batchSize);
        try (PDDocument document = PDDocument.load(pdf)) {
            assertEquals(sheetsPerSide * 2, document.getNumberOfPages());
        }
        System.out.println("  " + outputFileName);
    }

    private static List<LabelPrintData> buildSampleCards(int batchSize, Path logo, Path workDir)
        throws WriterException, IOException {
        List<LabelPrintData> cards = new ArrayList<>(batchSize);
        for (int i = 1; i <= batchSize; i++) {
            String certNumber = String.format("HAGS-2026-%06d", i);
            String certUrl = LabelCertificateUrls.certUrl(certNumber);
            Path qr = workDir.resolve("qr-" + i + ".png");
            writeQrPng(qr, certUrl);
            cards.add(new LabelPrintData(
                "Hags",
                "Umbreon ex",
                "Prismatic Evolutions",
                "161/131",
                certNumber,
                9.5,
                certUrl,
                logo,
                qr
            ));
        }
        return cards;
    }

    static Path resolveSampleQr(Path outputDir) throws WriterException, IOException {
        ClassPathResource resource = new ClassPathResource("labels/assets/qr_HAGS-2026-000123.png");
        if (resource.exists()) {
            Path cached = outputDir.resolve("qr_HAGS-2026-000123.png");
            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, cached, StandardCopyOption.REPLACE_EXISTING);
            }
            return cached;
        }
        Path generated = outputDir.resolve("qr_HAGS-2026-000123.png");
        writeQrPng(generated, SAMPLE_CERT_URL);
        return generated;
    }

    static void writeQrPng(Path path, String url) throws WriterException, IOException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, 300, 300, hints);
        MatrixToImageWriter.writeToPath(matrix, "PNG", path);
    }
}
