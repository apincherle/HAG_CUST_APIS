package com.example.qrcert.util;

/**
 * Maps numeric grades to slab label text (e.g. {@code 9.5 GEM MT}).
 */
public final class GradeLabelFormatter {

    private static final String SUFFIX = "GEM MT";

    private GradeLabelFormatter() {
    }

    public record GradeLabel(String numeric, String suffix, String full) {
    }

    public static GradeLabel format(double grade) {
        String numeric = formatNumeric(grade);
        String full = numeric + " " + SUFFIX;
        return new GradeLabel(numeric, SUFFIX, full);
    }

    private static String formatNumeric(double grade) {
        if (grade == Math.floor(grade) && !Double.isInfinite(grade)) {
            return String.valueOf((long) grade);
        }
        return String.format("%.1f", grade);
    }
}
