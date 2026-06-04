package com.example.shopify.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Order-level data from Shopify webhooks: Globo note_attributes, Subscribee/subscription hints, tags.
 */
@Entity
@Table(name = "shopify_order_extras")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopifyOrderExtras {

    @Id
    @Column(name = "shopify_order_id")
    private Long shopifyOrderId;

    @Column(name = "shopify_order_name", length = 32)
    private String shopifyOrderName;

    @Column(name = "order_note", columnDefinition = "TEXT")
    private String orderNote;

    @Column(name = "note_attributes_json", columnDefinition = "TEXT")
    private String noteAttributesJson;

    @Column(name = "tags", length = 2000)
    private String tags;

    @Column(name = "source_name", length = 128)
    private String sourceName;

    @Column(name = "subscription_metadata_json", columnDefinition = "TEXT")
    private String subscriptionMetadataJson;

    /** Full line_items snapshot: properties (Globo), selling_plan_allocation (Subscribee). */
    @Column(name = "line_items_json", columnDefinition = "TEXT")
    private String lineItemsJson;

    /**
     * Structured Globo card slots: cardname-N, notes-N, card-front-N, card-back-N per line (bundles up to 5+).
     */
    @Column(name = "globo_cards_json", columnDefinition = "TEXT")
    private String globoCardsJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
