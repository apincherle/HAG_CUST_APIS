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
    private String note;
    private ShopifyCustomerRef customer;
    @JsonProperty("line_items")
    private List<ShopifyLineItemPayload> lineItems = new ArrayList<>();
    @JsonProperty("cancelled_at")
    private String cancelledAt;
    /** Cart / checkout attributes (Globo sometimes mirrors here). */
    @JsonProperty("note_attributes")
    private List<ShopifyPropertyPayload> noteAttributes = new ArrayList<>();
    /** Comma-separated in REST webhooks. */
    private String tags;
    @JsonProperty("source_name")
    private String sourceName;
    @JsonProperty("app_id")
    private Long appId;
}
