package com.example.shopify.service;

import com.example.shopify.config.ShopifyProperties;
import com.example.shopify.dto.ShopifyLineItemPayload;
import com.example.shopify.dto.ShopifyOrderPayload;
import com.example.shopify.entity.PurchaseEntitlement;
import com.example.shopify.repository.PurchaseEntitlementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ShopifyOrderWebhookService {

    private static final Logger log = LoggerFactory.getLogger(ShopifyOrderWebhookService.class);

    private final PurchaseEntitlementRepository entitlementRepository;
    private final ShopifyOrderExtrasService orderExtrasService;
    private final ShopifyProperties properties;
    private final ObjectMapper objectMapper;

    public ShopifyOrderWebhookService(
            PurchaseEntitlementRepository entitlementRepository,
            ShopifyOrderExtrasService orderExtrasService,
            ShopifyProperties properties,
            ObjectMapper objectMapper) {
        this.entitlementRepository = entitlementRepository;
        this.orderExtrasService = orderExtrasService;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void handleOrderPaid(byte[] payload) throws Exception {
        ShopifyOrderPayload order = objectMapper.readValue(payload, ShopifyOrderPayload.class);
        if (order.getId() == null) {
            throw new IllegalArgumentException("Shopify order payload missing id");
        }
        if (order.getFinancialStatus() != null
                && !"paid".equalsIgnoreCase(order.getFinancialStatus())
                && !"partially_paid".equalsIgnoreCase(order.getFinancialStatus())) {
            log.warn("Ignoring orders/paid webhook for order {} with financial_status={}",
                    order.getId(), order.getFinancialStatus());
            return;
        }
        orderExtrasService.upsertFromOrder(order);

        Long shopifyCustomerId = resolveCustomerId(order);
        if (shopifyCustomerId == null) {
            throw new IllegalArgumentException("Order " + order.getId() + " has no customer id");
        }

        int created = 0;
        for (ShopifyLineItemPayload lineItem : order.getLineItems()) {
            if (lineItem.getId() == null) {
                continue;
            }
            String tierCode = resolveTierCode(lineItem.getSku());
            if (tierCode == null) {
                log.debug("Skipping line item {} — SKU {} not mapped to a tier",
                        lineItem.getId(), lineItem.getSku());
                continue;
            }
            if (entitlementRepository.findByShopifyOrderIdAndShopifyLineItemId(
                    order.getId(), lineItem.getId()).isPresent()) {
                continue;
            }
            int quantity = lineItem.getQuantity() != null && lineItem.getQuantity() > 0
                    ? lineItem.getQuantity() : 1;
            int cardsAllowed = resolveCardsAllowed(tierCode) * quantity;

            PurchaseEntitlement entitlement = new PurchaseEntitlement();
            entitlement.setShopifyOrderId(order.getId());
            entitlement.setShopifyLineItemId(lineItem.getId());
            entitlement.setShopifyCustomerId(shopifyCustomerId);
            entitlement.setShopifyOrderName(order.getName());
            entitlement.setLineItemTitle(lineItem.getTitle());
            entitlement.setLinePropertiesJson(orderExtrasService.linePropertiesJson(lineItem));
            entitlement.setGloboCardsJson(orderExtrasService.globoCardsJson(lineItem));
            entitlement.setTierCode(tierCode);
            entitlement.setCardsAllowed(cardsAllowed);
            entitlement.setCardsUsed(0);
            entitlement.setStatus(PurchaseEntitlement.EntitlementStatus.ACTIVE);
            entitlementRepository.save(entitlement);
            created++;
        }
        log.info("Processed orders/paid for order {} — {} new entitlement(s), extras persisted",
                order.getId(), created);
    }

    @Transactional
    public void handleOrderCancelled(byte[] payload) throws Exception {
        ShopifyOrderPayload order = objectMapper.readValue(payload, ShopifyOrderPayload.class);
        if (order.getId() == null) {
            throw new IllegalArgumentException("Shopify order payload missing id");
        }
        List<PurchaseEntitlement> entitlements = entitlementRepository.findByShopifyOrderId(order.getId());
        for (PurchaseEntitlement entitlement : entitlements) {
            if (entitlement.getStatus() == PurchaseEntitlement.EntitlementStatus.CANCELLED) {
                continue;
            }
            entitlement.setStatus(PurchaseEntitlement.EntitlementStatus.CANCELLED);
            entitlementRepository.save(entitlement);
        }
        log.info("Cancelled {} entitlement(s) for order {}", entitlements.size(), order.getId());
    }

    @Transactional
    public void handleOrderCreateOrUpdate(String topic, byte[] payload) throws Exception {
        ShopifyOrderPayload order = objectMapper.readValue(payload, ShopifyOrderPayload.class);
        orderExtrasService.upsertFromOrder(order);
        log.info("Persisted {} extras for order {} ({})", topic, order.getId(), order.getName());
    }

    private Long resolveCustomerId(ShopifyOrderPayload order) {
        if (order.getCustomer() != null && order.getCustomer().getId() != null) {
            return order.getCustomer().getId();
        }
        return null;
    }

    private String resolveTierCode(String sku) {
        if (sku == null || sku.isBlank()) {
            return null;
        }
        Map<String, String> mapping = properties.getTierSkuMapping();
        if (mapping.isEmpty()) {
            return null;
        }
        String trimmed = sku.trim();
        if (mapping.containsKey(trimmed)) {
            return mapping.get(trimmed);
        }
        return mapping.entrySet().stream()
                .filter(e -> trimmed.equalsIgnoreCase(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private int resolveCardsAllowed(String tierCode) {
        Map<String, Integer> cards = properties.getTierCardsAllowed();
        if (cards.containsKey(tierCode)) {
            return cards.get(tierCode);
        }
        return cards.getOrDefault(tierCode.toUpperCase(), 1);
    }
}
