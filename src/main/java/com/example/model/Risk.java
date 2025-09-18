package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Risk {
    @JsonProperty("_id")
    private String id;
    
    @JsonProperty("risk_code")
    private String riskCode;
    
    @JsonProperty("currency_code")
    private String currencyCode;
    
    private Double amount;
    private Double rate;
} 