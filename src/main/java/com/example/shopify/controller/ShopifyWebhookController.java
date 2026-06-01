package com.example.shopify.controller;

import com.example.shopify.service.ShopifyWebhookProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/shopify")
public class ShopifyWebhookController {

    private final ShopifyWebhookProcessor processor;

    public ShopifyWebhookController(ShopifyWebhookProcessor processor) {
        this.processor = processor;
    }

    @PostMapping
    public ResponseEntity<Void> receiveWebhook(
            @RequestBody byte[] rawBody,
            @RequestHeader(value = "X-Shopify-Topic", required = false) String topic,
            @RequestHeader(value = "X-Shopify-Shop-Domain", required = false) String shopDomain,
            @RequestHeader(value = "X-Shopify-Webhook-Id", required = false) String webhookId,
            @RequestHeader(value = "X-Shopify-Hmac-Sha256", required = false) String hmacHeader) {
        processor.process(rawBody, topic, shopDomain, webhookId, hmacHeader);
        return ResponseEntity.ok().build();
    }
}
