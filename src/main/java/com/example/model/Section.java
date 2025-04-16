package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class Section {
    @JsonProperty("_id")
    private String id;
    
    @JsonProperty("sequence_number")
    private Integer sequenceNumber;
    
    private String status;
    
    @JsonProperty("period_type")
    private String periodType;
    
    @JsonProperty("binding_information")
    private BindingInformation bindingInformation;
    
    @JsonProperty("_metadata")
    private Metadata metadata;
    
    @JsonProperty("geographic_coverage")
    private List<String> geographicCoverage;
    
    private List<Document> documents;
    
    @JsonProperty("facility_usage")
    private List<String> facilityUsage;
    
    @Data
    public static class BindingInformation {
        @JsonProperty("line_percentage")
        private Double linePercentage;
        
        @JsonProperty("written_line")
        private Double writtenLine;
        
        @JsonProperty("signed_line")
        private Double signedLine;
    }
} 