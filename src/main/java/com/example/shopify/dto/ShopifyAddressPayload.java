package com.example.shopify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyAddressPayload {

    private String address1;
    private String address2;
    private String city;
    private String province;
    private String zip;
    private String country;
    @JsonProperty("country_code")
    private String countryCode;
    private String phone;
}
