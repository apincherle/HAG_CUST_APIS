package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class Excess {
    @JsonProperty("_id")
    private String id;
    
    private String type;
    
    @JsonProperty("currency_code")
    private String currencyCode;
    
    private Double amount;
    private Double percentage;
    
    @JsonProperty("basis_refs")
    private List<String> basisRefs;
    
    @JsonProperty("basis_type_code")
    private String basisTypeCode;
    
    private String specification;
    
    public Excess() {
        this.basisRefs = new ArrayList<>();
    }
}
