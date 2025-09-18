package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class Deductible {
    @JsonProperty("_id")
    private String id;
    
    @JsonProperty("type_ref")
    private String typeRef;
    
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
    
    public Deductible() {
        this.basisRefs = new ArrayList<>();
    }
}
