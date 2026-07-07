package com.example.qrcert.controller;

import com.example.qrcert.label.LabelPrintOperations;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
@Tag(name = "Labels", description = "On-demand slab label PDFs (graded certificates by date)")
public class LabelPrintController {

    private final LabelPrintOperations labelPrintService;

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
        summary = "Download label sheet PDF for a grading day",
        description = """
            Generates an A4 print pack for every VERIFIED certificate graded on the given date \
            (fronts on some pages, backs on the rest — print → flip → print). \
            The PDF is returned in the HTTP response — use Swagger **Execute** or curl to download. \
            On Azure Container Apps there is no persistent local disk; this endpoint is the primary way to get the file. \
            When blob storage is configured, a copy is also cached under grading-exports/label-batches/{date}/labels.pdf.
            """,
        operationId = "downloadLabelSheetPdf"
    )
    public ResponseEntity<byte[]> downloadLabelSheetPdf(
        @Parameter(description = "Grading day (yyyy-MM-dd); defaults to today")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @Parameter(description = "Optional: only certs from this submission")
        @RequestParam(required = false) String submissionId) {
        LocalDate printDate = date != null ? date : LocalDate.now();
        byte[] pdf = labelPrintService.generateSheetPdfForDate(date, submissionId);
        String filename = "labels-" + printDate + ".pdf";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    @GetMapping(value = "/pdf/reprint", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
        summary = "Reprint a single label pair",
        description = "Returns a 2-page PDF (front + back) for one certificate by serial number.",
        operationId = "reprintLabel"
    )
    public ResponseEntity<byte[]> reprintLabel(
        @Parameter(description = "Certificate serial, e.g. HAGS-2026-000123", required = true)
        @RequestParam String certificateNumber) {
        byte[] pdf = labelPrintService.reprintSingleLabel(certificateNumber);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + certificateNumber + "_reprint.pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }
}
