package com.example.shopify.service;

import com.example.shopify.config.ShopifyProperties;
import com.example.shopify.entity.ShopifyWebhookEvent;
import com.example.shopify.repository.ShopifyWebhookEventRepository;
import com.example.shopify.security.ShopifyHmacVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class ShopifyWebhookProcessor {

    private static final Logger log = LoggerFactory.getLogger(ShopifyWebhookProcessor.class);

    private final ShopifyProperties properties;
    private final ShopifyHmacVerifier hmacVerifier;
    private final ShopifyWebhookEventRepository eventRepository;
    private final ShopifyWebhookDispatcher dispatcher;

    public ShopifyWebhookProcessor(
            ShopifyProperties properties,
            ShopifyHmacVerifier hmacVerifier,
            ShopifyWebhookEventRepository eventRepository,
            ShopifyWebhookDispatcher dispatcher) {
        this.properties = properties;
        this.hmacVerifier = hmacVerifier;
        this.eventRepository = eventRepository;
        this.dispatcher = dispatcher;
    }

    public void process(
            byte[] rawBody,
            String topic,
            String shopDomain,
            String webhookId,
            String hmacHeader) {
        validateBeforePersistence(rawBody, topic, shopDomain, webhookId, hmacHeader);
        processInTransaction(rawBody, topic, shopDomain, webhookId);
    }

    private void validateBeforePersistence(
            byte[] rawBody,
            String topic,
            String shopDomain,
            String webhookId,
            String hmacHeader) {
        if (!properties.getWebhook().isEnabled()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopify webhooks are disabled");
        }
        validateShopDomain(shopDomain);
        if (!hmacVerifier.verify(rawBody, hmacHeader)) {
            if (properties.getWebhook().getSecret() == null || properties.getWebhook().getSecret().isBlank()) {
                log.error("Shopify webhook HMAC failed: SHOPIFY_WEBHOOK_SECRET is not set on the container");
            } else if (hmacHeader == null || hmacHeader.isBlank()) {
                log.warn("Shopify webhook HMAC failed: missing X-Shopify-Hmac-Sha256 header");
            } else {
                log.warn("Shopify webhook HMAC failed: secret does not match Shopify signing secret "
                        + "(check SHOPIFY_WEBHOOK_SECRET vs Admin → Settings → Notifications → Webhooks)");
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Shopify webhook signature");
        }
        if (topic == null || topic.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing X-Shopify-Topic");
        }
        if (webhookId == null || webhookId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing X-Shopify-Webhook-Id");
        }
    }

    @Transactional
    void processInTransaction(byte[] rawBody, String topic, String shopDomain, String webhookId) {
        Optional<ShopifyWebhookEvent> existing = eventRepository.findById(webhookId);
        if (existing.isPresent() && existing.get().getStatus() == ShopifyWebhookEvent.ProcessingStatus.OK) {
            log.debug("Duplicate Shopify webhook {}, already processed", webhookId);
            return;
        }

        ShopifyWebhookEvent event = existing.orElse(null);
        if (event == null) {
            event = new ShopifyWebhookEvent();
            event.setWebhookId(webhookId);
            event.setTopic(topic);
            event.setShopDomain(shopDomain != null ? shopDomain : "");
            event.setStatus(ShopifyWebhookEvent.ProcessingStatus.PROCESSING);
            event.setPayloadHash(sha256Hex(rawBody));
            event.setReceivedAt(LocalDateTime.now());
            try {
                event = eventRepository.save(event);
            } catch (DataIntegrityViolationException duplicate) {
                event = eventRepository.findById(webhookId)
                        .orElseThrow(() -> duplicate);
                if (event.getStatus() == ShopifyWebhookEvent.ProcessingStatus.OK) {
                    return;
                }
            }
        }

        try {
            dispatcher.dispatch(topic, rawBody);
            event.setStatus(ShopifyWebhookEvent.ProcessingStatus.OK);
            event.setProcessedAt(LocalDateTime.now());
            event.setErrorMessage(null);
            eventRepository.save(event);
        } catch (Exception e) {
            log.error("Failed processing Shopify webhook {} topic {}", webhookId, topic, e);
            event.setStatus(ShopifyWebhookEvent.ProcessingStatus.FAILED);
            event.setProcessedAt(LocalDateTime.now());
            event.setErrorMessage(truncate(e.getMessage(), 2000));
            eventRepository.save(event);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Webhook processing failed: " + e.getMessage());
        }
    }

    private void validateShopDomain(String shopDomain) {
        String expected = properties.getShop().getDomain();
        if (expected == null || expected.isBlank()) {
            return;
        }
        expected = expected.trim();
        String received = shopDomain != null ? shopDomain.trim() : null;
        if (received == null || !expected.equalsIgnoreCase(received)) {
            log.warn(
                    "Shopify shop domain mismatch: X-Shopify-Shop-Domain='{}', SHOPIFY_SHOP_DOMAIN='{}'. "
                            + "In Shopify Admin open Settings → Domains and use the *.myshopify.com hostname, "
                            + "or clear SHOPIFY_SHOP_DOMAIN to skip this check.",
                    received,
                    expected);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unexpected Shopify shop domain");
        }
    }

    private static String sha256Hex(byte[] body) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(body));
        } catch (Exception e) {
            return null;
        }
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
