package com.example.shopify.dto;

import com.example.shopify.support.GloboCardCapture;
import com.example.shopify.support.ShopifyWebhookTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class ShopifyOrderPayloadDeserializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void orderCreateFixture_deserializesGloboLineProperties() throws Exception {
        byte[] body = ShopifyWebhookTestSupport.loadFixture(ShopifyWebhookTestSupport.ORDER_CREATE_FIXTURE);
        ShopifyOrderPayload order = objectMapper.readValue(body, ShopifyOrderPayload.class);

        assertNotNull(order.getLineItems());
        assertEquals(1, order.getLineItems().size());
        assertFalse(order.getLineItems().get(0).getLineProperties().isEmpty());
        assertEquals("Charizard", order.getLineItems().get(0).getLineProperties().get(0).getValue());

        String globoJson = GloboCardCapture.toOrderJson(order, objectMapper);
        assertNotNull(globoJson);
    }
}
