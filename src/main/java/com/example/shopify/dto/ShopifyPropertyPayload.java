package com.example.shopify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Shopify line item property or order note_attribute (name/value pair).
 * Globo Product Options and many subscription apps store data here.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyPropertyPayload {

    private String name;
    private String value;
}
