package com.example.shopify.security;

import com.example.shopify.config.ShopifyProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopifyHmacVerifierTest {

    private static final String SECRET = "dev-webhook-secret-change-me";

    private ShopifyHmacVerifier verifier;

    @BeforeEach
    void setUp() {
        ShopifyProperties properties = new ShopifyProperties();
        properties.getWebhook().setSecret(SECRET);
        properties.getWebhook().setVerifyHmac(true);
        verifier = new ShopifyHmacVerifier(properties);
    }

    @Test
    void verify_validHmac() throws Exception {
        byte[] body = "{\"id\":123}".getBytes(StandardCharsets.UTF_8);
        String hmac = sign(body, SECRET);
        assertTrue(verifier.verify(body, hmac));
    }

    @Test
    void verify_rejectsTamperedBody() throws Exception {
        byte[] body = "{\"id\":123}".getBytes(StandardCharsets.UTF_8);
        String hmac = sign(body, SECRET);
        byte[] tampered = "{\"id\":999}".getBytes(StandardCharsets.UTF_8);
        assertFalse(verifier.verify(tampered, hmac));
    }

    @Test
    void verify_skippedWhenDisabled() {
        ShopifyProperties properties = new ShopifyProperties();
        properties.getWebhook().setVerifyHmac(false);
        ShopifyHmacVerifier noVerify = new ShopifyHmacVerifier(properties);
        assertTrue(noVerify.verify("anything".getBytes(StandardCharsets.UTF_8), null));
    }

    private static String sign(byte[] body, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(body));
    }
}
