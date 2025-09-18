package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DigitalContract {
    @JsonProperty("integration_id")
    private String integrationId;
    
    @JsonProperty("integration_code")
    private String integrationCode;
    
    private Template template;
    
    @JsonProperty("is_synced")
    private Boolean isSynced;
    
    @JsonProperty("integration_doc_number")
    private Integer integrationDocNumber;
    
    @JsonProperty("is_doc_not_sent_to_quote")
    private Boolean isDocNotSentToQuote;
    
    @Data
    public static class Template {
        @JsonProperty("_id")
        private String id;
        
        private String code;
        private String name;
    }
}
