package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class Contract {
    @JsonProperty("_id")
    private String id;
    
    private Boolean default_;
    
    @JsonProperty("_metadata")
    private Metadata metadata;
    
    private List<Section> sections;
    
    @JsonProperty("backload_indicator")
    private Boolean backloadIndicator;
    
    @JsonProperty("backload_reason_code")
    private String backloadReasonCode;
    
    @JsonProperty("backload_description")
    private String backloadDescription;
    
    @JsonProperty("submission_state")
    private Document.SubmissionState submissionState;
} 