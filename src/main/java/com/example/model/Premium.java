package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class Premium {
    @JsonProperty("_id")
    private String id;
    
    @JsonProperty("type_ref")
    private String typeRef;
    
    private String type;
    
    @JsonProperty("currency_code")
    private String currencyCode;
    
    private Double amount;
    private Double rate;
    
    @JsonProperty("rate_unit_code")
    private String rateUnitCode;
    
    @JsonProperty("basis_refs")
    private List<String> basisRefs;
    
    @JsonProperty("basis_type_code")
    private String basisTypeCode;
    
    @JsonProperty("basis_percentage")
    private Double basisPercentage;
    
    @JsonProperty("adjustment_type_code")
    private String adjustmentTypeCode;
    
    @JsonProperty("adjustment_rate")
    private AdjustmentRate adjustmentRate;
    
    @JsonProperty("discount_applied_indicator")
    private String discountAppliedIndicator;
    
    @JsonProperty("period_type_code")
    private String periodTypeCode;

    public Premium() {
        this.basisRefs = new ArrayList<>();
    }
} 