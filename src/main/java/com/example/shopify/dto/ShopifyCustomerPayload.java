package com.example.shopify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyCustomerPayload {

    private Long id;
    private String email;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    private String phone;
    @JsonProperty("default_address")
    private ShopifyAddressPayload defaultAddress;
    @JsonProperty("updated_at")
    private String updatedAt;
}
