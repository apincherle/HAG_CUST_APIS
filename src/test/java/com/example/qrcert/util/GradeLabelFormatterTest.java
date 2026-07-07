package com.example.qrcert.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GradeLabelFormatterTest {

    @Test
    void formatsHalfPointGrade() {
        GradeLabelFormatter.GradeLabel label = GradeLabelFormatter.format(9.5);
        assertEquals("9.5", label.numeric());
        assertEquals("GEM MT", label.suffix());
        assertEquals("9.5 GEM MT", label.full());
    }

    @Test
    void formatsWholeNumberGrade() {
        GradeLabelFormatter.GradeLabel label = GradeLabelFormatter.format(10.0);
        assertEquals("10", label.numeric());
        assertEquals("10 GEM MT", label.full());
    }
}
