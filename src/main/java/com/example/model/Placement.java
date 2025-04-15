package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "placements")
public class Placement {
    @Id
    @JsonProperty("_id")
    private String _id;
    
    @JsonProperty("_metadata")
    private Map<String, Object> _metadata;
    
    private User user;
    private List<String> placement_read_access;
    private Branch branch;
    private String client_name;
    private String description;
    private Integer effective_year;
    private BrokerTeam broker_team;
    private String inception_date;
    private String type;

    @Data
    public static class User {
        @JsonProperty("_xid")
        private String _xid;
        private String first_name;
        private String last_name;
        private String organisation_name;
        private String company_name;
        private String company_xid;
        private String organisation_xid;
    }

    @Data
    public static class Branch {
        @JsonProperty("_xid")
        private String _xid;
        private String name;
    }

    @Data
    public static class BrokerTeam {
        @JsonProperty("_xid")
        private String _xid;
        private String name;
    }
} 