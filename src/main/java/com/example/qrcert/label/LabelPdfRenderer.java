package com.example.qrcert.label;

import com.example.qrcert.util.GradeLabelFormatter;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Renders 70 mm × 20 mm front/back label pages from HTML templates.
 */
@Service
@Slf4j
public class LabelPdfRenderer implements LabelPdfGenerator {

    private static final String LABELS_DIR = "labels/";
    private static final String FRONT_TEMPLATE = "hags_slab_label_front_v1.html";
    private static final String BACK_TEMPLATE = "hags_slab_label_back_v1.html";
    private static final String THEME_CSS = "label_theme.css";
    private static final String SHEET_CSS = "label_sheet.css";
    private static final String STYLE_OPEN_TAG = "<style>";
    private static final String STYLE_CLOSE_TAG = "</style>";

    public byte[] renderLabelPair(LabelPrintData data) throws IOException {
        return renderBatch(List.of(data));
    }

    /**
     * One 70×20 mm PDF page per label side (front, back, front, back, …).
     * Use for label printers that accept custom page sizes.
     */
    public byte[] renderBatch(List<LabelPrintData> cards) throws IOException {
        if (cards.isEmpty()) {
            throw new IllegalArgumentException("At least one card is required");
        }

        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream merged = new ByteArrayOutputStream();
        merger.setDestinationStream(merged);

        Path labelsBase = resolveLabelsBaseDir();
        String themeCss = loadClasspathText(LABELS_DIR + THEME_CSS);

        for (LabelPrintData card : cards) {
            byte[] front = renderPage(FRONT_TEMPLATE, themeCss, labelsBase, card);
            byte[] back = renderPage(BACK_TEMPLATE, themeCss, labelsBase, card);
            merger.addSource(new ByteArrayInputStream(front));
            merger.addSource(new ByteArrayInputStream(back));
        }

        merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        return merged.toByteArray();
    }

    /**
     * Batch print layout on A4 sheets: all fronts in a spaced grid, then all backs
     * in the same grid positions (for print fronts → flip sheet → print backs).
     *
     * <p>Grid: 3 labels per row (70 mm), 2 mm vertical gap, 8 mm top/bottom margin.
     */
    public byte[] renderBatchOnSheets(List<LabelPrintData> cards) throws IOException {
        if (cards.isEmpty()) {
            throw new IllegalArgumentException("At least one card is required");
        }

        Path labelsBase = resolveLabelsBaseDir();
        String themeCss = loadClasspathText(LABELS_DIR + THEME_CSS);
        String sheetCss = loadClasspathText(LABELS_DIR + SHEET_CSS);
        int perSheet = LabelSheetLayout.labelsPerSheet();

        List<byte[]> pages = new ArrayList<>();
        for (int start = 0; start < cards.size(); start += perSheet) {
            int end = Math.min(start + perSheet, cards.size());
            pages.add(renderA4Sheet(cards.subList(start, end), FRONT_TEMPLATE, themeCss, sheetCss, labelsBase));
        }
        for (int start = 0; start < cards.size(); start += perSheet) {
            int end = Math.min(start + perSheet, cards.size());
            pages.add(renderA4Sheet(cards.subList(start, end), BACK_TEMPLATE, themeCss, sheetCss, labelsBase));
        }

        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream merged = new ByteArrayOutputStream();
        merger.setDestinationStream(merged);
        for (byte[] page : pages) {
            merger.addSource(new ByteArrayInputStream(page));
        }
        merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        return merged.toByteArray();
    }

    private byte[] renderA4Sheet(
        List<LabelPrintData> cardsOnSheet,
        String templateName,
        String themeCss,
        String sheetCss,
        Path labelsBase
    ) throws IOException {
        String templateStyles = extractInlineStyles(loadClasspathText(LABELS_DIR + templateName));
        StringBuilder grid = new StringBuilder();
        grid.append("<table class=\"sheet-grid\">\n");

        int rowsNeeded = (int) Math.ceil(cardsOnSheet.size() / (double) LabelSheetLayout.COLUMNS);
        for (int row = 0; row < rowsNeeded; row++) {
            grid.append("<tr>");
            for (int column = 0; column < LabelSheetLayout.COLUMNS; column++) {
                int index = row * LabelSheetLayout.COLUMNS + column;
                grid.append("<td class=\"label-cell\">");
                if (index < cardsOnSheet.size()) {
                    LabelPrintData card = cardsOnSheet.get(index);
                    String labelHtml = renderFilledTemplate(templateName, themeCss, card);
                    grid.append(extractBodyContent(labelHtml));
                }
                grid.append("</td>");
            }
            grid.append("</tr>\n");

            if (row < rowsNeeded - 1) {
                grid.append("<tr class=\"row-gap\"><td colspan=\"")
                    .append(LabelSheetLayout.COLUMNS)
                    .append("\"></td></tr>\n");
            }
        }
        grid.append("</table>\n");

        String sheetHtml = """
            <!DOCTYPE html>
            <html class="sheet-root" lang="en">
            <head>
              <meta charset="UTF-8"/>
              <title>Hags label sheet</title>
              <style>
            """ + themeCss + "\n" + sheetCss + "\n" + templateStyles + """
              </style>
            </head>
            <body class="sheet-page">
            """ + grid + """
            </body>
            </html>
            """;

        return renderHtmlToPdf(
            sheetHtml,
            labelsBase,
            LabelSheetLayout.SHEET_WIDTH_MM,
            LabelSheetLayout.SHEET_HEIGHT_MM
        );
    }

    private String renderFilledTemplate(
        String templateName,
        String themeCss,
        LabelPrintData data
    ) throws IOException {
        String html = loadClasspathText(LABELS_DIR + templateName);
        html = inlineStylesheet(html, themeCss);
        return applyPlaceholders(html, data);
    }

    private byte[] renderPage(
        String templateName,
        String themeCss,
        Path labelsBase,
        LabelPrintData data
    ) throws IOException {
        String html = renderFilledTemplate(templateName, themeCss, data);
        return renderHtmlToPdf(html, labelsBase, LabelSheetLayout.LABEL_WIDTH_MM, LabelSheetLayout.LABEL_HEIGHT_MM);
    }

    private byte[] renderHtmlToPdf(
        String html,
        Path labelsBase,
        float widthMm,
        float heightMm
    ) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useDefaultPageSize(widthMm, heightMm, BaseRendererBuilder.PageSizeUnits.MM);
        builder.withHtmlContent(html, labelsBase.toUri().toString());
        builder.toStream(out);
        builder.useFastMode();
        builder.run();
        return out.toByteArray();
    }

    private static String extractBodyContent(String html) {
        int bodyStart = html.indexOf("<body>");
        int bodyEnd = html.indexOf("</body>");
        if (bodyStart < 0 || bodyEnd < 0) {
            throw new IllegalStateException("Label template missing body element");
        }
        return html.substring(bodyStart + "<body>".length(), bodyEnd).trim();
    }

    private static String extractInlineStyles(String html) {
        StringBuilder styles = new StringBuilder();
        int searchFrom = 0;
        int start = html.indexOf(STYLE_OPEN_TAG, searchFrom);
        while (start >= 0) {
            int end = html.indexOf(STYLE_CLOSE_TAG, start);
            if (end < 0) {
                break;
            }
            styles.append(html, start + STYLE_OPEN_TAG.length(), end).append('\n');
            searchFrom = end + STYLE_CLOSE_TAG.length();
            start = html.indexOf(STYLE_OPEN_TAG, searchFrom);
        }
        return styles.toString();
    }

    private String inlineStylesheet(String html, String themeCss) {
        String styleBlock = STYLE_OPEN_TAG + "\n" + themeCss + "\n" + STYLE_CLOSE_TAG;
        return html.replaceFirst(
            "<link rel=\"stylesheet\" href=\"label_theme.css\"/>",
            styleBlock
        );
    }

    private String applyPlaceholders(String html, LabelPrintData data) {
        GradeLabelFormatter.GradeLabel gradeLabel = GradeLabelFormatter.format(data.grade());

        Map<String, String> values = Map.of(
            "{{companyName}}", data.companyName(),
            "{{logoImage}}", toFileUri(data.logoImagePath()),
            "{{cardName}}", data.cardName(),
            "{{setName}}", data.setName(),
            "{{cardNumber}}", data.cardNumber(),
            "{{certificateNumber}}", data.certificateNumber(),
            "{{gradeNumeric}}", gradeLabel.numeric(),
            "{{gradeSuffix}}", gradeLabel.suffix(),
            "{{certificateUrlDisplay}}", LabelCertificateUrls.certUrlDisplay(data.certificateNumber()),
            "{{qrImage}}", toFileUri(data.qrImagePath())
        );

        String result = html;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace(entry.getKey(), escapeHtml(entry.getValue()));
        }
        return result;
    }

    private static String escapeHtml(String value) {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }

    private static String toFileUri(Path path) {
        return path.toUri().toString();
    }

    private static String loadClasspathText(String classpathLocation) throws IOException {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static Path resolveLabelsBaseDir() throws IOException {
        ClassPathResource resource = new ClassPathResource(LABELS_DIR);
        URI uri = resource.getURI();
        if ("file".equals(uri.getScheme())) {
            return Path.of(uri);
        }
        throw new IOException(
            "Label templates must be on disk for PDF rendering; run from Maven/IDE, not a packaged JAR without extraction."
        );
    }
}
