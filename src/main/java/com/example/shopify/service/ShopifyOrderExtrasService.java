package com.example.shopify.service;

import com.example.shopify.dto.ShopifyLineItemPayload;
import com.example.shopify.dto.ShopifyOrderPayload;
import com.example.shopify.dto.ShopifyPropertyPayload;
import com.example.shopify.entity.ShopifyOrderExtras;
import com.example.shopify.repository.ShopifyOrderExtrasRepository;
import com.example.shopify.support.GloboCardCapture;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShopifyOrderExtrasService {

    private static final Logger log = LoggerFactory.getLogger(ShopifyOrderExtrasService.class);

    private final ShopifyOrderExtrasRepository repository;
    private final ObjectMapper objectMapper;

    public ShopifyOrderExtrasService(ShopifyOrderExtrasRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void upsertFromOrder(ShopifyOrderPayload order) {
        if (order.getId() == null) {
            return;
        }
        ShopifyOrderExtras extras = repository.findById(order.getId()).orElse(new ShopifyOrderExtras());
        extras.setShopifyOrderId(order.getId());
        extras.setShopifyOrderName(order.getName());
        extras.setOrderNote(order.getNote());
        extras.setNoteAttributesJson(toJson(order.getNoteAttributes()));
        extras.setTags(order.getTags());
        extras.setSourceName(order.getSourceName());
        extras.setSubscriptionMetadataJson(buildSubscriptionMetadataJson(order));
        extras.setLineItemsJson(buildLineItemsJson(order));
        extras.setGloboCardsJson(GloboCardCapture.toOrderJson(order, objectMapper));
        repository.save(extras);
        log.debug("Saved shopify_order_extras for order {}", order.getId());
    }

    public String linePropertiesJson(ShopifyLineItemPayload lineItem) {
        if (lineItem.getLineProperties() == null || lineItem.getLineProperties().isEmpty()) {
            return null;
        }
        return toJson(lineItem.getLineProperties());
    }

    public String globoCardsJson(ShopifyLineItemPayload lineItem) {
        return GloboCardCapture.toLineJson(lineItem, objectMapper);
    }

    private String buildLineItemsJson(ShopifyOrderPayload order) {
        if (order.getLineItems() == null || order.getLineItems().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(order.getLineItems());
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize line_items for order {}: {}", order.getId(), e.getMessage());
            return null;
        }
    }

    private String buildSubscriptionMetadataJson(ShopifyOrderPayload order) {
        ObjectNode root = objectMapper.createObjectNode();
        if (order.getSourceName() != null) {
            root.put("source_name", order.getSourceName());
        }
        if (order.getAppId() != null) {
            root.put("app_id", order.getAppId());
        }
        if (order.getTags() != null && !order.getTags().isBlank()) {
            root.put("tags", order.getTags());
        }

        ArrayNode lines = objectMapper.createArrayNode();
        for (ShopifyLineItemPayload item : order.getLineItems()) {
            if (item.getId() == null) {
                continue;
            }
            ObjectNode lineNode = objectMapper.createObjectNode();
            lineNode.put("line_item_id", item.getId());
            if (item.getSku() != null) {
                lineNode.put("sku", item.getSku());
            }
            if (item.getSellingPlanAllocation() != null) {
                try {
                    lineNode.set("selling_plan_allocation",
                            objectMapper.valueToTree(item.getSellingPlanAllocation()));
                } catch (Exception ignored) {
                    // keep other fields
                }
            }
            List<ShopifyPropertyPayload> subProps = subscriptionRelatedProperties(item.getLineProperties());
            if (!subProps.isEmpty()) {
                try {
                    lineNode.set("subscription_properties", objectMapper.valueToTree(subProps));
                } catch (Exception ignored) {
                    // keep other fields
                }
            }
            if (lineNode.size() > 1) {
                lines.add(lineNode);
            }
        }
        if (!lines.isEmpty()) {
            root.set("line_items", lines);
        }
        return root.isEmpty() ? null : root.toString();
    }

    /**
     * Subscribee and similar apps often prefix properties or use selling-plan-related names.
     */
    private static List<ShopifyPropertyPayload> subscriptionRelatedProperties(List<ShopifyPropertyPayload> properties) {
        if (properties == null) {
            return List.of();
        }
        List<ShopifyPropertyPayload> matched = new ArrayList<>();
        for (ShopifyPropertyPayload p : properties) {
            if (p == null || p.getName() == null) {
                continue;
            }
            String name = p.getName().toLowerCase();
            if (name.contains("subscription")
                    || name.contains("subscribee")
                    || name.contains("selling_plan")
                    || name.contains("delivery")
                    || name.startsWith("_")) {
                matched.add(p);
            }
        }
        return matched;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof List<?> list && list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize Shopify extras JSON: {}", e.getMessage());
            return null;
        }
    }

    /** Detect Globo file-upload URLs stored as property values. */
    public static Map<String, String> fileUploadProperties(List<ShopifyPropertyPayload> properties) {
        Map<String, String> files = new LinkedHashMap<>();
        if (properties == null) {
            return files;
        }
        for (ShopifyPropertyPayload p : properties) {
            if (p == null || p.getValue() == null || p.getName() == null) {
                continue;
            }
            String v = p.getValue().trim();
            if (v.startsWith("http://") || v.startsWith("https://")) {
                files.put(p.getName(), v);
            }
        }
        return files;
    }
}
