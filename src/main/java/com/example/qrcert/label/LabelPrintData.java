package com.example.qrcert.label;

import java.nio.file.Path;

/**
 * Data for one slab label pair (front + back PDF pages).
 */
public record LabelPrintData(
    String companyName,
    String cardName,
    String setName,
    String cardNumber,
    String certificateNumber,
    double grade,
    String certificateUrl,
    Path logoImagePath,
    Path qrImagePath
) {
}
