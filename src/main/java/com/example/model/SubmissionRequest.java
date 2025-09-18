package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class SubmissionRequest {
    @JsonProperty("_id")
    private String id;
    
    @JsonProperty("_metadata")
    private Metadata metadata;
    
    private String type;
    private String status;
    private String name;
    
    @JsonProperty("general_message")
    private String generalMessage;
    
    @JsonProperty("created_date")
    private String createdDate;
    
    @JsonProperty("sent_date")
    private String sentDate;
    
    @JsonProperty("created_by")
    private User createdBy;
    
    @JsonProperty("sent_by")
    private User sentBy;
    
    @JsonProperty("broker_team")
    private BrokerTeam brokerTeam;
    
    @JsonProperty("total_submissions")
    private Integer totalSubmissions;
    
    private List<Approval> approval;
    
    public SubmissionRequest() {
        this.approval = new ArrayList<>();
    }
    
    @Data
    public static class Approval {
        @JsonProperty("_id")
        private String id;
        
        private User user;
        
        @JsonProperty("sent_date")
        private String sentDate;
        
        private String message;
        private String status;
    }
}
