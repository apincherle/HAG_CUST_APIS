package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class AdjustmentRate {
    private Double rate;
    
    @JsonProperty("rate_unit")
    private String rateUnit;
    
    @JsonProperty("basis_type_codes")
    private List<String> basisTypeCodes;
    
    @JsonProperty("basis_description")
    private String basisDescription;
    
    public AdjustmentRate() {
        this.basisTypeCodes = new ArrayList<>();
    }
}
