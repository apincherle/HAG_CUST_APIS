package com.example.shopify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyLineItemPayload {

    private Long id;
    private String sku;
    private Integer quantity;
    @JsonProperty("product_id")
    private Long productId;
    @JsonProperty("variant_id")
    private Long variantId;
}
