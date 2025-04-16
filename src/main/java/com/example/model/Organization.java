package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Organization {
    @JsonProperty("_xid")
    private String xid;
    
    private String name;
} 