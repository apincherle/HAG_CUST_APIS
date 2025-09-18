package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class Programme {
    @JsonProperty("_id")
    private String id;
    
    private Boolean default_;
    
    @JsonProperty("broker_team")
    private BrokerTeam brokerTeam;
    
    private User user;
    
    private String description;
    
    @JsonProperty("inception_date")
    private String inceptionDate;
    
    @JsonProperty("status_code")
    private String statusCode;
    
    @JsonProperty("sequence_number")
    private Integer sequenceNumber;
    
    @JsonProperty("documents")
    private List<Document> documents;
    
    @JsonProperty("contracts")
    private List<Contract> contracts;
    
    @JsonProperty("submission_state")
    private Document.SubmissionState submissionState;
    
    @JsonProperty("_metadata")
    private Metadata metadata;

    public Programme() {
        this.documents = new ArrayList<>();
        this.contracts = new ArrayList<>();
    }
} 