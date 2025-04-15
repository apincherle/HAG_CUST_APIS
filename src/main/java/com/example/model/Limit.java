package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Limit {
    @JsonProperty("_id")
    private String _id;
    // Add other limit properties as needed
} 