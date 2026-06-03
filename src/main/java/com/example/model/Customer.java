package com.example.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_customers_email", columnList = "email"),
    @Index(name = "idx_customers_status", columnList = "status"),
    @Index(name = "idx_customers_created_at", columnList = "created_at"),
    @Index(name = "idx_customers_shopify_customer_id", columnList = "shopify_customer_id", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "customer_id")
    private UUID customerId;
    
    @Column(name = "shopify_customer_id", unique = true)
    private Long shopifyCustomerId;

    @Column(name = "shopify_updated_at")
    private LocalDateTime shopifyUpdatedAt;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(name = "phone", length = 50, nullable = true)
    private String phone;
    
    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "line1", column = @Column(name = "billing_line1")),
        @AttributeOverride(name = "line2", column = @Column(name = "billing_line2")),
        @AttributeOverride(name = "city", column = @Column(name = "billing_city")),
        @AttributeOverride(name = "region", column = @Column(name = "billing_region")),
        @AttributeOverride(name = "postcode", column = @Column(name = "billing_postcode")),
        @AttributeOverride(name = "country", column = @Column(name = "billing_country"))
    })
    private Address billingAddress;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "line1", column = @Column(name = "shipping_line1")),
        @AttributeOverride(name = "line2", column = @Column(name = "shipping_line2")),
        @AttributeOverride(name = "city", column = @Column(name = "shipping_city")),
        @AttributeOverride(name = "region", column = @Column(name = "shipping_region")),
        @AttributeOverride(name = "postcode", column = @Column(name = "shipping_postcode")),
        @AttributeOverride(name = "country", column = @Column(name = "shipping_country"))
    })
    private Address shippingAddress;
    
    @Column(name = "marketing_opt_in", nullable = false)
    private Boolean marketingOptIn = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CustomerStatus status = CustomerStatus.ACTIVE;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum CustomerStatus {
        ACTIVE, DELETED
    }
}

