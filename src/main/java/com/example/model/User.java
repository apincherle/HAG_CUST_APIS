package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class User {
    @JsonProperty("_xid")
    private String _xid;
    private String first_name;
    private String last_name;
    private String organisation_name;
    private String company_name;
    private String company_xid;
    private String organisation_xid;
} 