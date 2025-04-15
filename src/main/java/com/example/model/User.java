package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class User {
    @JsonProperty("_xid")
    private String xid;
    
    @JsonProperty("first_name")
    private String firstName;
    
    @JsonProperty("last_name")
    private String lastName;
    
    @JsonProperty("organisation_name")
    private String organisationName;
    
    @JsonProperty("company_name")
    private String companyName;
    
    @JsonProperty("company_xid")
    private String companyXid;
    
    @JsonProperty("organisation_xid")
    private String organisationXid;
} 