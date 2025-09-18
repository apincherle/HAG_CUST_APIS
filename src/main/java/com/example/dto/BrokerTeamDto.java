package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BrokerTeamDto {
    @JsonProperty("team_id")
    private String teamId;
    
    @JsonProperty("team_name")
    private String teamName;
    
    @JsonProperty("company_name")
    private String companyName;
    
    @JsonProperty("branch_name")
    private String branchName;
}
