package com.example.qrcert.label;

/**
 * Grid layout for batch-printing 70 mm × 20 mm labels on A4 sheets.
 *
 * <p>3 labels across (70 × 3 = 210 mm), spaced rows for peel/cut alignment.
 */
public final class LabelSheetLayout {

    public static final float SHEET_WIDTH_MM = 210f;
    public static final float SHEET_HEIGHT_MM = 297f;
    public static final float LABEL_WIDTH_MM = 70f;
    public static final float LABEL_HEIGHT_MM = 20f;
    public static final int COLUMNS = 3;
    public static final float ROW_GAP_MM = 2f;
    public static final float MARGIN_TOP_MM = 8f;
    public static final float MARGIN_BOTTOM_MM = 8f;
    public static final float MARGIN_LEFT_MM = 0f;

    private LabelSheetLayout() {
    }

    public record GridSlot(int sheetIndex, int row, int column) {
    }

    public static int rowPitchMm() {
        return Math.round(LABEL_HEIGHT_MM + ROW_GAP_MM);
    }

    public static int rowsPerSheet() {
        float usableHeight = SHEET_HEIGHT_MM - MARGIN_TOP_MM - MARGIN_BOTTOM_MM;
        return (int) Math.floor(usableHeight / rowPitchMm());
    }

    public static int labelsPerSheet() {
        return COLUMNS * rowsPerSheet();
    }

    public static int sheetCount(int labelCount) {
        if (labelCount <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) labelCount / labelsPerSheet());
    }

    public static GridSlot slotForIndex(int index) {
        int perSheet = labelsPerSheet();
        int sheetIndex = index / perSheet;
        int onSheet = index % perSheet;
        int row = onSheet / COLUMNS;
        int column = onSheet % COLUMNS;
        return new GridSlot(sheetIndex, row, column);
    }

    public static float leftMm(int column) {
        return MARGIN_LEFT_MM + column * LABEL_WIDTH_MM;
    }

    public static float topMm(int row) {
        return MARGIN_TOP_MM + row * rowPitchMm();
    }

    /** PDF user-space origin is bottom-left; returns the lower-left corner of the label. */
    public static float xPt(int column) {
        return mmToPt(MARGIN_LEFT_MM + column * LABEL_WIDTH_MM);
    }

    public static float yPt(int row) {
        float topOfRowMm = MARGIN_TOP_MM + row * rowPitchMm();
        float lowerLeftMm = SHEET_HEIGHT_MM - topOfRowMm - LABEL_HEIGHT_MM;
        return mmToPt(lowerLeftMm);
    }

    public static float mmToPt(float mm) {
        return mm * 72f / 25.4f;
    }
}
