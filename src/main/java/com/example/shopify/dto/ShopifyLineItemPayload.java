package com.example.shopify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyLineItemPayload {

    private Long id;
    private String sku;
    private Integer quantity;
    private String title;
    @JsonProperty("product_id")
    private Long productId;
    @JsonProperty("variant_id")
    private Long variantId;
    /** Globo custom fields, Subscribee markers, file upload URLs, etc. */
    private List<ShopifyPropertyPayload> properties = new ArrayList<>();
    @JsonProperty("selling_plan_allocation")
    private ShopifySellingPlanAllocationPayload sellingPlanAllocation;
}
