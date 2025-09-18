package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class Document {
    @JsonProperty("_xid")
    private String xid;
    
    private String name;
    
    @JsonProperty("submission_state")
    private SubmissionState submissionState;
    
    @Data
    public static class SubmissionState {
        @JsonProperty("firm_order")
        private SubmissionStateDetails firmOrder;
        
        private SubmissionStateDetails correction;
        
        @JsonProperty("additional_information")
        private SubmissionStateDetails additionalInformation;
        
        private SubmissionStateDetails quote;
    }
    
    @Data
    public static class SubmissionStateDetails {
        private Boolean selected;
        private Boolean locked;
        
        @JsonProperty("active_locks")
        private Integer activeLocks;
        
        @JsonProperty("submission_requests")
        private List<String> submissionRequests;
    }
} 