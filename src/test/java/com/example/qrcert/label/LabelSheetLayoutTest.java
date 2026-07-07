package com.example.qrcert.label;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LabelSheetLayoutTest {

    @Test
    void fitsThreeLabelsAcrossA4() {
        assertEquals(3, LabelSheetLayout.COLUMNS);
        assertEquals(210f, LabelSheetLayout.COLUMNS * LabelSheetLayout.LABEL_WIDTH_MM);
    }

    @Test
    void placesTwentyLabelsOnOneSheetPerSide() {
        assertEquals(1, LabelSheetLayout.sheetCount(20));
    }

    @Test
    void placesEightyLabelsOnThreeSheetsPerSide() {
        assertEquals(3, LabelSheetLayout.sheetCount(80));
        assertEquals(36, LabelSheetLayout.labelsPerSheet());
    }

    @Test
    void mapsIndexToGridSlots() {
        LabelSheetLayout.GridSlot first = LabelSheetLayout.slotForIndex(0);
        assertEquals(0, first.sheetIndex());
        assertEquals(0, first.row());
        assertEquals(0, first.column());

        LabelSheetLayout.GridSlot fourth = LabelSheetLayout.slotForIndex(3);
        assertEquals(0, fourth.sheetIndex());
        assertEquals(1, fourth.row());
        assertEquals(0, fourth.column());

        LabelSheetLayout.GridSlot lastOnFirstSheet = LabelSheetLayout.slotForIndex(
            LabelSheetLayout.labelsPerSheet() - 1
        );
        assertEquals(0, lastOnFirstSheet.sheetIndex());
    }
}
