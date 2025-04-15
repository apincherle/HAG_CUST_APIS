package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Programme {
    @JsonProperty("_id")
    private String _id;
    private Boolean default_;
    private BrokerTeam broker_team;
    private User user;
    // Add other programme fields as needed
} 