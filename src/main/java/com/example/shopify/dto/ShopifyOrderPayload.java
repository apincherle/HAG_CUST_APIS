package com.example.shopify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyOrderPayload {

    private Long id;
    private String name;
    @JsonProperty("financial_status")
    private String financialStatus;
    private String email;
    private ShopifyCustomerRef customer;
    @JsonProperty("line_items")
    private List<ShopifyLineItemPayload> lineItems = new ArrayList<>();
    @JsonProperty("cancelled_at")
    private String cancelledAt;
}
