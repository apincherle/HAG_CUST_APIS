package com.example.shopify.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ShopifyWebhookProcessorDomainTest {

    @Test
    void normalizeShopHost_stripsSchemeAndPath() {
        assertEquals("h-a-g-s.myshopify.com", ShopifyWebhookProcessor.normalizeShopHost("https://h-a-g-s.myshopify.com/"));
        assertEquals("h-a-g-s.myshopify.com", ShopifyWebhookProcessor.normalizeShopHost("h-a-g-s.myshopify.com"));
    }

    @Test
    void normalizeShopHost_blankIsNull() {
        assertNull(ShopifyWebhookProcessor.normalizeShopHost("  "));
    }
}
