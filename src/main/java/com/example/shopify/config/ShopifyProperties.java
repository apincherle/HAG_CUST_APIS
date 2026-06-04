package com.example.shopify.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "shopify")
public class ShopifyProperties {

    private Webhook webhook = new Webhook();
    private Shop shop = new Shop();
    /** SKU (or variant SKU) -> tier code, e.g. HAGS-SUB-BRONZE -> BRONZE */
    private Map<String, String> tierSkuMapping = new HashMap<>();
    /** Tier code -> number of cards allowed on the entitlement */
    private Map<String, Integer> tierCardsAllowed = new HashMap<>();

    @Data
    public static class Webhook {
        private boolean enabled = true;
        private String secret = "";
        private boolean verifyHmac = true;
        /** Log full JSON body to container logs (disable in prod when stable). */
        private boolean logPayload = false;
    }

    @Data
    public static class Shop {
        private String domain = "";
    }
}
