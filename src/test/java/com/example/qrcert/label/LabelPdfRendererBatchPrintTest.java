package com.example.qrcert.label;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Batch layout assertions. PDFs are also written by {@link LabelPdfRendererPrintTest}.
 */
class LabelPdfRendererBatchPrintTest {

    @Test
    void writesTwentyLabelBatchOnSpacedA4Sheets() throws Exception {
        Path workDir = Path.of("target", "label-batch-test-20");
        Files.createDirectories(workDir);
        Path logo = new ClassPathResource("labels/assets/hags_logo_gold.png").getFile().toPath();
        LabelSamplePdfGenerator.regenerateBatch(
            new LabelPdfRenderer(), workDir, logo, 20, "batch-test-20.pdf");
        Path output = Path.of("target", "batch-test-20.pdf");
        assertTrue(Files.size(output) > 0);
        try (PDDocument document = PDDocument.load(Files.readAllBytes(output))) {
            assertEquals(LabelSheetLayout.sheetCount(20) * 2, document.getNumberOfPages());
        }
    }

    @Test
    void writesEightyLabelBatchAcrossMultipleA4Sheets() throws Exception {
        Path workDir = Path.of("target", "label-batch-test-80");
        Files.createDirectories(workDir);
        Path logo = new ClassPathResource("labels/assets/hags_logo_gold.png").getFile().toPath();
        LabelSamplePdfGenerator.regenerateBatch(
            new LabelPdfRenderer(), workDir, logo, 80, "batch-test-80.pdf");
        Path output = Path.of("target", "batch-test-80.pdf");
        assertTrue(Files.size(output) > 0);
        try (PDDocument document = PDDocument.load(Files.readAllBytes(output))) {
            assertEquals(LabelSheetLayout.sheetCount(80) * 2, document.getNumberOfPages());
        }
    }
}
