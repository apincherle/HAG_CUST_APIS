package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UnderwriterPoolItem {
    @JsonProperty("_xid")
    private String _xid;
    private String first_name;
    private String last_name;
    private Organisation organisation;
    private Company company;

    @Data
    public static class Organisation {
        @JsonProperty("_xid")
        private String _xid;
        private String name;
    }

    @Data
    public static class Company {
        @JsonProperty("_xid")
        private String _xid;
        private String name;
    }
} 