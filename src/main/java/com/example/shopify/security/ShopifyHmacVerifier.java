package com.example.shopify.security;

import com.example.shopify.config.ShopifyProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class ShopifyHmacVerifier {

    private final ShopifyProperties properties;

    public ShopifyHmacVerifier(ShopifyProperties properties) {
        this.properties = properties;
    }

    public boolean isVerificationEnabled() {
        return properties.getWebhook().isVerifyHmac();
    }

    public boolean verify(byte[] rawBody, String hmacHeader) {
        if (!isVerificationEnabled()) {
            return true;
        }
        if (hmacHeader == null || hmacHeader.isBlank()) {
            return false;
        }
        String secret = properties.getWebhook().getSecret();
        if (secret == null || secret.isBlank()) {
            return false;
        }
        secret = secret.trim();
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(rawBody);
            String computed = Base64.getEncoder().encodeToString(digest);
            return MessageDigest.isEqual(
                    computed.getBytes(StandardCharsets.UTF_8),
                    hmacHeader.trim().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }
}
