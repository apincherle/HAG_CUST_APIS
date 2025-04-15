package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Metadata {
    @JsonProperty("creation_date")
    private String creationDate;
    
    @JsonProperty("creation_channel")
    private String creationChannel;
    
    @JsonProperty("creation_user")
    private String creationUser;
    
    @JsonProperty("modified_date")
    private String modifiedDate;
    
    @JsonProperty("modified_channel")
    private String modifiedChannel;
    
    @JsonProperty("modified_user")
    private String modifiedUser;
} 