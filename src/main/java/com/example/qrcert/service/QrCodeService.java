package com.example.qrcert.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.example.qrcert.config.QrCertificateProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrCodeService {

    private final QrCertificateProperties properties;

    /**
     * Public verification URL encoded in slab QR codes ({@code /cert/{serialNumber}}).
     */
    public String generateVerificationUrl(String serialNumber, boolean signed) {
        String baseUrl = normalizedBaseUrl();
        String path = properties.getVerificationPath();
        if (path == null || path.isBlank()) {
            path = "/cert";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/")) {
            path = path;
        }
        String certPath = path.endsWith("/") ? path + serialNumber : path + "/" + serialNumber;
        if (!signed) {
            return baseUrl + certPath;
        }
        String signature = signPublicId(serialNumber);
        return baseUrl + certPath + "?sig=" + signature;
    }

    /**
     * Legacy URL using opaque public_id ({@code /c/{publicId}}).
     */
    public String generateCertificateUrl(String publicId, boolean signed) {
        String baseUrl = normalizedBaseUrl();
        if (!signed) {
            return baseUrl + "/c/" + publicId;
        }
        String signature = signPublicId(publicId);
        return baseUrl + "/c/" + publicId + "?sig=" + signature;
    }

    private String normalizedBaseUrl() {
        return properties.getBaseUrl().endsWith("/")
                ? properties.getBaseUrl().substring(0, properties.getBaseUrl().length() - 1)
                : properties.getBaseUrl();
    }

    /**
     * Signs a public ID using HMAC-SHA256
     */
    public String signPublicId(String publicId) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                properties.getSecret().getBytes("UTF-8"), 
                "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] hash = mac.doFinal(publicId.getBytes("UTF-8"));
            // Use first 16 bytes, URL-safe base64, remove padding
            String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(hash);
            // Take first 16 characters, or full string if shorter
            String signature = encoded.length() >= 16 ? encoded.substring(0, 16) : encoded;
            return signature;
        } catch (NoSuchAlgorithmException | InvalidKeyException | java.io.UnsupportedEncodingException e) {
            log.error("Error signing public ID", e);
            throw new RuntimeException("Failed to sign public ID", e);
        }
    }

    /**
     * Verifies a signature for a public ID
     */
    public boolean verifySignature(String publicId, String signature) {
        String expectedSignature = signPublicId(publicId);
        return expectedSignature.equals(signature);
    }

    /**
     * Generates a QR code image and saves it to disk
     */
    public Path generateQrCodeImage(String url, String publicId) throws IOException, WriterException {
        // Ensure storage directory exists
        Path storagePath = Paths.get(properties.getQrStoragePath());
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }

        // Generate QR code
        int size = 300; // pixels
        int border = 2;
        
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, border);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, size, size, hints);

        // Create image
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, size, size);
        graphics.setColor(Color.BLACK);

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (bitMatrix.get(x, y)) {
                    graphics.fillRect(x, y, 1, 1);
                }
            }
        }
        graphics.dispose();

        // Save image
        Path qrPath = storagePath.resolve(publicId + ".png");
        javax.imageio.ImageIO.write(image, "PNG", qrPath.toFile());
        
        log.info("QR code generated and saved to: {}", qrPath);
        return qrPath;
    }

    /**
     * Gets the path to an existing QR code image
     */
    public Path getQrCodeImagePath(String publicId) {
        Path storagePath = Paths.get(properties.getQrStoragePath());
        return storagePath.resolve(publicId + ".png");
    }

    /**
     * Checks if a QR code image exists
     */
    public boolean qrCodeImageExists(String publicId) {
        Path qrPath = getQrCodeImagePath(publicId);
        return Files.exists(qrPath);
    }
}

