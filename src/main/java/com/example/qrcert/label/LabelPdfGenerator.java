package com.example.qrcert.label;

import java.io.IOException;
import java.util.List;

/**
 * PDF generation facade for label batch orchestration (enables testing without mocking concrete renderer).
 */
public interface LabelPdfGenerator {

    byte[] renderBatchOnSheets(List<LabelPrintData> cards) throws IOException;

    byte[] renderLabelPair(LabelPrintData data) throws IOException;
}
