package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UnderwriterPool {
    @JsonProperty("_xid")
    private String xid;
    
    @JsonProperty("first_name")
    private String firstName;
    
    @JsonProperty("last_name")
    private String lastName;
    
    private Organisation organisation;
    private Company company;

    @Data
    public static class Organisation {
        @JsonProperty("_xid")
        private String xid;
        private String name;
    }

    @Data
    public static class Company {
        @JsonProperty("_xid")
        private String xid;
        private String name;
    }
} 