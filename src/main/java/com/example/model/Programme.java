package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class Programme {
    @JsonProperty("_id")
    private String id;
    
    private Boolean default_;
    
    @JsonProperty("broker_team")
    private BrokerTeam brokerTeam;
    
    private User user;
    
    private List<Section> sections;
    
    @JsonProperty("_metadata")
    private Metadata metadata;
} 