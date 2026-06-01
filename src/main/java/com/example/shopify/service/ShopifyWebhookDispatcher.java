package com.example.shopify.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ShopifyWebhookDispatcher {

    private static final Logger log = LoggerFactory.getLogger(ShopifyWebhookDispatcher.class);

    private static final Set<String> CUSTOMER_TOPICS = Set.of("customers/create", "customers/update");
    private static final Set<String> ORDER_PAID_TOPICS = Set.of("orders/paid");
    private static final Set<String> ORDER_CANCELLED_TOPICS = Set.of("orders/cancelled");
    private static final Set<String> ORDER_INFO_TOPICS = Set.of("orders/create", "orders/updated");

    private final ShopifyCustomerSyncService customerSyncService;
    private final ShopifyOrderWebhookService orderWebhookService;

    public ShopifyWebhookDispatcher(
            ShopifyCustomerSyncService customerSyncService,
            ShopifyOrderWebhookService orderWebhookService) {
        this.customerSyncService = customerSyncService;
        this.orderWebhookService = orderWebhookService;
    }

    public void dispatch(String topic, byte[] payload) throws Exception {
        if (CUSTOMER_TOPICS.contains(topic)) {
            customerSyncService.handleCustomerWebhook(topic, payload);
            return;
        }
        if (ORDER_PAID_TOPICS.contains(topic)) {
            orderWebhookService.handleOrderPaid(payload);
            return;
        }
        if (ORDER_CANCELLED_TOPICS.contains(topic)) {
            orderWebhookService.handleOrderCancelled(payload);
            return;
        }
        if (ORDER_INFO_TOPICS.contains(topic)) {
            orderWebhookService.handleOrderCreateOrUpdate(topic, payload);
            return;
        }
        log.warn("No handler registered for Shopify topic: {}", topic);
    }
}
