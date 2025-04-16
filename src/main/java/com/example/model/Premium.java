package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class Premium {
    @JsonProperty("_id")
    private String id;
    
    @JsonProperty("type_ref")
    private String typeRef;
    
    @JsonProperty("basis_refs")
    private List<String> basisRefs;
    
    @JsonProperty("currency_code")
    private String currencyCode;
    
    private Double amount;
    private Double rate;
} 