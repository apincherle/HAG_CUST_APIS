package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class Placement {
    @JsonProperty("_id")
    private String id;
    
    @JsonProperty("_metadata")
    private Metadata metadata;
    
    private User user;
    
    @JsonProperty("placement_read_access")
    private List<String> placementReadAccess;
    
    private Branch branch;
    
    @JsonProperty("client_name")
    private String clientName;
    
    private String description;
    
    @JsonProperty("effective_year")
    private Integer effectiveYear;
    
    @JsonProperty("broker_team")
    private BrokerTeam brokerTeam;
    
    @JsonProperty("inception_date")
    private String inceptionDate;
    
    private String type;
    
    @JsonProperty("underwriter_pool")
    private List<UnderwriterPool> underwriterPool;
} 