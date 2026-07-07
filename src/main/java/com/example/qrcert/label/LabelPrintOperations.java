package com.example.qrcert.label;

import java.time.LocalDate;

public interface LabelPrintOperations {

    byte[] generateSheetPdfForDate(LocalDate date, String submissionId);

    byte[] reprintSingleLabel(String certificateNumber);
}
