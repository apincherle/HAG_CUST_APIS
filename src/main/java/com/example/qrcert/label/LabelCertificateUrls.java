package com.example.qrcert.label;

/**
 * Production verify-site URLs for label print samples and tests.
 */
public final class LabelCertificateUrls {

    public static final String VERIFY_BASE = "https://jolly-ground-02380ae03.7.azurestaticapps.net";

    /** Short text on the label back; QR still encodes {@link #certUrl}. Point this host at VERIFY_BASE in DNS. */
    public static final String DISPLAY_HOST = "hags.co";

    private LabelCertificateUrls() {
    }

    public static String certUrl(String certificateNumber) {
        return VERIFY_BASE + "/cert/" + certificateNumber;
    }

    /** Compact URL printed on the back label (QR uses full {@link #certUrl}). */
    public static String certUrlDisplay(String certificateNumber) {
        return DISPLAY_HOST + "/cert/" + certificateNumber;
    }
}
