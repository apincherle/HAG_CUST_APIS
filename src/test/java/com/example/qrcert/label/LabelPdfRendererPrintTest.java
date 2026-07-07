package com.example.qrcert.label;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regenerates <strong>all</strong> sample label PDFs from the current HTML templates.
 *
 * <p>Run after every template change:
 * <pre>mvn test -Dtest=LabelPdfRendererPrintTest</pre>
 *
 * <p>Outputs:
 * <ul>
 *   <li>{@code target/sample_labels.pdf}</li>
 *   <li>{@code target/sample_labels_batch_20.pdf}</li>
 *   <li>{@code target/sample_labels_batch_80.pdf}</li>
 * </ul>
 */
class LabelPdfRendererPrintTest {

    @Test
    void regenerateAllSampleLabelPdfs() throws Exception {
        LabelSamplePdfGenerator.regenerateAll();
        assertTrue(Files.size(Path.of("target", "sample_labels.pdf")) > 0);
        assertTrue(Files.size(Path.of("target", "sample_labels_batch_20.pdf")) > 0);
        assertTrue(Files.size(Path.of("target", "sample_labels_batch_80.pdf")) > 0);
    }
}
