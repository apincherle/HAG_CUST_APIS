package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class PlacementMarketDto {
    @JsonProperty("placement_id")
    private String placementId;
    
    private PlacementMetadataDto metadata;
    
    @JsonProperty("broker_team")
    private BrokerTeamDto brokerTeam;
    
    @JsonProperty("broker_user")
    private BrokerUserDto brokerUser;
    
    @JsonProperty("client_name")
    private String clientName;
    
    private String description;
    
    @JsonProperty("effective_year")
    private Integer effectiveYear;
    
    @JsonProperty("earliest_inception_date")
    private String earliestInceptionDate;
    
    private String status;
    
    private String type;
    
    private List<ProgrammeDto> programmes;
}
