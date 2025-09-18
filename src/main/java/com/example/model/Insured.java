package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Insured {
    @JsonProperty("_id")
    private String id;
    private String name;
    private String role;  // enum: ["insured", "reinsured", "retrocedent"]
} 