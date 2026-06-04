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

import java.nio.charset.StandardCharsets;
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
        logInboundWebhook(rawBody, topic, shopDomain, webhookId, hmacHeader);

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
        String expectedRaw = properties.getShop().getDomain();
        if (expectedRaw == null || expectedRaw.isBlank()) {
            return;
        }
        String expected = normalizeShopHost(expectedRaw);
        String received = normalizeShopHost(shopDomain);
        if (received == null || !expected.equals(received)) {
            log.warn(
                    "Shopify shop domain mismatch: X-Shopify-Shop-Domain='{}' (normalized='{}'), "
                            + "SHOPIFY_SHOP_DOMAIN='{}' (normalized='{}'). "
                            + "Use the *.myshopify.com host only (no https://), or remove SHOPIFY_SHOP_DOMAIN to skip.",
                    shopDomain,
                    received,
                    expectedRaw,
                    expected);
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Unexpected Shopify shop domain: received="
                            + received
                            + ", expected="
                            + expected
                            + " (check X-Shopify-Shop-Domain in Shopify webhook delivery)");
        }
    }

    private void logInboundWebhook(
            byte[] rawBody,
            String topic,
            String shopDomain,
            String webhookId,
            String hmacHeader) {
        int bodyBytes = rawBody != null ? rawBody.length : 0;
        log.info(
                "Shopify webhook received: topic={}, X-Shopify-Shop-Domain={}, webhookId={}, "
                        + "bodyBytes={}, hmacHeaderPresent={}, SHOPIFY_SHOP_DOMAIN='{}'",
                topic,
                shopDomain,
                webhookId,
                bodyBytes,
                hmacHeader != null && !hmacHeader.isBlank(),
                properties.getShop().getDomain());

        if (properties.getWebhook().isLogPayload() && rawBody != null && rawBody.length > 0) {
            log.info("Shopify webhook JSON body: {}", truncateUtf8(rawBody, 32_000));
        }
    }

    private static String truncateUtf8(byte[] rawBody, int maxChars) {
        String json = new String(rawBody, StandardCharsets.UTF_8);
        return json.length() <= maxChars ? json : json.substring(0, maxChars) + "...(truncated)";
    }

    /** Strips https://, paths, and casing so env typos like https://store.myshopify.com/ still match. */
    static String normalizeShopHost(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String host = value.trim().toLowerCase();
        if (host.startsWith("https://")) {
            host = host.substring(8);
        } else if (host.startsWith("http://")) {
            host = host.substring(7);
        }
        int slash = host.indexOf('/');
        if (slash >= 0) {
            host = host.substring(0, slash);
        }
        return host.isEmpty() ? null : host;
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
