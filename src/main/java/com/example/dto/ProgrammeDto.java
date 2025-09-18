package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProgrammeDto {
    @JsonProperty("programme_id")
    private String programmeId;
    
    private String description;
    
    @JsonProperty("earliest_inception_date")
    private String earliestInceptionDate;
    
    private String status;
}
