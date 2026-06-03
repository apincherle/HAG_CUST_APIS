package com.example.shopify.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "purchase_entitlements", uniqueConstraints = {
        @UniqueConstraint(name = "uq_purchase_entitlements_order_line",
                columnNames = {"shopify_order_id", "shopify_line_item_id"})
}, indexes = {
        @Index(name = "idx_purchase_entitlements_customer", columnList = "shopify_customer_id"),
        @Index(name = "idx_purchase_entitlements_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseEntitlement {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "entitlement_id")
    private UUID entitlementId;

    @Column(name = "shopify_order_id", nullable = false)
    private Long shopifyOrderId;

    @Column(name = "shopify_line_item_id", nullable = false)
    private Long shopifyLineItemId;

    @Column(name = "shopify_customer_id", nullable = false)
    private Long shopifyCustomerId;

    @Column(name = "shopify_order_name", length = 32)
    private String shopifyOrderName;

    @Column(name = "tier_code", nullable = false, length = 32)
    private String tierCode;

    @Column(name = "cards_allowed", nullable = false)
    private Integer cardsAllowed;

    @Column(name = "cards_used", nullable = false)
    private Integer cardsUsed = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private EntitlementStatus status = EntitlementStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (entitlementId == null) {
            entitlementId = UUID.randomUUID();
        }
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum EntitlementStatus {
        ACTIVE, CONSUMED, REFUNDED, CANCELLED
    }
}
