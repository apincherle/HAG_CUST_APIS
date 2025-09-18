package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlacementMetadataDto {
    @JsonProperty("created_date")
    private String createdDate;
    
    @JsonProperty("created_channel")
    private String createdChannel;
    
    @JsonProperty("created_by")
    private UserInfoDto createdBy;
    
    @JsonProperty("modified_date")
    private String modifiedDate;
    
    @JsonProperty("modified_channel")
    private String modifiedChannel;
    
    @JsonProperty("modified_by")
    private UserInfoDto modifiedBy;
}
