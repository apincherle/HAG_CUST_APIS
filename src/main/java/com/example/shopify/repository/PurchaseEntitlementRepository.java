package com.example.shopify.repository;

import com.example.shopify.entity.PurchaseEntitlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseEntitlementRepository extends JpaRepository<PurchaseEntitlement, UUID> {

    Optional<PurchaseEntitlement> findByShopifyOrderIdAndShopifyLineItemId(
            Long shopifyOrderId, Long shopifyLineItemId);

    List<PurchaseEntitlement> findByShopifyOrderId(Long shopifyOrderId);
}
