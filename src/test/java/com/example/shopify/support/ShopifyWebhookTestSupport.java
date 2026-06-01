package com.example.shopify.support;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class ShopifyWebhookTestSupport {

    public static final String WEBHOOK_PATH = "/api/webhooks/shopify";
    public static final String TEST_SECRET = "test-webhook-secret";
    public static final String TEST_SHOP_DOMAIN = "h-a-g-s.myshopify.com";

    public static final String CUSTOMER_UPDATE_FIXTURE =
            "com/example/repository/cusomers/update.json";
    public static final String ORDER_CREATE_FIXTURE = "orders/create.json";
    public static final String ORDER_PAID_FIXTURE = "orders/paid.json";

    private ShopifyWebhookTestSupport() {
    }

    public static byte[] loadFixture(String classpathResource) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathResource);
        return StreamUtils.copyToByteArray(resource.getInputStream());
    }

    public static String signHmac(byte[] body, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(body));
    }
}
