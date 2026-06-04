package com.example.shopify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifySellingPlanPayload {

    private Long id;
    private String name;
    private String description;
    @JsonProperty("options")
    private java.util.List<String> options;
}
