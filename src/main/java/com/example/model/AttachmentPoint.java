package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class AttachmentPoint {
    @JsonProperty("_id")
    private String id;
    
    @JsonProperty("currency_code")
    private String currencyCode;
    
    private Double amount;
    private Double percentage;
    
    @JsonProperty("basis_type_codes")
    private List<String> basisTypeCodes;
    
    @JsonProperty("basis_description")
    private String basisDescription;
    
    private String specification;
    
    public AttachmentPoint() {
        this.basisTypeCodes = new ArrayList<>();
    }
}
