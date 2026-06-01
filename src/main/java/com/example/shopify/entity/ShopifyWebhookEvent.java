package com.example.shopify.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "shopify_webhook_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopifyWebhookEvent {

    @Id
    @Column(name = "webhook_id", length = 64)
    private String webhookId;

    @Column(name = "topic", nullable = false, length = 128)
    private String topic;

    @Column(name = "shop_domain", nullable = false, length = 255)
    private String shopDomain;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ProcessingStatus status;

    @Column(name = "payload_hash", length = 64)
    private String payloadHash;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public enum ProcessingStatus {
        PROCESSING, OK, FAILED
    }
}
