package com.example.shopify.webhook;

import com.example.model.Customer;
import com.example.repository.CustomerRepository;
import com.example.shopify.entity.PurchaseEntitlement;
import com.example.shopify.entity.ShopifyWebhookEvent;
import com.example.shopify.entity.ShopifyOrderExtras;
import com.example.shopify.repository.PurchaseEntitlementRepository;
import com.example.shopify.repository.ShopifyOrderExtrasRepository;
import com.example.shopify.repository.ShopifyWebhookEventRepository;
import com.example.shopify.support.ShopifyWebhookTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ShopifyWebhookIntegrationTest {

    private static final long FIXTURE_SHOPIFY_CUSTOMER_ID = 8800123456789L;
    private static final long FIXTURE_SHOPIFY_ORDER_ID = 55000112233L;
    private static final long FIXTURE_LINE_ITEM_ID = 77000112233L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PurchaseEntitlementRepository entitlementRepository;

    @Autowired
    private ShopifyWebhookEventRepository webhookEventRepository;

    @Autowired
    private ShopifyOrderExtrasRepository orderExtrasRepository;

    @Test
    @Order(1)
    @DisplayName("customers/update fixture upserts HAGS customer")
    void customersUpdate_upsertsCustomerFromFixture() throws Exception {
        byte[] body = ShopifyWebhookTestSupport.loadFixture(ShopifyWebhookTestSupport.CUSTOMER_UPDATE_FIXTURE);
        String webhookId = "test-customers-update-" + UUID.randomUUID();

        postWebhook("customers/update", webhookId, body)
                .andExpect(status().isOk());

        Optional<Customer> customer = customerRepository.findByShopifyCustomerId(FIXTURE_SHOPIFY_CUSTOMER_ID);
        assertTrue(customer.isPresent(), "customer should exist after webhook");
        assertEquals("collector@hags-grading.co.uk", customer.get().getEmail());
        assertEquals("Alex Collector", customer.get().getFullName());
        assertEquals("London", customer.get().getShippingAddress().getCity());

        ShopifyWebhookEvent event = webhookEventRepository.findById(webhookId).orElseThrow();
        assertEquals(ShopifyWebhookEvent.ProcessingStatus.OK, event.getStatus());
    }

    @Test
    @Order(2)
    @DisplayName("customers/create fixture uses same HAGS payload shape")
    void customersCreate_upsertsCustomerFromFixture() throws Exception {
        byte[] body = ShopifyWebhookTestSupport.loadFixture(ShopifyWebhookTestSupport.CUSTOMER_UPDATE_FIXTURE);
        String webhookId = "test-customers-create-" + UUID.randomUUID();

        postWebhook("customers/create", webhookId, body)
                .andExpect(status().isOk());

        assertTrue(customerRepository.findByShopifyCustomerId(FIXTURE_SHOPIFY_CUSTOMER_ID).isPresent());
    }

    @Test
    @Order(3)
    @DisplayName("orders/create fixture is accepted (no entitlement while pending)")
    void ordersCreate_acceptsWithoutEntitlement() throws Exception {
        byte[] body = ShopifyWebhookTestSupport.loadFixture(ShopifyWebhookTestSupport.ORDER_CREATE_FIXTURE);
        String webhookId = "test-orders-create-" + UUID.randomUUID();

        postWebhook("orders/create", webhookId, body)
                .andExpect(status().isOk());

        List<PurchaseEntitlement> entitlements =
                entitlementRepository.findByShopifyOrderId(FIXTURE_SHOPIFY_ORDER_ID);
        assertEquals(0, entitlements.size(), "orders/create must not create entitlements");

        ShopifyOrderExtras extras = orderExtrasRepository.findById(FIXTURE_SHOPIFY_ORDER_ID).orElseThrow();
        assertNotNull(extras.getLineItemsJson());
        assertTrue(extras.getLineItemsJson().contains("cardname-1"), "line_items_json should include Globo properties");
        assertNotNull(extras.getGloboCardsJson(), "globo_cards_json should be parsed from line properties");
        assertTrue(extras.getGloboCardsJson().contains("cardname"));
        assertTrue(extras.getGloboCardsJson().contains("Charizard"));
        assertTrue(extras.getNoteAttributesJson().contains("REF-2026-42"));
    }

    @Test
    @Order(4)
    @DisplayName("orders/paid fixture creates BRONZE entitlement for HAGS-SUB-BRONZE")
    void ordersPaid_createsEntitlementFromFixture() throws Exception {
        byte[] body = ShopifyWebhookTestSupport.loadFixture(ShopifyWebhookTestSupport.ORDER_PAID_FIXTURE);
        String webhookId = "test-orders-paid-" + UUID.randomUUID();

        postWebhook("orders/paid", webhookId, body)
                .andExpect(status().isOk());

        PurchaseEntitlement entitlement = entitlementRepository
                .findByShopifyOrderIdAndShopifyLineItemId(FIXTURE_SHOPIFY_ORDER_ID, FIXTURE_LINE_ITEM_ID)
                .orElseThrow();
        assertEquals(FIXTURE_SHOPIFY_CUSTOMER_ID, entitlement.getShopifyCustomerId());
        assertEquals("BRONZE", entitlement.getTierCode());
        assertEquals(10, entitlement.getCardsAllowed());
        assertEquals(PurchaseEntitlement.EntitlementStatus.ACTIVE, entitlement.getStatus());
        assertEquals("#HAGS-1001", entitlement.getShopifyOrderName());
        assertNotNull(entitlement.getGloboCardsJson());
        assertTrue(entitlement.getGloboCardsJson().contains("\"slot\":5"));
        assertTrue(entitlement.getGloboCardsJson().contains("Mewtwo"));
        assertTrue(entitlement.getGloboCardsJson().contains("card_front_url"));

        ShopifyOrderExtras extras = orderExtrasRepository.findById(FIXTURE_SHOPIFY_ORDER_ID).orElseThrow();
        assertEquals("Please handle with care", extras.getOrderNote());
        assertTrue(extras.getSubscriptionMetadataJson().contains("selling_plan"));
        assertTrue(extras.getGloboCardsJson().contains("\"cardname\":\"Charizard\""));
        assertTrue(extras.getGloboCardsJson().contains("\"slot\":5"));
    }

    @Test
    @Order(5)
    @DisplayName("duplicate webhook id is idempotent")
    void duplicateWebhookId_returnsOkWithoutDuplicateEntitlement() throws Exception {
        byte[] body = ShopifyWebhookTestSupport.loadFixture(ShopifyWebhookTestSupport.ORDER_PAID_FIXTURE);
        String webhookId = "test-orders-paid-dup-" + UUID.randomUUID();

        postWebhook("orders/paid", webhookId, body).andExpect(status().isOk());
        postWebhook("orders/paid", webhookId, body).andExpect(status().isOk());

        long entitlementCount = entitlementRepository.findAll().stream()
                .filter(e -> e.getShopifyOrderId().equals(FIXTURE_SHOPIFY_ORDER_ID)
                        && e.getShopifyLineItemId().equals(FIXTURE_LINE_ITEM_ID))
                .count();
        assertEquals(1, entitlementCount);
    }

    @Test
    @Order(6)
    @DisplayName("invalid HMAC is rejected")
    void invalidHmac_returnsUnauthorized() throws Exception {
        byte[] body = ShopifyWebhookTestSupport.loadFixture(ShopifyWebhookTestSupport.CUSTOMER_UPDATE_FIXTURE);

        mockMvc.perform(post(ShopifyWebhookTestSupport.WEBHOOK_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-Shopify-Topic", "customers/update")
                        .header("X-Shopify-Shop-Domain", ShopifyWebhookTestSupport.TEST_SHOP_DOMAIN)
                        .header("X-Shopify-Webhook-Id", "test-invalid-hmac-" + UUID.randomUUID())
                        .header("X-Shopify-Hmac-Sha256", "not-valid"))
                .andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.ResultActions postWebhook(
            String topic, String webhookId, byte[] body) throws Exception {
        String hmac = ShopifyWebhookTestSupport.signHmac(body, ShopifyWebhookTestSupport.TEST_SECRET);
        return mockMvc.perform(post(ShopifyWebhookTestSupport.WEBHOOK_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("X-Shopify-Topic", topic)
                .header("X-Shopify-Shop-Domain", ShopifyWebhookTestSupport.TEST_SHOP_DOMAIN)
                .header("X-Shopify-Webhook-Id", webhookId)
                .header("X-Shopify-Hmac-Sha256", hmac));
    }
}
