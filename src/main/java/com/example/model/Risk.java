package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Risk {
    @JsonProperty("_id")
    private String _id;
    private String risk_code;
    private String currency_code;
    private Double amount;
    private Double rate;
} 